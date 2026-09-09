package com.fieldstory.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Crop {
    private UUID cropUuid;
    private CropType cropType;
    private GrowthStage growthStage;
    private double growthProgress;
    private LocalDateTime plantWorldTime;
    private LocalDateTime lastGrowthWorldTime;
    private int manualWaterCount;
    private LocalDate lastManualWaterGameDay;

    public Crop() {}

    public Crop(CropType type, LocalDateTime worldTime) {
        this.cropUuid = UUID.randomUUID();
        this.cropType = type;
        this.growthStage = GrowthStage.SEED;
        this.growthProgress = 0.0;
        this.plantWorldTime = worldTime;
        this.lastGrowthWorldTime = worldTime;
    }

    public UUID getCropUuid() { return cropUuid; }
    public void setCropUuid(UUID cropUuid) { this.cropUuid = cropUuid; }
    public CropType getCropType() { return cropType; }
    public void setCropType(CropType cropType) { this.cropType = cropType; }
    public GrowthStage getGrowthStage() { return growthStage; }
    public void setGrowthStage(GrowthStage growthStage) { this.growthStage = growthStage; }
    public double getGrowthProgress() { return growthProgress; }
    public void setGrowthProgress(double growthProgress) { this.growthProgress = growthProgress; }
    public LocalDateTime getPlantWorldTime() { return plantWorldTime; }
    public void setPlantWorldTime(LocalDateTime plantWorldTime) { this.plantWorldTime = plantWorldTime; }
    public LocalDateTime getLastGrowthWorldTime() { return lastGrowthWorldTime; }
    public void setLastGrowthWorldTime(LocalDateTime lastGrowthWorldTime) { this.lastGrowthWorldTime = lastGrowthWorldTime; }
    public int getManualWaterCount() { return manualWaterCount; }
    public void setManualWaterCount(int manualWaterCount) { this.manualWaterCount = manualWaterCount; }
    public LocalDate getLastManualWaterGameDay() { return lastManualWaterGameDay; }
    public void setLastManualWaterGameDay(LocalDate lastManualWaterGameDay) { this.lastManualWaterGameDay = lastManualWaterGameDay; }
}
