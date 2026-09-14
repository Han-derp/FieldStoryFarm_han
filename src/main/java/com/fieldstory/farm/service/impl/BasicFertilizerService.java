package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropMemory;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.item.Inventory;
import com.fieldstory.farm.model.item.ItemType;
import com.fieldstory.farm.service.FertilizeResult;
import com.fieldstory.farm.service.FertilizerService;
import com.fieldstory.farm.service.MemoryService;

import java.util.Objects;

/**
 * {@link FertilizerService} 基础实现（C 模块 品质与传说域，P1 施肥系统）。
 *
 * <p>规则文档 §二十六 / 验收规范 §六十三：
 * <ul>
 *   <li>允许阶段：SPROUT、GROWING（SEED 与 MATURE 不可施）；</li>
 *   <li>每株每天最多 1 次（按 {@link CropMemory#getLastFertilizeGameDay}
 *       判定，D14 口径 long 游戏日）；</li>
 *   <li>生命周期最多 3 次；</li>
 *   <li>每次消耗 1 肥料（B 的 {@link Inventory#consume}，库存不足不扣减）。</li>
 * </ul>
 *
 * <p>施肥效果双轨：
 * <ul>
 *   <li>品质 +8/次（上限 +24）：收获时 {@code BasicQualityService} 直接
 *       读记忆施肥次数计分（规则文档 §三十六），本服务无需额外处理；</li>
 *   <li>成长 +15%/次（上限 +45%）：经 {@link #fertilizerGrowthRate} 提供
 *       纯查询，由 A 的 GrowthService 接入（跨模块协作点）。</li>
 * </ul>
 *
 * <p>校验全部通过后一次性扣库存并落档；任何失败不产生变更
 * （规则文档 §六十八 事务原子性精神）。
 */
public class BasicFertilizerService implements FertilizerService {

    /** 每次施肥成长加成：+15%（规则文档 §二十六） */
    private static final double GROWTH_BONUS_PER_TIME = 0.15;

    /** 记忆服务（施肥次数与施肥日落档） */
    private final MemoryService memoryService;

    /**
     * 注入记忆服务。
     *
     * @param memoryService 生命记忆服务（C 的 P2 服务）
     */
    public BasicFertilizerService(MemoryService memoryService) {
        this.memoryService = Objects.requireNonNull(memoryService, "记忆服务不能为空");
    }

    @Override
    public FertilizeResult fertilize(Crop crop, CropMemory memory,
                                     Inventory inventory, long gameDay) {
        // ① 基础校验（作物与档案必须同时存在）
        if (crop == null || memory == null) {
            return FertilizeResult.NO_CROP_OR_MEMORY;
        }

        // ② 阶段校验（规则文档 §二十六：仅 SPROUT / GROWING）
        GrowthStage stage = crop.getGrowthStage();
        if (stage != GrowthStage.SPROUT && stage != GrowthStage.GROWING) {
            return FertilizeResult.NOT_ALLOWED_STAGE;
        }

        // ③ 每日 1 次校验（验收规范 §六十三；-1 哨兵 = 从未施肥）
        if (memory.getLastFertilizeGameDay() == gameDay) {
            return FertilizeResult.ALREADY_FERTILIZED_TODAY;
        }

        // ④ 生命周期 3 次校验（规则文档 §二十六）
        if (memory.getFertilizerCount() >= MAX_FERTILIZE_PER_LIFE) {
            return FertilizeResult.MAX_TIMES_PER_LIFE;
        }

        // ⑤ 库存校验 + 扣减（B 的 Inventory；null 或不足均视为无肥料）
        if (inventory == null || !inventory.consume(ItemType.FERTILIZER, FERTILIZER_COST)) {
            return FertilizeResult.NOT_ENOUGH_FERTILIZER;
        }

        // ⑥ 落档：施肥次数 +1、记录施肥日（C 的 MemoryService 约定）
        memoryService.recordFertilizer(memory);
        memory.setLastFertilizeGameDay(gameDay);
        return FertilizeResult.SUCCESS;
    }

    @Override
    public double fertilizerGrowthRate(CropMemory memory) {
        Objects.requireNonNull(memory, "档案不能为空");
        int count = Math.max(0, memory.getFertilizerCount());
        // 3 次封顶自然为 +45%（规则文档 §二十六"最多成长+45%"）
        return Math.min(count, MAX_FERTILIZE_PER_LIFE) * GROWTH_BONUS_PER_TIME;
    }
}
