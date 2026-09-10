package com.fieldstory.farm.service.economy.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.economy.PurchaseResult;
import com.fieldstory.farm.service.economy.EconomyService;

import java.util.Objects;

/**
 * B模块P0经济服务默认实现。
 *
 * 负责：
 * 1. 金币查询、收入与支出
 * 2. 种子购买
 * 3. 种子库存查询与消耗
 * 4. P0基础售价访问
 */
public class EconomyServiceImpl implements EconomyService {

    private final Player player;

    /**
     * 创建经济服务。
     *
     * @param player 当前游戏会话中的Player
     */
    public EconomyServiceImpl(Player player) {
        this.player = Objects.requireNonNull(
                player,
                "player cannot be null"
        );
    }

    @Override
    public int getGold() {
        return player.getGold();
    }

    @Override
    public boolean canAfford(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative"
            );
        }

        return player.getGold() >= amount;
    }

    @Override
    public void spendGold(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative"
            );
        }

        if (!canAfford(amount)) {
            throw new IllegalStateException(
                    "insufficient gold"
            );
        }

        player.setGold(
                player.getGold() - amount
        );
    }

    @Override
    public void addGold(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative"
            );
        }

        player.setGold(
                player.getGold() + amount
        );
    }

    @Override
    public PurchaseResult buySeed(
            CropType type,
            int quantity) {

        Objects.requireNonNull(
                type,
                "crop type cannot be null"
        );

        if (quantity <= 0) {
            return PurchaseResult.INVALID_QUANTITY;
        }

        /*
         * CropType是价格唯一数据源。
         * B模块禁止自己维护switch价格表。
         */
        int unitPrice = type.getSeedPrice();

        int totalPrice =
                unitPrice * quantity;

        if (!canAfford(totalPrice)) {
            return PurchaseResult.INSUFFICIENT_GOLD;
        }

        int newSeedCount =
                getSeedCount(type) + quantity;

        /*
         * 一次购买业务：
         * 扣金币 + 增加库存。
         */
        spendGold(totalPrice);

        player.getSeedInventory()
                .put(type, newSeedCount);

        return PurchaseResult.SUCCESS;
    }

    @Override
    public int getSeedCount(CropType type) {

        Objects.requireNonNull(
                type,
                "crop type cannot be null"
        );

        return player
                .getSeedInventory()
                .getOrDefault(type, 0);
    }

    @Override
    public boolean hasSeed(
            CropType type,
            int quantity) {

        Objects.requireNonNull(
                type,
                "crop type cannot be null"
        );

        if (quantity <= 0) {
            return false;
        }

        return getSeedCount(type) >= quantity;
    }

    @Override
    public boolean consumeSeed(
            CropType type,
            int quantity) {

        Objects.requireNonNull(
                type,
                "crop type cannot be null"
        );

        if (quantity <= 0) {
            return false;
        }

        if (!hasSeed(type, quantity)) {
            return false;
        }

        int remaining =
                getSeedCount(type) - quantity;

        player.getSeedInventory()
                .put(type, remaining);

        return true;
    }

    @Override
    public int calculateBaseSellPrice(
            CropType type) {

        Objects.requireNonNull(
                type,
                "crop type cannot be null"
        );

        /*
         * CropType是基础售价唯一数据源。
         */
        return type.getBaseSellPrice();
    }
}