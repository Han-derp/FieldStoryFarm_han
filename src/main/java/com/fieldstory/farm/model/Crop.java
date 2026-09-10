package com.fieldstory.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作物接口（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§7.3。实现类 {@code BasicCrop} 由 A 模块提供。
 *
 * <p>设计原则：Model 只描述"对象当前状态"，全部 setter 由 Service 或 E 模块切换使用，
 * 不含任何业务判断。
 */
public interface Crop {

    /**
     * 获取作物唯一标识。
     *
     * @return 作物 UUID
     */
    UUID getCropUuid();

    /**
     * 设置作物唯一标识。
     *
     * @param cropUuid 作物 UUID
     */
    void setCropUuid(UUID cropUuid);

    /**
     * 获取作物类型。
     *
     * @return 作物类型
     */
    CropType getCropType();

    /**
     * 设置作物类型。
     *
     * @param cropType 作物类型
     */
    void setCropType(CropType cropType);

    /**
     * 获取生长阶段。
     *
     * @return 生长阶段
     */
    GrowthStage getGrowthStage();

    /**
     * 设置生长阶段。
     *
     * @param growthStage 生长阶段
     */
    void setGrowthStage(GrowthStage growthStage);

    /**
     * 获取生长进度（0.0 ~ 100.0）。
     *
     * @return 生长进度
     */
    double getGrowthProgress();

    /**
     * 设置生长进度。
     *
     * @param growthProgress 生长进度
     */
    void setGrowthProgress(double growthProgress);

    /**
     * 获取种植时的世界时间。
     *
     * @return 种植世界时间
     */
    LocalDateTime getPlantWorldTime();

    /**
     * 设置种植时的世界时间。
     *
     * @param plantWorldTime 种植世界时间
     */
    void setPlantWorldTime(LocalDateTime plantWorldTime);

    /**
     * 获取手动浇水次数（0 ~ 5）。
     *
     * @return 手动浇水次数
     */
    int getManualWaterCount();

    /**
     * 设置手动浇水次数。
     *
     * @param manualWaterCount 手动浇水次数
     */
    void setManualWaterCount(int manualWaterCount);

    /**
     * 获取最近一次手动浇水的游戏日。
     *
     * @return 最近手动浇水游戏日
     */
    LocalDate getLastManualWaterGameDay();

    /**
     * 设置最近一次手动浇水的游戏日。
     *
     * @param lastManualWaterGameDay 最近手动浇水游戏日
     */
    void setLastManualWaterGameDay(LocalDate lastManualWaterGameDay);
}
