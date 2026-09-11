package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.SceneManager;
import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameClock;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.service.HarvestResult;
import com.fieldstory.farm.service.HarvestService;
import com.fieldstory.farm.service.LandService;
import com.fieldstory.farm.service.PlantingResult;
import com.fieldstory.farm.service.PlantingService;
import com.fieldstory.farm.service.ReclaimResult;
import com.fieldstory.farm.service.WateringResult;
import com.fieldstory.farm.service.WateringService;
import com.fieldstory.farm.view.FarmView;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * 农场视图控制器（A 模块 P0 视图层）。
 *
 * <p>播种按钮只显示作物名，不显示价格。
 */
public class FarmViewController {

    private final Farm farm;
    private final LandService landService;
    private final PlantingService plantingService;
    private final WateringService wateringService;
    private final HarvestService harvestService;
    private final GameClock gameClock;
    private final FarmView farmView;

    public FarmViewController(
            Farm farm,
            LandService landService,
            PlantingService plantingService,
            WateringService wateringService,
            HarvestService harvestService,
            GameClock gameClock) {
        this.farm = farm;
        this.landService = landService;
        this.plantingService = plantingService;
        this.wateringService = wateringService;
        this.harvestService = harvestService;
        this.gameClock = gameClock;
        this.farmView = new FarmView(farm);
        this.farmView.setOnTileSelected(this::onTileSelected);
    }

    public FarmView getView() {
        return farmView;
    }

    public void mountToScene() {
        SceneManager.getInstance().mount(SceneManager.Slot.CENTER, farmView);
    }

    // ==================== 纯静态函数（可无 GUI 线程单测） ====================

    public static List<FarmAction> actionsFor(Soil soil) {
        if (soil == null) {
            return List.of();
        }
        switch (soil.getState()) {
            case EMPTY:
                return List.of(FarmAction.RECLAIM);
            case TILLED:
                return List.of(FarmAction.PLANT);
            case PLANTED:
                Crop crop = soil.getCrop();
                if (crop != null && crop.getGrowthStage() == GrowthStage.MATURE) {
                    return List.of(FarmAction.HARVEST);
                }
                return List.of(FarmAction.WATER);
            case LOCKED:
            default:
                return List.of();
        }
    }

    public static String actionMessageFor(ReclaimResult result) {
        switch (result) {
            case SUCCESS:
                return "开垦成功";
            case NOT_EMPTY:
                return "该格不是空地，无法开垦";
            case NO_GOLD:
                return "金币不足，开垦需要5金币";
            default:
                return "开垦失败";
        }
    }

    public static String actionMessageFor(PlantingResult result) {
        switch (result) {
            case SUCCESS:
                return "播种成功";
            case NOT_TILLED:
                return "该格未开垦，无法播种";
            case NO_SEED:
                return "种子不足，无法播种";
            default:
                return "播种失败";
        }
    }

    public static String actionMessageFor(WateringResult result) {
        switch (result) {
            case SUCCESS:
                return "浇水成功";
            case SEED_STAGE:
                return "种子阶段还不能浇水";
            case ALREADY_WATERED_TODAY:
                return "今天已经浇过水了";
            case WATER_LIMIT_REACHED:
                return "这株作物已经不需要浇水了";
            default:
                return "浇水失败";
        }
    }

    public static String actionMessageFor(HarvestResult result) {
        switch (result) {
            case SUCCESS:
                return "收获成功";
            case NOT_PLANTED:
            case NO_CROP:
                return "该格没有可收获的作物";
            case NOT_MATURE:
                return "作物还没成熟";
            default:
                return "收获失败";
        }
    }

    // ==================== 交互 ====================

    private void onTileSelected(Soil soil) {
        farmView.setCurrentGameDay(gameClock.getGameDay());
        if (soil == null) {
            farmView.hideMenu();
            farmView.selectTile(null);
            return;
        }
        farmView.selectTile(soil);
        List<FarmAction> actions = actionsFor(soil);
        if (actions.isEmpty()) {
            farmView.hideMenu();
            return;
        }
        farmView.showMenuFor(soil, buildButtons(soil, actions));
    }

    private List<Node> buildButtons(Soil soil, List<FarmAction> actions) {
        List<Node> buttons = new ArrayList<>();
        for (FarmAction action : actions) {
            Button button = farmView.createMenuButton(labelFor(action));
            button.setOnAction(event -> perform(soil, action));
            buttons.add(button);
        }
        return buttons;
    }

    private static String labelFor(FarmAction action) {
        switch (action) {
            case RECLAIM:
                return "开垦";
            case PLANT:
                return "播种";
            case WATER:
                return "浇水";
            case HARVEST:
                return "收获";
            default:
                return "";
        }
    }

    private void perform(Soil soil, FarmAction action) {
        switch (action) {
            case RECLAIM:
                reclaim(soil);
                break;
            case PLANT:
                showSeedButtons(soil);
                break;
            case WATER:
                water(soil);
                break;
            case HARVEST:
                harvest(soil);
                break;
            default:
                break;
        }
    }

    private void reclaim(Soil soil) {
        ReclaimResult result = landService.reclaim(soil);
        if (result == ReclaimResult.SUCCESS) {
            farmView.hideMenu();
            farmView.setCurrentGameDay(gameClock.getGameDay());
            farmView.refreshTile(soil);
        } else {
            farmView.showTip(soil, actionMessageFor(result));
        }
    }

    /**
     * 播种入口：弹出种子选择按钮。
     * 按钮只显示作物名，不显示价格。
     */
    private void showSeedButtons(Soil soil) {
        List<Node> buttons = new ArrayList<>();
        for (CropType type : CropType.values()) {
            Button button = farmView.createMenuButton(type.getDisplayName());
            button.setOnAction(event -> plant(soil, type));
            buttons.add(button);
        }
        farmView.showMenuFor(soil, buttons);
    }

    private void plant(Soil soil, CropType type) {
        PlantingResult result = plantingService.plant(soil, type);
        if (result == PlantingResult.SUCCESS) {
            farmView.hideMenu();
            farmView.setCurrentGameDay(gameClock.getGameDay());
            farmView.refreshTile(soil);
        } else {
            farmView.showTip(soil, actionMessageFor(result));
        }
    }

    private void water(Soil soil) {
        Crop crop = soil.getCrop();
        if (crop == null) {
            return;
        }
        WateringResult result = wateringService.water(crop, gameClock.getGameDay());
        if (result == WateringResult.SUCCESS) {
            farmView.hideMenu();
            farmView.setCurrentGameDay(gameClock.getGameDay());
            farmView.refreshTile(soil);
        } else {
            farmView.showTip(soil, actionMessageFor(result));
        }
    }

    private void harvest(Soil soil) {
        HarvestResult result = harvestService.harvest(soil);
        if (result == HarvestResult.SUCCESS) {
            farmView.hideMenu();
            farmView.refreshTile(soil);
        } else {
            farmView.showTip(soil, actionMessageFor(result));
        }
    }
}
