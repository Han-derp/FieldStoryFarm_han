package com.fieldstory.farm.model;

/**
 * 作物类型枚举（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§6.4、决策 D12：数值唯一来源，禁止硬编码数值。
 */
public enum CropType {

    /** 小麦。 */
    WHEAT(2, 10, 50),

    /** 玉米。 */
    CORN(3, 15, 70),

    /** 胡萝卜。 */
    CARROT(4, 20, 60);

    private final int baseGrowthDays;
    private final int seedPrice;
    private final int baseSellPrice;

    CropType(int baseGrowthDays, int seedPrice, int baseSellPrice) {
        this.baseGrowthDays = baseGrowthDays;
        this.seedPrice = seedPrice;
        this.baseSellPrice = baseSellPrice;
    }

    /**
     * 获取基础生长天数。
     *
     * @return 基础生长天数
     */
    public int getBaseGrowthDays() {
        return baseGrowthDays;
    }

    /**
     * 获取种子价格。
     *
     * @return 种子价格
     */
    public int getSeedPrice() {
        return seedPrice;
    }

    /**
     * 获取基础售价。
     *
     * @return 基础售价
     */
    public int getBaseSellPrice() {
        return baseSellPrice;
    }

    /**
     * 获取每日基础生长进度（= 100 / 基础生长天数，验收规范 §十六）。
     *
     * @return 每日基础生长进度
     */
    public double getBaseDailyProgress() {
        return 100.0 / baseGrowthDays;
    }
}
