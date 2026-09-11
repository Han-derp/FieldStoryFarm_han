package com.fieldstory.farm.service;

import com.fieldstory.farm.model.BuffSnapshot;
import com.fieldstory.farm.model.CropType;

/**
 * 装饰 Buff 唯一聚合入口。
 */
public interface BuffService {

    BuffSnapshot getSnapshot(int row, int column, CropType cropType);

    double getGrowthRate(int row, int column, CropType cropType);

    double getQualityScoreBonus(int row, int column, CropType cropType);

    double getPriceRate(int row, int column, CropType cropType);

    double getWitherResistanceRate(int row, int column, CropType cropType);

    double getWaterOperationMultiplier(int row, int column, CropType cropType);

    double getFertilizerOperationMultiplier(int row, int column, CropType cropType);

    double getSetGrowthBonus();

    double getSetPriceRate();

    double getSetLegendaryBonus();
}