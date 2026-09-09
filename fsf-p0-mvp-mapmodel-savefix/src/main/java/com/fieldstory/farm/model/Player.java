package com.fieldstory.farm.model;

import java.util.EnumMap;
import java.util.Map;

public class Player {
    public static final int INITIAL_GOLD = 500;

    private String name = "Player";
    private int gold = INITIAL_GOLD;
    private Map<CropType, Integer> seedInventory = new EnumMap<>(CropType.class);

    public Player() {
        for (CropType type : CropType.values()) {
            seedInventory.put(type, 0);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public Map<CropType, Integer> getSeedInventory() { return seedInventory; }
    public void setSeedInventory(Map<CropType, Integer> seedInventory) {
        this.seedInventory = new EnumMap<>(CropType.class);
        if (seedInventory != null) this.seedInventory.putAll(seedInventory);
        for (CropType type : CropType.values()) this.seedInventory.putIfAbsent(type, 0);
    }
}
