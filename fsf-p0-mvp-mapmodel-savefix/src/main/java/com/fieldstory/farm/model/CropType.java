package com.fieldstory.farm.model;

public enum CropType {
    WHEAT(2, 10, 50),
    CORN(3, 15, 70),
    CARROT(4, 20, 60);

    private final int baseGrowthDays;
    private final int seedPrice;
    private final int baseSellPrice;

    CropType(int baseGrowthDays, int seedPrice, int baseSellPrice) {
        this.baseGrowthDays = baseGrowthDays;
        this.seedPrice = seedPrice;
        this.baseSellPrice = baseSellPrice;
    }

    public int getBaseGrowthDays() { return baseGrowthDays; }
    public int getSeedPrice() { return seedPrice; }
    public int getBaseSellPrice() { return baseSellPrice; }
}
