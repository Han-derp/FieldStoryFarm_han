package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.service.GameClock;
import com.fieldstory.farm.service.GrowthService;

import java.time.Duration;
import java.time.LocalDateTime;

public class BasicGrowthService implements GrowthService {
    private final GameClock clock;

    public BasicGrowthService(GameClock clock) {
        this.clock = clock;
    }

    @Override
    public void update(Farm farm) {
        if (farm == null) return;

        LocalDateTime now = clock.now();
        for (Soil soil : farm.allSoils()) {
            Crop crop = soil.getCrop();
            if (crop == null
                    || crop.getGrowthStage() == GrowthStage.MATURE
                    || crop.getGrowthStage() == GrowthStage.WITHERED) {
                continue;
            }

            LocalDateTime last = crop.getLastGrowthWorldTime();
            if (last == null) last = crop.getPlantWorldTime();
            if (last == null || !now.isAfter(last)) continue;

            // 使用纳秒避免高频刷新时整秒截断。
            double elapsedGameDays = Duration.between(last, now).toNanos()
                    / (86_400.0 * 1_000_000_000.0);

            double baseDailyProgress = 100.0 / crop.getCropType().getBaseGrowthDays();
            double operationRate = operationRate(crop);
            double delta = baseDailyProgress * elapsedGameDays * operationRate;

            double progress = Math.min(100.0, crop.getGrowthProgress() + delta);
            crop.setGrowthProgress(progress);
            crop.setLastGrowthWorldTime(now);
            crop.setGrowthStage(stageFor(progress));
        }
    }

    @Override
    public long estimateRemainingGameHours(Crop crop) {
        if (crop == null || crop.getGrowthStage() == GrowthStage.MATURE) return 0;

        double remainingProgress = Math.max(0.0, 100.0 - crop.getGrowthProgress());
        double baseDailyProgress = 100.0 / crop.getCropType().getBaseGrowthDays();
        double progressPerGameDay = baseDailyProgress * operationRate(crop);
        if (progressPerGameDay <= 0.0) return Long.MAX_VALUE;

        double remainingDays = remainingProgress / progressPerGameDay;
        return (long) Math.ceil(remainingDays * 24.0);
    }

    private double operationRate(Crop crop) {
        double waterBonus = Math.min(crop.getManualWaterCount() * 0.05, 0.20);
        return 1.0 + waterBonus;
    }

    private GrowthStage stageFor(double progress) {
        if (progress >= 100.0) return GrowthStage.MATURE;
        if (progress >= 50.0) return GrowthStage.GROWING;
        if (progress >= 20.0) return GrowthStage.SPROUT;
        return GrowthStage.SEED;
    }
}
