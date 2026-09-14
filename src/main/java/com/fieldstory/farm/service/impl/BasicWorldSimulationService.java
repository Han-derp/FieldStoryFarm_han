package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.model.WeatherType;
import com.fieldstory.farm.service.DailySimulationResult;
import com.fieldstory.farm.service.DecorationRateResolver;
import com.fieldstory.farm.service.DaySettlementInput;
import com.fieldstory.farm.service.EventService;
import com.fieldstory.farm.service.GrowthRates;
import com.fieldstory.farm.service.GrowthService;
import com.fieldstory.farm.service.WeatherService;
import com.fieldstory.farm.service.WitherResult;
import com.fieldstory.farm.service.WitherService;
import com.fieldstory.farm.service.WorldSimulationService;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link WorldSimulationService} 基础实现（A 模块 P2：持续世界引擎；
 * 验收规范 §八十九 每日离线 14 步，规则文档 §八十一）。
 *
 * <p>在线每日结算与离线模拟共用本实现（验收 §八十九：禁止 OnlineDailyService 与
 * OfflineSimulationService 两套算法）；B 模块 OfflineSimulationService 负责按分段调用
 * {@link #growSegment} 并在日末切点调用 {@link #settleDay}。
 *
 * <p>与时间约束（决策 D18/D19 精神延续）：本实现不调用 RandomProvider、
 * 不读取系统时间、不依赖 GameClock——枯萎掷骰值由调用方经
 * {@code DaySettlementInput.witherRolls} 按农场遍历顺序传入（取尽视为 1.0，
 * 异常输入不破坏状态）；天气/事件抽取由注入的 D 模块 {@link WeatherService}/
 * {@link EventService} 内部完成。
 *
 * <p>只产数据（决策 D24）：不收获、不出售、不动金币（验收 §八十六/§八十七），
 * 不碰数据库与任何 DAO；摘要经 {@link DailySimulationResult} 返回，落库与循环
 * 驱动由调用方（B 模块 OfflineSimulationService）负责。
 */
public class BasicWorldSimulationService implements WorldSimulationService {

    /** 成长服务：分段成长入口（构造器注入同层服务，BasicGrowthService 注入 WateringService 先例） */
    private final GrowthService growthService;

    /** 枯萎服务：当日天气记录与枯萎判定（构造器注入同层服务） */
    private final WitherService witherService;

    /** 天气服务：日末掷出新天气（构造器注入，D 模块交付） */
    private final WeatherService weatherService;

    /** 事件服务：关闭过期事件与日末抽取新事件（构造器注入，D 模块交付） */
    private final EventService eventService;

    /**
     * 构造器注入四个同层服务（BasicGrowthService 注入 WateringService 的先例）。
     *
     * @param growthService  成长服务
     * @param witherService  枯萎服务
     * @param weatherService 天气服务（D 模块）
     * @param eventService   事件服务（D 模块）
     */
    public BasicWorldSimulationService(GrowthService growthService,
            WitherService witherService,
            WeatherService weatherService,
            EventService eventService) {
        this.growthService = growthService;
        this.witherService = witherService;
        this.weatherService = weatherService;
        this.eventService = eventService;
    }

    @Override
    public List<Crop> growSegment(Farm farm, double gameHours, GrowthRates rates) {
        return growSegment(farm, gameHours, rates, null);
    }

    @Override
    public List<Crop> growSegment(Farm farm, double gameHours, GrowthRates rates,
                                  DecorationRateResolver decorationResolver) {
        // 验收 §八十八：本方法只处理当前时间段成长，不判枯萎、不换天气。
        if (gameHours <= 0) {
            return List.of();
        }

        GrowthRates effectiveRates = rates == null ? GrowthRates.P0 : rates;
        double elapsedGameDays = gameHours / 24.0;
        List<Crop> newlyMatured = new ArrayList<>();

        // A/B P2 决议：DecorationRate 天然逐 Crop。
        // A 不实现装饰规则，只逐格调用 B 提供的 resolver；三参调用 resolver=null，
        // 完全回退既有单一 rates.decorationRate() 行为。
        for (Soil soil : farm.getSoils()) {
            if (soil.getState() != SoilState.PLANTED || soil.getCrop() == null) {
                continue;
            }

            Crop crop = soil.getCrop();
            if (crop.getGrowthStage() == GrowthStage.WITHERED) {
                continue;
            }

            double decorationRate = decorationResolver == null
                    ? effectiveRates.decorationRate()
                    : decorationResolver.decorationRate(
                            soil.getRow(), soil.getColumn(), crop.getCropType());

            GrowthRates perCropRates = new GrowthRates(
                    effectiveRates.weatherRate(),
                    decorationRate,
                    effectiveRates.eventRate());

            boolean wasMature = crop.getGrowthProgress() >= 100.0;
            growthService.applyGrowth(crop, elapsedGameDays, perCropRates);
            if (!wasMature && crop.getGrowthProgress() >= 100.0) {
                newlyMatured.add(crop);
            }
        }
        return newlyMatured;
    }

    @Override
    public DailySimulationResult settleDay(Farm farm, DaySettlementInput input) {
        List<Crop> planted = plantedCrops(farm);

        // §八十九 第 1 步：处理当前时间段成长 —— 今日成长已由 growSegment 完成，
        // settleDay 内只做汇总，不重复结算成长
        // §八十九 第 2 步：更新阶段 —— GrowthService.applyGrowth 已内建 stageOf
        // （阈值 ≥20 SPROUT / ≥50 GROWING / ≥100 MATURE，验收 §二十二），无需重复

        // §八十九 第 3 步：标记成熟 —— progress ≥100 → maturedCount+1；
        // 不收获、不自动出售（验收 §八十七：stage = MATURE，玩家返回后亲自点击收获）
        int maturedCount = 0;
        for (Crop crop : planted) {
            if (crop.getGrowthProgress() >= 100.0) {
                maturedCount++;
            }
        }

        // §八十九 第 4 步：到达日结边界 —— worldTimeAtSettle 即当日日末切点（00:00），
        // 由调用方（B 模块）按分段保证

        // §八十九 第 5 步：处理补水 —— 有效补水判定（WitherService.isEffectivelyHydrated：
        // 当日主动浇水成功 或 当日天气为 RAIN；规则 §二十九），内嵌于第 6 步调用的
        // recordDailyWeather（DROUGHT 分支无有效补水 streak+1 否则归零）
        // §八十九 第 6 步：更新 droughtStreak —— WitherService.recordDailyWeather
        // 已内建 streak 逻辑，每 PLANTED 作物调用一次（规则 §二十一~§二十三；验收 §五十二）：
        // RAIN → rainCount+1、lastHydratedWorldTime、streak=0（雨天自动补水）；
        // DROUGHT → droughtCount+1，无有效补水 streak+1 否则归零；
        // GREEN_RAIN → greenRainCount+1、streak=0；SUNNY → streak=0
        for (Crop crop : planted) {
            witherService.recordDailyWeather(crop, input.weather(), input.gameDay(),
                    input.worldTimeAtSettle());
        }
        // 当日雨天自动补水计数（规则 §二十一）：当日天气为 RAIN 时全部 PLANTED
        // 作物均获补水（rainCount+1、streak=0）
        int rainHydratedCount = input.weather() == WeatherType.RAIN ? planted.size() : 0;

        // §八十九 第 7 步：枯萎判定 —— 四条件（规则 §二十八：非 SEED、当日存在干旱风险、
        // 没有有效补水、streak 达风险区间）；roll 从 witherRolls 按农场遍历顺序消费，
        // 每次枯萎掷骰取下一个，取尽视为 1.0（必不枯萎，异常输入不破坏状态）
        int witheredCount = 0;
        for (int i = 0; i < planted.size(); i++) {
            double roll = nextRoll(input.witherRolls(), i);
            WitherResult result = witherService.judgeWither(planted.get(i), input.weather(),
                    input.gameDay(), input.witherMitigationRate(), roll);
            if (result == WitherResult.WITHERED) {
                witheredCount++;
            }
        }

        // §八十九 第 8 步：关闭过期 Event —— currentWorldTime >= endWorldTime 时
        // 重置为 NONE（规则 §八十一 第 ⑤ 步）
        eventService.expireIfNeeded(input.worldTimeAtSettle());

        // §八十九 第 9 步：保存 DailyLog —— 决策 D24：A 只产数据、不碰数据库与任何 DAO；
        // 摘要（gameDay / weather / event / maturedCount / witheredCount / rainHydratedCount）
        // 在方法末尾统一填入 DailySimulationResult 返回，落库由调用方（B/E）负责

        // §八十九 第 10 步：GameDay+1 —— 结果记录 gameDay 为结算当日；
        // A 不负责推进 D 的时钟（时钟推进由 B 模块 OfflineSimulationService 驱动）

        // §八十九 第 11 步：生成新 Weather —— 概率 晴 40% / 雨 25% / 旱 20% / 绿雨 15%
        // （规则 §十九），结果由 BasicWeatherService 写入 D 的 WeatherState
        WeatherType newWeather = weatherService.rollDailyWeather((int) (input.gameDay() + 1));

        // §八十九 第 12 步：雨天自动补水（规则 §八十一 第 ⑨ 步）——
        // 决策 D30：只写 lastHydratedWorldTime（补水不计数），
        // rainCount/greenRainCount/streak 一律在次日日结 ⑤⑥ recordDailyWeather 计数
        // （每雨日 +1 一次，验收 §五十"累计雨日数"口径；待团队确认转交卡）
        if (newWeather == WeatherType.RAIN) {
            for (Crop crop : planted) {
                crop.setLastHydratedWorldTime(input.worldTimeAtSettle());
            }
        }

        // §八十九 第 13 步：抽取当天 Event —— 概率 74/5/8/10/3，一次随机抽取决定结果
        // （规则 §四十七；验收 §九十），结果由 BasicEventService 写入 D 的 EventState
        // 决策 D29：返回值不进当日摘要；次日 EventRate 由调用方读 EventState 组装
        eventService.rollDailyEvent((int) (input.gameDay() + 1));

        // §八十九 第 14 步：继续下一日 —— 方法返回摘要，
        // 循环由调用方（B 模块 OfflineSimulationService）驱动
        return new DailySimulationResult(input.gameDay(), input.weather(),
                input.eventInEffect(), maturedCount, witheredCount, rainHydratedCount);
    }

    /**
     * 按 {@link Farm#getSoils()} 遍历顺序收集全部 PLANTED 作物
     * （SoilState == PLANTED 且 crop != null，防御存档恢复异常）。
     * 该顺序即 witherRolls 的消费顺序。
     */
    private List<Crop> plantedCrops(Farm farm) {
        List<Crop> crops = new ArrayList<>();
        for (Soil soil : farm.getSoils()) {
            if (soil.getState() == SoilState.PLANTED && soil.getCrop() != null) {
                crops.add(soil.getCrop());
            }
        }
        return crops;
    }

    /**
     * 取第 index 个枯萎掷骰值：列表为 null 或取尽（index ≥ size）视为 1.0
     * （必不枯萎，异常输入不破坏状态）；null 元素同视为 1.0。
     */
    private static double nextRoll(List<Double> rolls, int index) {
        if (rolls == null || index >= rolls.size()) {
            return 1.0;
        }
        Double roll = rolls.get(index);
        return roll == null ? 1.0 : roll;
    }
}
