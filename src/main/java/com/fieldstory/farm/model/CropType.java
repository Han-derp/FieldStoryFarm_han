package com.fieldstory.farm.model;

public enum CropType {
    WHEAT(2, 10, 50),
    CORN(3, 15, 70),
    CARROT(4, 20, 60);

    private final int baseGrowthDays;   // 规则文档十三
    private final int seedPrice;        // 规则文档十三
    private final int baseSellPrice;    // 规则文档十三

    CropType(int baseGrowthDays, int seedPrice, int baseSellPrice) {
        this.baseGrowthDays = baseGrowthDays;
        this.seedPrice = seedPrice;
        this.baseSellPrice = baseSellPrice;
    }

    public int getBaseGrowthDays() { return baseGrowthDays; }
    public int getSeedPrice() { return seedPrice; }
    public int getBaseSellPrice() { return baseSellPrice; }

    /** 基础日成长进度 = 100 ÷ 基础生长天数（验收规范二十三） */
    public double getBaseDailyProgress() { return 100.0 / baseGrowthDays; }
}
