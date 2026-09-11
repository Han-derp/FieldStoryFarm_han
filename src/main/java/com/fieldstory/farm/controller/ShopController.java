package com.fieldstory.farm.controller;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import com.fieldstory.farm.model.economy.PurchaseResult;
import com.fieldstory.farm.service.ShopService;

import java.util.Objects;

/**
 * B 模块 P1 商店控制器。
 *
 * <p>新增回调：装饰购买成功后通知装配层刷新装饰面板与地图装饰层。
 */
public class ShopController {

    private final ShopService shopService;

    /** 装饰购买成功回调（默认空）。 */
    private Runnable onDecorationPurchased = () -> {};

    public ShopController(ShopService shopService) {
        this.shopService = Objects.requireNonNull(shopService);
    }

    public void setOnDecorationPurchased(Runnable callback) {
        if (callback != null) {
            this.onDecorationPurchased = callback;
        }
    }

    public PurchaseResult buySeed(CropType type, int quantity) {
        return shopService.buySeed(type, quantity);
    }

    public DecorationPurchaseResult buyDecoration(DecorationType type, int quantity) {
        DecorationPurchaseResult result = shopService.buyDecoration(type, quantity);
        if (result == DecorationPurchaseResult.SUCCESS) {
            onDecorationPurchased.run();
        }
        return result;
    }

    public boolean canAffordDecoration(DecorationType type, int quantity) {
        return shopService.canAffordDecoration(type, quantity);
    }

    public int getGold() {
        return shopService.getGold();
    }

    public static String messageFor(PurchaseResult result, CropType type) {
        switch (result) {
            case SUCCESS:
                return type == null ? "购买成功" : type.getDisplayName() + "种子购买成功";
            case INSUFFICIENT_GOLD:
                return "金币不足";
            case INVALID_QUANTITY:
                return "购买数量无效";
            default:
                return "购买失败";
        }
    }

    public static String messageFor(DecorationPurchaseResult result, DecorationType type) {
        switch (result) {
            case SUCCESS:
                return type == null ? "购买成功" : type.getDisplayName() + "购买成功";
            case INSUFFICIENT_GOLD:
                return "金币不足";
            case INVALID_QUANTITY:
                return "购买数量无效";
            default:
                return "购买失败";
        }
    }
}