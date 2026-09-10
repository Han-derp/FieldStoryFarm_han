package com.fieldstory.farm.model.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.GrowthStage;

import java.util.UUID;

/**
 * {@link Crop} 基础实现：纯状态容器，只提供字段读写，不含任何计算与业务规则。
 */
public class BasicCrop implements Crop {

    /** 作物唯一标识 */
    private UUID cropUuid;

    /** 作物类型 */
    private CropType cropType;

    /** 成长阶段 */
    private GrowthStage growthStage;

    /** 成长进度，内部口径 0~100 */
    private double growthProgress;

    /** 播种时刻世界时间（游戏小时，来自 GameClock.getWorldTime） */
    private long plantWorldTime;

    /** 主动浇水累计次数 */
    private int manualWaterCount;

    /** 最近一次主动浇水的游戏日（游戏日，来自 GameClock.getGameDay） */
    private long lastManualWaterGameDay;

    @Override
    public UUID getCropUuid() {
        return cropUuid;
    }

    @Override
    public void setCropUuid(UUID cropUuid) {
        this.cropUuid = cropUuid;
    }

    @Override
    public CropType getCropType() {
        return cropType;
    }

    @Override
    public void setCropType(CropType cropType) {
        this.cropType = cropType;
    }

    @Override
    public GrowthStage getGrowthStage() {
        return growthStage;
    }

    @Override
    public void setGrowthStage(GrowthStage growthStage) {
        this.growthStage = growthStage;
    }

    @Override
    public double getGrowthProgress() {
        return growthProgress;
    }

    @Override
    public void setGrowthProgress(double growthProgress) {
        this.growthProgress = growthProgress;
    }

    @Override
    public long getPlantWorldTime() {
        return plantWorldTime;
    }

    @Override
    public void setPlantWorldTime(long plantWorldTime) {
        this.plantWorldTime = plantWorldTime;
    }

    @Override
    public int getManualWaterCount() {
        return manualWaterCount;
    }

    @Override
    public void setManualWaterCount(int manualWaterCount) {
        this.manualWaterCount = manualWaterCount;
    }

    @Override
    public long getLastManualWaterGameDay() {
        return lastManualWaterGameDay;
    }

    @Override
    public void setLastManualWaterGameDay(long lastManualWaterGameDay) {
        this.lastManualWaterGameDay = lastManualWaterGameDay;
    }
}
