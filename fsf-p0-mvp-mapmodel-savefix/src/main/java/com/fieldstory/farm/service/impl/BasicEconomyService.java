package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.PurchaseResult;
import com.fieldstory.farm.service.EconomyService;

public class BasicEconomyService implements EconomyService {
    private final Player player;

    public BasicEconomyService(Player player) {
        this.player = player;
    }

    @Override public int getGold() { return player.getGold(); }

    @Override
    public boolean canAfford(int amount) {
        return amount >= 0 && player.getGold() >= amount;
    }

    @Override
    public void spendGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount < 0");
        if (!canAfford(amount)) throw new IllegalStateException("insufficient gold");
        player.setGold(player.getGold() - amount);
    }

    @Override
    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount < 0");
        player.setGold(player.getGold() + amount);
    }

    @Override
    public PurchaseResult buySeed(CropType type, int quantity) {
        if (quantity <= 0) return PurchaseResult.INVALID_QUANTITY;
        int total = type.getSeedPrice() * quantity;
        if (!canAfford(total)) return PurchaseResult.INSUFFICIENT_GOLD;
        spendGold(total);
        player.getSeedInventory().merge(type, quantity, Integer::sum);
        return PurchaseResult.SUCCESS;
    }

    @Override
    public int getSeedCount(CropType type) {
        return player.getSeedInventory().getOrDefault(type, 0);
    }

    @Override
    public boolean hasSeed(CropType type, int quantity) {
        return quantity > 0 && getSeedCount(type) >= quantity;
    }

    @Override
    public boolean consumeSeed(CropType type, int quantity) {
        if (!hasSeed(type, quantity)) return false;
        player.getSeedInventory().put(type, getSeedCount(type) - quantity);
        return true;
    }

    @Override
    public int calculateBaseSellPrice(CropType type) {
        return type.getBaseSellPrice();
    }
}
