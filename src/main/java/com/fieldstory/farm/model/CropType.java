package com.fieldstory.farm.model;

/**
 * 作物类型（规则文档 §十三；验收规范 §十七）。
 *
 * <p>P0 三种基础作物数值的唯一硬编码来源（决策 D12），
 * 字段结构与 crop-config.json 对齐。
 *
 * <p>baseScore 为 P1 品质系统预留（规则文档 §十四、§三十四），
 * P0 品质系统未启用，本字段不参与任何计算。
 */
public enum CropType {

    /** 小麦：耐旱、成长最快（规则文档 §14.1） */
    WHEAT("小麦", 2, 10, 50, 50),

    /** 玉米：均衡、基础售价最高（规则文档 §14.2） */
    CORN("玉米", 3, 15, 70, 50),

    /** 胡萝卜：品质潜力，基础品质分 55（规则文档 §14.3） */
    CARROT("胡萝卜", 4, 20, 60, 55);

    /** 显示名称 */
    private final String displayName;

    /** 基础成长天数（游戏日），规则文档 §十三 */
    private final int baseGrowthDays;

    /** 种子价格（金币），规则文档 §十三 */
    private final int seedPrice;

    /** 基础售价（金币），规则文档 §十三 */
    private final int basePrice;

    /** 基础品质分：P1 品质系统启用前不使用，仅为与 crop-config.json 字段结构对齐预留（规则文档 §三十四） */
    private final int baseScore;

    CropType(String displayName, int baseGrowthDays, int seedPrice, int basePrice, int baseScore) {
        this.displayName = displayName;
        this.baseGrowthDays = baseGrowthDays;
        this.seedPrice = seedPrice;
        this.basePrice = basePrice;
        this.baseScore = baseScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseGrowthDays() {
        return baseGrowthDays;
    }

    public int getSeedPrice() {
        return seedPrice;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public int getBaseScore() {
        return baseScore;
    }
}
