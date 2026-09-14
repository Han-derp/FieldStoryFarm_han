package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;

import java.util.List;

/**
 * 世界模拟服务接口（A 模块 P2：持续世界引擎；验收规范 §八十九）。
 *
 * <p>在线每日结算与离线模拟共用同一套领域逻辑（验收 §八十九：禁止开发
 * OnlineDailyService 一套算法、OfflineSimulationService 另一套算法）；
 * B 模块 {@code OfflineSimulationService} 只负责按时间窗口（游戏日 00:00 边界 +
 * 事件结束时间 + 作物成熟时间，规则 §八十三；验收 §八十八）分段调用本服务。
 *
 * <p>纯函数约束（决策 D18/D19 精神延续）：本接口不依赖 GameClock、不读系统时间、
 * 不调用 RandomProvider——时间（gameDay / worldTimeAtSettle / gameHours）与随机
 * （witherRolls）全部由调用方传入；天气/事件抽取由注入的 D 模块
 * {@link WeatherService}/{@link EventService} 内部完成。
 *
 * <p>只产数据（决策 D24）：不收获、不出售、不动金币（验收 §八十六/§八十七）、
 * 不碰数据库与任何 DAO。
 */
public interface WorldSimulationService {

    /**
     * 分段成长（验收 §八十八）：全部 PLANTED 作物按 {@code gameHours / 24} 折算天数成长，
     * 返回新成熟作物列表；不判枯萎、不换天气（枯萎/天气只发生在日结）。
     *
     * @param farm      农场
     * @param gameHours 本段经过的游戏小时（验收 §二十五：支持非整日成长）
     * @param rates     成长倍率三件套（调用方按当日天气/装饰/事件组装）
     * @return 本段新成熟（成长进度跨过 100）的作物列表；无则空列表
     */
    List<Crop> growSegment(Farm farm, double gameHours, GrowthRates rates);

    /**
     * 每日结算（验收 §八十九 14 步，规则 §八十一）。
     *
     * <p>严格按 §八十九 顺序执行：① 成长汇总 ② 更新阶段 ③ 标记成熟 ④ 日结边界
     * ⑤ 补水判定 ⑥ 更新 droughtStreak ⑦ 枯萎判定 ⑧ 关闭过期事件 ⑨ DailyLog 数据
     * ⑩ GameDay+1 ⑪ 生成新天气 ⑫ 雨天自动补水 ⑬ 抽取事件 ⑭ 返回摘要；
     * 禁止任何步骤重排或增减，循环由调用方驱动。
     *
     * @param farm  农场
     * @param input 一次日结的全部入参
     * @return 每日结算摘要
     */
    DailySimulationResult settleDay(Farm farm, DaySettlementInput input);
}
