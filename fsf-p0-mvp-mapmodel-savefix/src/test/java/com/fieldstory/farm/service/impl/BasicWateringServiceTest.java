package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WaterResult;
import com.fieldstory.farm.service.GameClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicWateringServiceTest {

    @Test
    void seedCannotBeWatered() {
        MutableClock clock = new MutableClock(LocalDateTime.of(2026, 1, 1, 8, 0));
        BasicWateringService service = new BasicWateringService(clock);
        Soil soil = plantedSoil(clock.now());

        assertEquals(WaterResult.STAGE_NOT_ALLOWED, service.water(soil));
    }

    @Test
    void sproutCanOnlyBeWateredOncePerGameDay() {
        MutableClock clock = new MutableClock(LocalDateTime.of(2026, 1, 1, 8, 0));
        BasicWateringService service = new BasicWateringService(clock);
        Soil soil = plantedSoil(clock.now());
        soil.getCrop().setGrowthStage(GrowthStage.SPROUT);

        assertEquals(WaterResult.SUCCESS, service.water(soil));
        assertEquals(WaterResult.ALREADY_WATERED_TODAY, service.water(soil));

        clock.set(LocalDateTime.of(2026, 1, 2, 8, 0));
        assertEquals(WaterResult.SUCCESS, service.water(soil));
    }

    @Test
    void sixthEffectiveWateringIsRejected() {
        MutableClock clock = new MutableClock(LocalDateTime.of(2026, 1, 6, 8, 0));
        BasicWateringService service = new BasicWateringService(clock);
        Soil soil = plantedSoil(clock.now());
        soil.getCrop().setGrowthStage(GrowthStage.GROWING);
        soil.getCrop().setManualWaterCount(5);
        soil.getCrop().setLastManualWaterGameDay(LocalDate.of(2026, 1, 5));

        assertEquals(WaterResult.MAX_WATER_COUNT_REACHED, service.water(soil));
        assertEquals(5, soil.getCrop().getManualWaterCount());
    }

    private Soil plantedSoil(LocalDateTime now) {
        Soil soil = new Soil(1, 0, 0);
        soil.setCrop(new Crop(CropType.CORN, now));
        return soil;
    }

    private static class MutableClock implements GameClock {
        private LocalDateTime now;

        MutableClock(LocalDateTime now) {
            this.now = now;
        }

        void set(LocalDateTime now) {
            this.now = now;
        }

        @Override
        public LocalDateTime now() {
            return now;
        }

        @Override
        public LocalDate currentGameDay() {
            return now.toLocalDate();
        }

        @Override
        public void advance(Duration realElapsed) {
            now = now.plus(realElapsed);
        }
    }
}
