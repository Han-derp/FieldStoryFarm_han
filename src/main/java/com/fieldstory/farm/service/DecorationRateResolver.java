package com.fieldstory.farm.service;

import com.fieldstory.farm.model.CropType;

/**
 * A/B P2 跨模块逐作物装饰成长倍率解析器。
 *
 * <p>装饰属于 B 模块领域，A 模块世界引擎只声明消费入口，不实现装饰规则。
 * B 通过 {@code BuffService.getGrowthRate(row, column, cropType)} 提供实际倍率。
 */
@FunctionalInterface
public interface DecorationRateResolver {

    /**
     * 返回指定地块/作物的完整 DecorationRate。
     *
     * <p>规则口径：1 + AdjacentBonus + GlobalBonus + CropSpecificBonus，
     * 当前 P2 不含 SetBonus，成长类装饰倍率上限由 B 的 BuffService 保证为 1.5。
     */
    double decorationRate(int row, int column, CropType cropType);
}
