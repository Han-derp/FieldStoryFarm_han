package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropMemory;
import com.fieldstory.farm.model.GameClock;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Quality;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.model.item.EventPriceRateProvider;
import com.fieldstory.farm.model.item.Inventory;
import com.fieldstory.farm.model.item.Item;
import com.fieldstory.farm.model.item.ItemType;
import com.fieldstory.farm.model.HarvestLog;
import com.fieldstory.farm.service.HarvestOutcome;
import com.fieldstory.farm.service.HarvestResult;
import com.fieldstory.farm.service.HarvestTransactionService;
import com.fieldstory.farm.service.LandService;
import com.fieldstory.farm.service.LegendaryFirstRewardService;
import com.fieldstory.farm.service.LegendaryService;
import com.fieldstory.farm.service.LogService;
import com.fieldstory.farm.service.MemoryService;
import com.fieldstory.farm.service.QualityScoreInput;
import com.fieldstory.farm.service.QualityService;
import com.fieldstory.farm.service.economy.EconomyService;

import java.util.Objects;

/**
 * {@link HarvestTransactionService} 基础实现（C 模块 品质与传说域，P2）。
 *
 * <p>完整收获事务（验收规范 §一百零三 流程；规则文档 §六十八 18 步）：
 * 检查 MATURE → QualityService 计算 Score → LegendaryService 突破判定
 * → 确定 Quality（§一百零二）→ 售价 = 基础售价 × 品质倍率 × 事件倍率
 * → 发金币 → 发肥料 → 首次传说奖励（§六十七，可选依赖）→ 落档 CropMemory
 * + 生成生命故事 → 写入 HarvestLog（§六十八 ⑮，可选依赖）→ 清除土地 Crop
 * → Soil=TILLED。
 *
 * <p>事务原子性（规则文档 §六十八）：全部计算完成后才依次变更状态
 * （金币→肥料→记忆→土地），无效收获（未种植/无作物/未成熟）不产生
 * 任何变更（概要设计说明书 §11.1）。
 *
 * <p>跨模块依赖契约：
 * <ul>
 *   <li>金币入账经 B 的 {@link EconomyService#addGold}（B-P0-DESIGH "C 收获"）；</li>
 *   <li>土地回退经 A 的 {@link LandService#removeCropAndSetTilled}（决策 D09）；</li>
 *   <li>事件倍率经 {@link EventPriceRateProvider}（D 模块 EventService 接入，
 *       默认 {@code NONE} 恒 1.0）；</li>
 *   <li>时间经 {@link GameClock}（D 正式接口，决策 D14 口径
 *       worldTime = gameDay×24 + gameHour）。</li>
 * </ul>
 */
public class BasicHarvestTransactionService implements HarvestTransactionService {

    /** 世界时间换算：1 游戏日 = 24 游戏小时（规则 §5.1） */
    private static final int HOURS_PER_DAY = 24;

    /** 经济服务（B 模块：基础售价读取与金币入账） */
    private final EconomyService economyService;

    /** 土地服务（A 模块：作物移除与土地回退 TILLED，决策 D09） */
    private final LandService landService;

    /** 品质服务（C：Score 计算与普通档位） */
    private final QualityService qualityService;

    /** 传说服务（C：突破条件/概率/掷骰） */
    private final LegendaryService legendaryService;

    /** 记忆服务（C：档案落档与故事生成） */
    private final MemoryService memoryService;

    /** 世界时钟（D 模块正式接口，收获时刻来源） */
    private final GameClock gameClock;

    /** 事件售价倍率提供者（D 模块实现；默认无事件倍率 1.0） */
    private final EventPriceRateProvider eventPriceRateProvider;

    /** 首次传说奖励服务（C 的 P2 服务；null = 不发放首次奖励，向后兼容） */
    private final LegendaryFirstRewardService firstRewardService;

    /** 收获日志服务（C 的 P2 服务；null = 不写日志，向后兼容） */
    private final LogService logService;

    /**
     * 便捷构造：事件倍率默认 {@link EventPriceRateProvider#NONE}（1.0），
     * 不发放首次传说奖励、不写收获日志（P2 之前旧行为）。
     */
    public BasicHarvestTransactionService(EconomyService economyService,
                                          LandService landService,
                                          QualityService qualityService,
                                          LegendaryService legendaryService,
                                          MemoryService memoryService,
                                          GameClock gameClock) {
        this(economyService, landService, qualityService, legendaryService,
                memoryService, gameClock, EventPriceRateProvider.NONE);
    }

    /**
     * 构造（P2 之前旧签名）：不发放首次传说奖励、不写收获日志。
     *
     * @param economyService        经济服务（B）
     * @param landService           土地服务（A）
     * @param qualityService        品质服务（C）
     * @param legendaryService      传说服务（C）
     * @param memoryService         记忆服务（C）
     * @param gameClock             世界时钟（D）
     * @param eventPriceRateProvider 事件售价倍率提供者（D）；null 视为 NONE
     */
    public BasicHarvestTransactionService(EconomyService economyService,
                                          LandService landService,
                                          QualityService qualityService,
                                          LegendaryService legendaryService,
                                          MemoryService memoryService,
                                          GameClock gameClock,
                                          EventPriceRateProvider eventPriceRateProvider) {
        this(economyService, landService, qualityService, legendaryService,
                memoryService, gameClock, eventPriceRateProvider, null, null);
    }

