package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.BuffSnapshot;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;

import java.util.Objects;

/**
 * BuffService 默认实现。
 *
 * <p>只遍历已放置装饰；P1 SetBonus = 0。
 */
public class BasicBuffService implements BuffService {

    /** 成长类装饰 Rate 上限（规则文档 §55）。 */
    private static final double MAX_DECORATION_GROWTH_RATE = 1.5;

    private final DecorationService decorationService;

    public BasicBuffService(DecorationService decorationService) {
        this.decorationService = Objects.requireNonNull(decorationService);
    }

    @Override
    public BuffSnapshot getSnapshot(int row, int column, CropType cropType) {
        double adjacent = calcAdjacentGrowth(row, column);
        double global = calcGlobalGrowth();
        double cropSpecific = calcCropSpecificGrowth(cropType);
        double growthRate = Math.min(
                MAX_DECORATION_GROWTH_RATE,
                1.0 + adjacent + global + cropSpecific
        );

        return new BuffSnapshot(
                growthRate,
                calcQualityScoreBonus(),
                calcPriceRate(),
                calcWitherResistanceRate(),
                calcWaterOperationMultiplier(),
                calcFertilizerOperationMultiplier(),
                getSetGrowthBonus(),
                getSetPriceRate(),
                getSetLegendaryBonus()
        );
    }

    @Override
    public double getGrowthRate(int row, int column, CropType cropType) {
        return getSnapshot(row, column, cropType).growthRate();
    }

    @Override
    public double getQualityScoreBonus(int row, int column, CropType cropType) {
        return calcQualityScoreBonus();
    }

    @Override
    public double getPriceRate(int row, int column, CropType cropType) {
        return calcPriceRate();
    }

    @Override
    public double getWitherResistanceRate(int row, int column, CropType cropType) {
        return calcWitherResistanceRate();
    }

    @Override
    public double getWaterOperationMultiplier(int row, int column, CropType cropType) {
        return calcWaterOperationMultiplier();
    }

    @Override
    public double getFertilizerOperationMultiplier(int row, int column, CropType cropType) {
        return calcFertilizerOperationMultiplier();
    }

    @Override
    public double getSetGrowthBonus() {
        return 0.0; // P1 套装未上线，P3 接入。
    }

    @Override
    public double getSetPriceRate() {
        return 0.0;
    }

    @Override
    public double getSetLegendaryBonus() {
        return 0.0;
    }

    // ==================== 内部聚合 ====================

    private double calcAdjacentGrowth(int row, int column) {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() != DecorationType.SUNFLOWER) {
                continue;
            }
            if (Math.abs(d.getRow() - row) <= 1 && Math.abs(d.getColumn() - column) <= 1) {
                total += 0.05;
            }
        }
        return Math.min(total, 0.15);
    }

    private double calcGlobalGrowth() {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.BIG_TREE) {
                total += 0.03;
            } else if (t == DecorationType.GOLDEN_FOUNTAIN) {
                total += 0.05;
            }
        }
        return total;
    }

    private double calcCropSpecificGrowth(CropType cropType) {
        if (cropType == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.WHEAT_WATCHER && cropType == CropType.WHEAT) {
                total += 0.10;
            } else if (t == DecorationType.CORN_HARVEST && cropType == CropType.CORN) {
                total += 0.10;
            } else if (t == DecorationType.CARROT_FIELD && cropType == CropType.CARROT) {
                total += 0.10;
            }
        }
        return total;
    }

    private double calcQualityScoreBonus() {
        double total = 0.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.RAINBOW_FOUNTAIN) {
                total += 10;
            } else if (t == DecorationType.HARVEST_GODDESS) {
                total += 5;
            }
        }
        return total;
    }

    private double calcPriceRate() {
        double total = 1.0;
        for (Decoration d : decorationService.getPlacedDecorations()) {
            DecorationType t = d.getDecorationType();
            if (t == DecorationType.GOLDEN_THRONE) {
                total += 0.10;
            } else if (t == DecorationType.HARVEST_GODDESS) {
                total += 0.15;
            }
        }
        return total;
    }

    private double calcWitherResistanceRate() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.STONE_LANTERN) {
                return 0.70;
            }
        }
        return 1.0;
    }

    private double calcWaterOperationMultiplier() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.ROSE_BED) {
                return 1.10;
            }
        }
        return 1.0;
    }

    private double calcFertilizerOperationMultiplier() {
        for (Decoration d : decorationService.getPlacedDecorations()) {
            if (d.getDecorationType() == DecorationType.SMALL_FOUNTAIN) {
                return 1.20;
            }
        }
        return 1.0;
    }
}