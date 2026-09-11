package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import com.fieldstory.farm.model.economy.PurchaseResult;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.service.ShopService;
import com.fieldstory.farm.service.economy.EconomyService;

import java.util.Objects;

/**
 * ShopService 默认实现。
 *
 * <p>装饰购买业务事务：扣金币 + 装饰入库，一次完成。
 */
public class BasicShopService implements ShopService {

    private final EconomyService economyService;
    private final DecorationService decorationService;

    public BasicShopService(EconomyService economyService,
                            DecorationService decorationService) {
        this.economyService = Objects.requireNonNull(economyService);
        this.decorationService = Objects.requireNonNull(decorationService);
    }

    @Override
    public PurchaseResult buySeed(CropType type, int quantity) {
        return economyService.buySeed(type, quantity);
    }

    @Override
    public DecorationPurchaseResult buyDecoration(DecorationType type, int quantity) {
        Objects.requireNonNull(type, "decoration type cannot be null");
        if (quantity <= 0) {
            return DecorationPurchaseResult.INVALID_QUANTITY;
        }
        int totalPrice = type.getPrice() * quantity;
        if (!economyService.canAfford(totalPrice)) {
            return DecorationPurchaseResult.INSUFFICIENT_GOLD;
        }
        economyService.spendGold(totalPrice);
        decorationService.addPurchasedDecoration(type, quantity);
        return DecorationPurchaseResult.SUCCESS;
    }

    @Override
    public boolean canAffordDecoration(DecorationType type, int quantity) {
        if (type == null || quantity <= 0) {
            return false;
        }
        return economyService.canAfford(type.getPrice() * quantity);
    }

    @Override
    public int getGold() {
        return economyService.getGold();
    }
}