    /**
     * 完整构造（P2 收获事务 18 步全链路）。
     *
     * @param economyService        经济服务（B）
     * @param landService           土地服务（A）
     * @param qualityService        品质服务（C）
     * @param legendaryService      传说服务（C）
     * @param memoryService         记忆服务（C）
     * @param gameClock             世界时钟（D）
     * @param eventPriceRateProvider 事件售价倍率提供者（D）；null 视为 NONE
     * @param firstRewardService    首次传说奖励服务（C）；null = 不发放（规则文档 §六十七）
     * @param logService            收获日志服务（C）；null = 不写日志（规则文档 §六十八 ⑮）
     */
    public BasicHarvestTransactionService(EconomyService economyService,
                                          LandService landService,
                                          QualityService qualityService,
                                          LegendaryService legendaryService,
                                          MemoryService memoryService,
                                          GameClock gameClock,
                                          EventPriceRateProvider eventPriceRateProvider,
                                          LegendaryFirstRewardService firstRewardService,
                                          LogService logService) {
        this.economyService = Objects.requireNonNull(economyService, "经济服务不能为空");
        this.landService = Objects.requireNonNull(landService, "土地服务不能为空");
        this.qualityService = Objects.requireNonNull(qualityService, "品质服务不能为空");
        this.legendaryService = Objects.requireNonNull(legendaryService, "传说服务不能为空");
        this.memoryService = Objects.requireNonNull(memoryService, "记忆服务不能为空");
        this.gameClock = Objects.requireNonNull(gameClock, "世界时钟不能为空");
        this.eventPriceRateProvider = eventPriceRateProvider == null
                ? EventPriceRateProvider.NONE
                : eventPriceRateProvider;
        this.firstRewardService = firstRewardService;
        this.logService = logService;
    }

    @Override
    public boolean canHarvest(Soil soil) {
        if (soil == null || soil.getState() != SoilState.PLANTED) {
            return false;
        }
        Crop crop = soil.getCrop();
        return crop != null && crop.getGrowthStage() == GrowthStage.MATURE;
    }

    @Override
    public HarvestOutcome harvest(Soil soil) {
        return harvest(soil, null);
    }

    @Override
    public HarvestOutcome harvest(Soil soil, Inventory inventory) {
        // ① 检查 MATURE（验收规范 §一百零三 第一步；失败不改任何状态）
        if (soil == null || soil.getState() != SoilState.PLANTED) {
            return HarvestOutcome.failure(HarvestResult.NOT_PLANTED);
        }
        Crop crop = soil.getCrop();
        if (crop == null) {
            return HarvestOutcome.failure(HarvestResult.NO_CROP);
        }
        if (crop.getGrowthStage() != GrowthStage.MATURE) {
            return HarvestOutcome.failure(HarvestResult.NOT_MATURE);
        }

        // ② 取/补建生命记忆档案（经历数据唯一权威来源，验收规范 §九十四）
        CropMemory memory = memoryService.findMemory(crop.getCropUuid())
                .orElseGet(() -> memoryService.createMemory(crop));

        // ③~⑤ 先完成全部计算（规则文档 §六十八：计算阶段不做任何变更）
        int score = qualityService.calculateScore(QualityScoreInput.of(crop.getCropType(), memory));
        boolean legendary = legendaryService.rollBreakthrough(crop, memory, score);
        Quality quality = legendary ? Quality.LEGENDARY : qualityService.determineQuality(score);
        int basePrice = economyService.calculateBaseSellPrice(crop.getCropType());
        double eventRate = eventPriceRateProvider.priceRateFor(crop.getCropType());
        int sellPrice = (int) Math.round(basePrice * quality.getPriceMultiplier() * eventRate);
        int fertilizerReward = quality.getFertilizerReward();
        long harvestWorldTime = currentWorldTime();

        // ⑥ 发金币（B 的 EconomyService.addGold，金币唯一入口）
        economyService.addGold(sellPrice);

        // ⑦ 发肥料（规则文档 §六十六：品质肥料奖励；背包注入时入包）
        if (inventory != null && fertilizerReward > 0) {
            inventory.addItem(new Item(ItemType.FERTILIZER, fertilizerReward));
        }

        // ⑫ 首次传说奖励 +500 金币（规则文档 §六十七：三种传说各领一次；
        // 未装配奖励服务时跳过，保持 P2 之前旧行为）
        int firstRewardGold = 0;
        if (legendary && firstRewardService != null) {
            firstRewardGold = firstRewardService.claimFirstReward(crop.getCropType());
            if (firstRewardGold > 0) {
                economyService.addGold(firstRewardGold);
            }
        }

        // ⑬⑭ 落档记忆 + 生成生命故事（验收规范 §九十四；规则文档 §七十）
        String story = memoryService.completeHarvest(memory, quality, legendary, harvestWorldTime);

        // ⑮ 写入 HarvestLog（规则文档 §六十八；未装配日志服务时跳过）
        if (logService != null) {
            logService.append(new HarvestLog(crop.getCropUuid(), crop.getCropType(),
                    quality, legendary, sellPrice, fertilizerReward, firstRewardGold,
                    story, harvestWorldTime));
        }

        // ⑯⑰ 清除土地 Crop → Soil=TILLED（A 的 LandService，决策 D09；
        // 必须先完成入账与落档再移除作物，作物移除后无法再读作物信息）
        landService.removeCropAndSetTilled(soil);

        return HarvestOutcome.success(quality, score, sellPrice, fertilizerReward,
                legendary, story, memory, firstRewardGold);
    }

    /** 当前世界时间（游戏小时）：gameDay×24 + gameHour（决策 D14 口径）。 */
    private long currentWorldTime() {
        return (long) gameClock.getGameDay() * HOURS_PER_DAY + gameClock.getGameHour();
    }
}
