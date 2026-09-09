package com.fieldstory.farm.service;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.PurchaseResult;

public interface EconomyService {
    int getGold();
    boolean canAfford(int amount);
    void spendGold(int amount);
    void addGold(int amount);
    PurchaseResult buySeed(CropType type, int quantity);
    int getSeedCount(CropType type);
    boolean hasSeed(CropType type, int quantity);
    boolean consumeSeed(CropType type, int quantity);
    int calculateBaseSellPrice(CropType type);
}
