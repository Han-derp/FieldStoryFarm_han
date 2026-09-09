package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.service.GameClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicGrowthServiceTest {

    @Test
    void cornReachesSproutAtTwentyPercent() {
        MutableClock clock = new MutableClock(LocalDateTime.of(2026, 1, 1, 0, 0));
        BasicGrowthService service = new BasicGrowthService(clock);
        Farm farm = oneCropFarm(CropType.CORN, clock.now());

        // 玉米每日约33.333%，14.4游戏小时约为20%。
        clock.set(LocalDateTime.of(2026, 1, 1, 14, 24));
        service.update(farm);

        Crop crop = farm.getSoil(0, 0).getCrop();
        assertTrue(crop.getGrowthProgress() >= 19.99);
        assertEquals(GrowthStage.SPROUT, crop.getGrowthStage());
    }

    @Test
    void matureProgressIsClampedAtOneHundred() {
        MutableClock clock = new MutableClock(LocalDateTime.of(2026, 1, 1, 0, 0));
        BasicGrowthService service = new BasicGrowthService(clock);
        Farm farm = oneCropFarm(CropType.WHEAT, clock.now());

        clock.set(LocalDateTime.of(2026, 1, 10, 0, 0));
        service.update(farm);

        Crop crop = farm.getSoil(0, 0).getCrop();
        assertEquals(100.0, crop.getGrowthProgress(), 0.0001);
        assertEquals(GrowthStage.MATURE, crop.getGrowthStage());
    }

    private Farm oneCropFarm(CropType type, LocalDateTime now) {
        Farm farm = Farm.createP0Farm();
        Soil soil = farm.getSoil(0, 0);
        soil.setState(SoilState.PLANTED);
        soil.setCrop(new Crop(type, now));
        return farm;
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
