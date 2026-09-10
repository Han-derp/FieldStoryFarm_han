package com.fieldstory.farm.model;

import java.util.UUID;

/**
 * 作物模型接口（验收规范 §二十；规则文档 §十七、§二十五、§六十九）。
 *
 * <p>只保存状态，不含任何计算与业务规则（脚手架 §七.1 Model 原则）。
 * 全部 setter 供 Service 与存档反序列化使用。
 */
public interface Crop {

    /** 作物唯一标识（规则文档 §六十九） */
    UUID getCropUuid();

    void setCropUuid(UUID cropUuid);

    CropType getCropType();

    void setCropType(CropType cropType);

    GrowthStage getGrowthStage();

    void setGrowthStage(GrowthStage growthStage);

    /** 成长进度，内部口径 0~100（规则文档 §十七） */
    double getGrowthProgress();

    void setGrowthProgress(double growthProgress);

    /** 播种时刻世界时间（游戏小时，来自 GameClock.getWorldTime） */
    long getPlantWorldTime();

    void setPlantWorldTime(long plantWorldTime);

    /** 主动浇水累计次数（规则文档 §二十四、§二十五） */
    int getManualWaterCount();

    void setManualWaterCount(int manualWaterCount);

    /** 最近一次主动浇水的游戏日（游戏日，来自 GameClock.getGameDay） */
    long getLastManualWaterGameDay();

    void setLastManualWaterGameDay(long lastManualWaterGameDay);
}
