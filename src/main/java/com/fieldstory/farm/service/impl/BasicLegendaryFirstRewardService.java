package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.service.LegendaryFirstRewardService;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * {@link LegendaryFirstRewardService} 基础实现（C 模块 品质与传说域，P2）。
 *
 * <p>内存登记表：已领取首次奖励的传说作物类型集合（规则文档 §六十七：
 * 三种传说分别只能领取一次 +500 金币）。P2 使用内存态；持久化由 E 存档
 * 统一处理（与 CropMemory 同模式）。
 */
public class BasicLegendaryFirstRewardService implements LegendaryFirstRewardService {

    /** 已领取首次奖励的传说作物类型 */
    private final Set<CropType> claimed = EnumSet.noneOf(CropType.class);

    @Override
    public int claimFirstReward(CropType cropType) {
        Objects.requireNonNull(cropType, "作物类型不能为空");
        if (!claimed.add(cropType)) {
            return 0; // 已领过：同一传说只领一次（规则文档 §六十七）
        }
        return FIRST_REWARD_GOLD;
    }

    @Override
    public boolean hasClaimed(CropType cropType) {
        Objects.requireNonNull(cropType, "作物类型不能为空");
        return claimed.contains(cropType);
    }
}
