package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropMemory;
import com.fieldstory.farm.model.item.Inventory;

/**
 * 施肥服务接口（C 模块 品质与传说域，P1 施肥系统；验收规范 §六十三）。
 *
 * <p>规则（规则文档 §二十六）：
 * <ul>
 *   <li>允许阶段：SPROUT / GROWING；</li>
 *   <li>每株每天最多 1 次；</li>
 *   <li>每株生命周期最多 3 次；</li>
 *   <li>每次消耗 1 肥料（经 B 的 {@link Inventory#consume} 扣减）；</li>
 *   <li>效果：成长速度 +15%/次（每株最多 +45%）、品质评分 +8/次
 *       （最多 +24，经 C 的 QualityService 施肥分生效，规则文档 §三十六）。</li>
 * </ul>
 *
 * <p>施肥事实落档到 {@link CropMemory}（C 的 MemoryService 经历记录），
 * 收获时 QualityService/LegendaryService 从记忆读取施肥次数——因此施肥
 * 立即影响最终品质判定与传说条件（彩虹玉米需施肥 ≥1，规则文档 §四十三）。
 *
 * <p><b>A 对接待办（验收规范 §六十四「P1 Crop 新增字段」）</b>：
 * 验收规范要求 Crop 增加字段 {@code int fertilizerCount} 与
 * {@code LocalDate lastFertilizedGameDay}（Crop 属 A 模块，C 未改动）。
 * A 补齐后，施肥流程需同时写入 Crop 字段：施肥成功后
 * {@code crop.setFertilizerCount(+1)}、{@code crop.setLastFertilizedGameDay(当天)}；
 * 成长结算侧（A 的 GrowthService）读 {@code crop.getFertilizerCount()}
 * 计算 +15%/次成长加成（或直接调 {@link #fertilizerGrowthRate}，二选一，
 * 避免双重加成）。在 A 补齐前，C 侧以 CropMemory 为唯一施肥数据源，
 * 功能不受影响。
 */
public interface FertilizerService {

    /** 每株生命周期施肥上限：3 次（规则文档 §二十六） */
    int MAX_FERTILIZE_PER_LIFE = 3;

    /** 每次消耗肥料数量：1（规则文档 §二十六） */
    int FERTILIZER_COST = 1;

    /**
     * 对指定作物执行一次施肥（校验通过后扣库存并落档）。
     *
     * <p>校验顺序：阶段 → 每日 1 次 → 生命 3 次 → 库存；全部通过才扣减
     * 库存并记录，任何失败都不产生变更（规则文档 §六十八 事务原子性精神）。
     *
     * @param crop     待施肥作物（提供生长阶段与类型）
     * @param memory   该作物的生命记忆（施肥次数/施肥日落档于此）
     * @param inventory 背包（扣 1 肥料）；null 视为无肥料
     * @param gameDay  当前游戏日（每日限制判定基准，决策 D14 口径）
     * @return 施肥结果码
     */
    FertilizeResult fertilize(Crop crop, CropMemory memory, Inventory inventory, long gameDay);

    /**
     * 施肥成长倍率（规则文档 §二十七 FertilizerBonus）：每施 1 次 +15%，
     * 即 {@code fertilizerCount × 0.15}（3 次封顶自然为 +45%，规则 §二十六）。
     *
     * <p>成长计算由 A 的 GrowthService 负责；本方法提供纯查询供其接入
     * （跨模块协作点，与 D07 装饰加成同模式）。品质侧无需接入——收获时
     * QualityService 已直接读取记忆中的施肥次数。
     *
     * @param memory 生命记忆
     * @return 成长倍率（0.0 ~ 0.45）
     */
    double fertilizerGrowthRate(CropMemory memory);
}
