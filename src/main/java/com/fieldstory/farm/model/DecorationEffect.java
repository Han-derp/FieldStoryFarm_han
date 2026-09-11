package com.fieldstory.farm.model;

/**
 * 装饰效果值对象（不可变）。
 */
public record DecorationEffect(
        BuffType buffType,
        double value,
        double maxValue,
        CropType targetCropType
) {
}