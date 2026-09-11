package com.fieldstory.farm.service;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import com.fieldstory.farm.model.economy.PurchaseResult;

/**
 * B 模块 P1 完整商店服务。
 *
 * <p>种子购买委托 P0 冻结的 {@link com.fieldstory.farm.service.economy.EconomyService#buySeed}。
 */
public interface ShopService {

    PurchaseResult buySeed(CropType type, int quantity);

    DecorationPurchaseResult buyDecoration(DecorationType type, int quantity);

    boolean canAffordDecoration(DecorationType type, int quantity);

    int getGold();
}