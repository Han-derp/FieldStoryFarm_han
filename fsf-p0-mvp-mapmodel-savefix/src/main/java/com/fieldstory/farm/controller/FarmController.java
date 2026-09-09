package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.PurchaseResult;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WaterResult;

/**
 * Controller只接收用户操作、调用Service并返回UI消息。
 * 严禁在Controller中推进GameClock。
 */
public class FarmController {
    private final GameManager game;

    public FarmController(GameManager game) {
        this.game = game;
    }

    public String reclaim(Soil soil) {
        boolean ok = game.getLandService().reclaim(soil);
        if (ok) game.save();
        return ok ? "开垦成功" : "开垦失败：土地状态不允许或金币不足";
    }

    public String buySeed(CropType type) {
        PurchaseResult result = game.getEconomyService().buySeed(type, 1);
        if (result == PurchaseResult.SUCCESS) game.save();

        return switch (result) {
            case SUCCESS -> "购买成功：" + type;
            case INSUFFICIENT_GOLD -> "购买失败：金币不足";
            case INVALID_QUANTITY -> "购买失败：数量非法";
        };
    }

    public String plant(Soil soil, CropType type) {
        boolean ok = game.getPlantingService().plant(soil, type);
        if (ok) game.save();
        return ok ? "播种成功：" + type : "播种失败：需要TILLED土地且库存充足";
    }

    public String water(Soil soil) {
        // 当前阶段已经由统一世界循环提交；按钮不再主动同步/推进时间。
        WaterResult result = game.getWateringService().water(soil);
        if (result == WaterResult.SUCCESS) game.save();

        return switch (result) {
            case SUCCESS -> "浇水成功";
            case NO_CROP -> "浇水失败：当前土地没有作物";
            case STAGE_NOT_ALLOWED -> "浇水失败：SEED阶段不可主动浇水，请等待进入SPROUT";
            case ALREADY_WATERED_TODAY -> "浇水失败：今天已经浇过水了";
            case MAX_WATER_COUNT_REACHED -> "浇水失败：该作物已达到5次主动浇水上限";
        };
    }

    public String harvest(Soil soil) {
        // 当前MATURE状态由统一世界循环提交；按钮不再推进时间。
        int gold = game.getHarvestService().harvest(soil);
        if (gold > 0) game.save();
        return gold > 0
                ? "收获成功，获得 " + gold + " 金币"
                : "收获失败：作物尚未成熟";
    }
}
