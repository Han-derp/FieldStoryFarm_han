package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;

public interface GrowthService {
    void update(Farm farm);

    /**
     * 按当前P0成长倍率估算距离成熟还需要多少游戏小时。
     */
    long estimateRemainingGameHours(Crop crop);
}
