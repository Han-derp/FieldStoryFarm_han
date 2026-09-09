package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.service.BasicHarvestService;
import com.fieldstory.farm.service.EconomyService;
import com.fieldstory.farm.service.LandService;

public class BasicHarvestServiceImpl implements BasicHarvestService {
    private final EconomyService economyService;
    private final LandService landService;

    public BasicHarvestServiceImpl(EconomyService economyService, LandService landService) {
        this.economyService = economyService;
        this.landService = landService;
    }

    @Override
    public int harvest(Soil soil) {
        if (soil == null || soil.getCrop() == null) return 0;
        Crop crop = soil.getCrop();
        if (crop.getGrowthStage() != GrowthStage.MATURE) return 0;
        int price = economyService.calculateBaseSellPrice(crop.getCropType());
        economyService.addGold(price);
        landService.removeCropAndSetTilled(soil);
        return price;
    }
}
