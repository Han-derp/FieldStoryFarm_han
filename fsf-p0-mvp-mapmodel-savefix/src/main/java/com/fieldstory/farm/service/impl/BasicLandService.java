package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.service.EconomyService;
import com.fieldstory.farm.service.LandService;

public class BasicLandService implements LandService {
    public static final int RECLAIM_COST = 5;
    private final EconomyService economyService;

    public BasicLandService(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean reclaim(Soil soil) {
        if (soil == null || soil.getState() != SoilState.EMPTY) return false;
        if (!economyService.canAfford(RECLAIM_COST)) return false;
        economyService.spendGold(RECLAIM_COST);
        soil.setState(SoilState.TILLED);
        return true;
    }

    @Override
    public void removeCropAndSetTilled(Soil soil) {
        if (soil == null) return;
        soil.setCrop(null);
        soil.setState(SoilState.TILLED);
    }
}
