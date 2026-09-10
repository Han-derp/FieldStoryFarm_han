package com.fieldstory.farm.factory;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.impl.BasicCrop;

import java.util.UUID;

/**
 * 作物工厂（脚手架 §七.6 factory 包职责：对象创建）。
 *
 * <p>创建一株全新作物的初始状态（验收规范 §二十 字段清单）：
 * 随机 cropUuid、growthStage=SEED、growthProgress=0、
 * manualWaterCount=0、lastManualWaterGameDay=0。
 */
public final class CropFactory {

    private CropFactory() {
        // 工厂不拆分、只提供静态创建，禁止实例化
    }

    /**
     * 创建初始作物。
     *
     * @param type            作物类型
     * @param plantWorldTime  播种时刻世界时间（游戏小时，来自 GameClock.getWorldTime）
     * @return 初始状态为 SEED 的新作物
     */
    public static Crop create(CropType type, long plantWorldTime) {
        BasicCrop crop = new BasicCrop();
        crop.setCropUuid(UUID.randomUUID());
        crop.setCropType(type);
        crop.setGrowthStage(GrowthStage.SEED);
        crop.setGrowthProgress(0.0);
        crop.setPlantWorldTime(plantWorldTime);
        crop.setManualWaterCount(0);
        crop.setLastManualWaterGameDay(0);
        return crop;
    }
}
