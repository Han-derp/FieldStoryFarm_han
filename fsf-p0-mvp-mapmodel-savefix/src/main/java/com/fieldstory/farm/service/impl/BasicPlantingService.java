package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.service.EconomyService;
import com.fieldstory.farm.service.GameClock;
import com.fieldstory.farm.service.PlantingService;

public class BasicPlantingService implements PlantingService {
    private final EconomyService economyService;
    private final GameClock clock;

    public BasicPlantingService(EconomyService economyService, GameClock clock) {
        this.economyService = economyService;
        this.clock = clock;
    }

    @Override
    public boolean plant(Soil soil, CropType type) {
        if (soil == null || type == null || soil.getState() != SoilState.TILLED) return false;
        if (!economyService.consumeSeed(type, 1)) return false;
        soil.setCrop(new Crop(type, clock.now()));
        soil.setState(SoilState.PLANTED);
        return true;
    }
}
