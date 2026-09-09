package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WaterResult;
import com.fieldstory.farm.service.GameClock;
import com.fieldstory.farm.service.WateringService;

import java.time.LocalDate;

public class BasicWateringService implements WateringService {
    private static final int MAX_MANUAL_WATER_COUNT = 5;

    private final GameClock clock;

    public BasicWateringService(GameClock clock) {
        this.clock = clock;
    }

    @Override
    public WaterResult water(Soil soil) {
        if (soil == null || soil.getCrop() == null) {
            return WaterResult.NO_CROP;
        }

        Crop crop = soil.getCrop();
        GrowthStage stage = crop.getGrowthStage();

        // P0只允许 SPROUT / GROWING / MATURE 主动浇水。
        if (stage != GrowthStage.SPROUT
                && stage != GrowthStage.GROWING
                && stage != GrowthStage.MATURE) {
            return WaterResult.STAGE_NOT_ALLOWED;
        }

        // 单株生命周期最多记录5次有效主动浇水。
        if (crop.getManualWaterCount() >= MAX_MANUAL_WATER_COUNT) {
            return WaterResult.MAX_WATER_COUNT_REACHED;
        }

        LocalDate today = clock.currentGameDay();
        if (today.equals(crop.getLastManualWaterGameDay())) {
            return WaterResult.ALREADY_WATERED_TODAY;
        }

        crop.setManualWaterCount(crop.getManualWaterCount() + 1);
        crop.setLastManualWaterGameDay(today);
        return WaterResult.SUCCESS;
    }
}
