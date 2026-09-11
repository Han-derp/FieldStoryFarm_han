package com.fieldstory.farm.model;

/**
 * 某位置/某作物的 Buff 聚合快照。
 *
 * <p>P1 套装加成全部为 0，字段预留以兼容 P3。
 */
public record BuffSnapshot(
        double growthRate,
        double qualityScoreBonus,
        double priceRate,
        double witherResistanceRate,
        double waterOperationMultiplier,
        double fertilizerOperationMultiplier,
        double setGrowthBonus,
        double setPriceRate,
        double setLegendaryBonus
) {
    /** P1 中性快照（无装饰时使用，便于测试）。 */
    public static BuffSnapshot neutral() {
        return new BuffSnapshot(1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0);
    }
}