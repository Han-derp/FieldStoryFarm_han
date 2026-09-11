package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.manager.SceneManager;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.GameClock;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.model.impl.BasicFarm;
import com.fieldstory.farm.model.impl.BasicGameClock;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.service.GrowthService;
import com.fieldstory.farm.service.HarvestService;
import com.fieldstory.farm.service.LandService;
import com.fieldstory.farm.service.PlantingService;
import com.fieldstory.farm.service.ShopService;
import com.fieldstory.farm.service.WateringService;
import com.fieldstory.farm.service.economy.EconomyService;
import com.fieldstory.farm.service.economy.impl.EconomyServiceImpl;
import com.fieldstory.farm.service.impl.BasicBuffService;
import com.fieldstory.farm.service.impl.BasicDecorationService;
import com.fieldstory.farm.service.impl.BasicGrowthService;
import com.fieldstory.farm.service.impl.BasicHarvestService;
import com.fieldstory.farm.service.impl.BasicLandService;
import com.fieldstory.farm.service.impl.BasicPlantingService;
import com.fieldstory.farm.service.impl.BasicShopService;
import com.fieldstory.farm.service.impl.BasicWateringService;
import com.fieldstory.farm.view.FarmView;
import com.fieldstory.farm.view.ShopPopupView;
import com.fieldstory.farm.view.StatusView;
import com.fieldstory.farm.view.WarehousePopupView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * 农场装配器（P0 + P1）。
 *
 * <p>P1 闭环：
 * <ul>
 *   <li>右上角 📦 仓库 → 弹出 {@link WarehousePopupView} 显示已拥有装饰；</li>
 *   <li>右上角 🏪 商城 → 弹出 {@link ShopPopupView}（种子 / 装饰）；</li>
 *   <li>仓库里点"选择" → 记录待放置装饰；</li>
 *   <li>点击地图绿色装饰区 → 放置装饰并刷新地图；</li>
 *   <li>仓库里点"收回" → 从地图移除。</li>
 * </ul>
 */
public final class FarmBootstrap {

    private static boolean mounted = false;
    private static Timeline renderTimeline;

    private FarmBootstrap() {
    }

    public static void mountFarmScene() {
        if (mounted) {
            return;
        }
        mounted = true;

        GameState state = GameManager.getInstance().currentState();

        Farm farm = new BasicFarm();
        EconomyService economy = new EconomyServiceImpl(state.getPlayer());
        GameClock gameClock = new BasicGameClock();

        LandService landService = new BasicLandService(economy);
        PlantingService plantingService = new BasicPlantingService(economy, gameClock);
        WateringService wateringService = new BasicWateringService();
        GrowthService growthService = new BasicGrowthService(wateringService);
        HarvestService harvestService = new BasicHarvestService(economy, landService);

        DecorationService decorationService = new BasicDecorationService(farm);
        BuffService buffService = new BasicBuffService(decorationService);
        ShopService shopService = new BasicShopService(economy, decorationService);

        FarmViewController farmViewController = new FarmViewController(
                farm, landService, plantingService, wateringService, harvestService, gameClock);
        farmViewController.mountToScene();
        FarmView farmView = farmViewController.getView();

        FarmGameModel gameModel = new FarmGameModel(gameClock);
        gameModel.setFarm(farm);
        StatusView statusView = new StatusView(gameModel, state.getPlayer());
        SceneManager.getInstance().mount(SceneManager.Slot.TOP, statusView);

        // 装饰控制器
        DecorationController decorationController =
                new DecorationController(decorationService, buffService);

        // 装饰状态变化 → 刷新地图装饰层
        Runnable refreshFarmDecorations =
                () -> farmView.refreshDecorations(decorationService.getPlacedDecorations());
        decorationController.setOnChanged(refreshFarmDecorations);

        // 装饰选择上下文
        DecorationSelectionContext.install(decorationController);

        // 地图装饰区点击 → 尝试放置当前选中的装饰
        farmView.setOnDecorationAreaClicked((row, column) ->
                DecorationSelectionContext.tryPlaceAt(row, column));

        // 仓库弹窗：📦 按钮
        WarehousePopupView warehousePopup =
                new WarehousePopupView(decorationController, DecorationSelectionContext::select);
        statusView.setOnWarehouseButtonClick(() ->
                warehousePopup.toggleBelow(statusView.getWarehouseButton()));

        // 商城弹窗：🏪 按钮
        SeedQuickBuyController seedController = new SeedQuickBuyController(economy);
        ShopController shopController = new ShopController(shopService);
        // 购买装饰成功后，若仓库弹窗打开则刷新（可选）
        shopController.setOnDecorationPurchased(() -> {
            // 仓库弹窗打开时刷新；未打开则下次打开时自动 refresh()
            if (warehousePopup.isShowing()) {
                warehousePopup.refresh();
            }
        });

        ShopPopupView shopPopup = new ShopPopupView(seedController, shopController);
        statusView.setOnShopButtonClick(() ->
                shopPopup.toggleBelow(statusView.getShopButton()));

        // D 的主循环
        FarmController farmController = new FarmController(gameModel, statusView, growthService);
        farmController.startGameLoop();

        farmController.setOnDayChanged(() -> {
            farmView.setCurrentGameDay(gameClock.getGameDay());
            for (Soil soil : farm.getSoils()) {
                farmView.refreshTile(soil);
            }
        });

        renderTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            for (Soil soil : farm.getSoils()) {
                if (soil.getState() == SoilState.PLANTED) {
                    farmView.refreshTile(soil);
                }
            }
        }));
        renderTimeline.setCycleCount(Timeline.INDEFINITE);
        renderTimeline.play();

        refreshFarmDecorations.run();
    }
}