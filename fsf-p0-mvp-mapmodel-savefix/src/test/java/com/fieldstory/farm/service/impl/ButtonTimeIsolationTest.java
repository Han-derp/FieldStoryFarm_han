package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 验证P0业务按钮对应Service不会推进GameClock。 */
class ButtonTimeIsolationTest {

    @Test
    void economyLandPlantWaterAndHarvestNeverAdvanceClock() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 8, 0);
        DemoGameClock clock = new DemoGameClock(start);
        Player player = new Player();
        BasicEconomyService economy = new BasicEconomyService(player);
        BasicLandService land = new BasicLandService(economy);
        BasicPlantingService planting = new BasicPlantingService(economy, clock);
        BasicWateringService watering = new BasicWateringService(clock);
        BasicHarvestServiceImpl harvest = new BasicHarvestServiceImpl(economy, land);
        Soil soil = new Soil(1, 0, 0);

        assertTrue(land.reclaim(soil));
        assertEquals(start, clock.now());

        assertEquals(PurchaseResult.SUCCESS, economy.buySeed(CropType.WHEAT, 1));
        assertEquals(start, clock.now());

        assertTrue(planting.plant(soil, CropType.WHEAT));
        assertEquals(start, clock.now());

        soil.getCrop().setGrowthStage(GrowthStage.SPROUT);
        assertEquals(WaterResult.SUCCESS, watering.water(soil));
        assertEquals(start, clock.now());

        soil.getCrop().setGrowthStage(GrowthStage.MATURE);
        harvest.harvest(soil);
        assertEquals(start, clock.now());
    }
}
