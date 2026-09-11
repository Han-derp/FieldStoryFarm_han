package com.fieldstory.farm.model;

/**
 * 具体 Buff 类型，供 BuffService 解释与聚合。
 */
public enum BuffType {

    ADJACENT_GROWTH(BuffCategory.GROWTH),
    GLOBAL_GROWTH(BuffCategory.GROWTH),
    CROP_SPECIFIC_GROWTH(BuffCategory.GROWTH),
    QUALITY_SCORE(BuffCategory.QUALITY),
    PRICE_RATE(BuffCategory.PRICE),
    WITHER_RESISTANCE(BuffCategory.WITHER_RESISTANCE),
    WATER_OPERATION_MULTIPLIER(BuffCategory.OPERATION_MODIFIER),
    FERTILIZER_OPERATION_MULTIPLIER(BuffCategory.OPERATION_MODIFIER);

    private final BuffCategory category;

    BuffType(BuffCategory category) {
        this.category = category;
    }

    public BuffCategory getCategory() {
        return category;
    }
}