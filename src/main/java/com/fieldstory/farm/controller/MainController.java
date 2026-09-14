package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.manager.SceneManager;
import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WeatherType;
import com.fieldstory.farm.model.impl.BasicFarm;
import com.fieldstory.farm.persistence.FarmStateAdapter;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.service.GrowthService;
import com.fieldstory.farm.service.HarvestService;
import com.fieldstory.farm.service.LandService;
import com.fieldstory.farm.service.PlantingService;
import com.fieldstory.farm.service.ShopService;
import com.fieldstory.farm.service.WateringService;
import com.fieldstory.farm.service.WitherService;
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
import com.fieldstory.farm.service.impl.BasicWitherService;
import com.fieldstory.farm.util.GameConstants;
import com.fieldstory.farm.util.RandomProvider;
import com.fieldstory.farm.view.BusinessToolbarView;
import com.fieldstory.farm.view.DecorationOverlayView;
import com.fieldstory.farm.view.FarmView;
import com.fieldstory.farm.view.StatusView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * 主界面控制器（E 场景组装：开始按钮装配 A/B/C/D 各模块，构成可玩最小闭环）。
 *
 * <p>本类只负责装配与调度：
 * A 提供农场/成长/浇水等能力，B 提供经济/商店/装饰/Buff，
 * C 提供收获，D 提供时间/天气，E 负责场景与存档。
 */
public class MainController {

    @FXML
    private Label welcomeText;

    /** 全局唯一游戏管理器（单例） */
    private final GameManager gameManager;

    /** 顶栏常驻提示标签（开局后主菜单被替换，承接保存/装饰操作反馈） */
    private Label topHintLabel;

    /** FXML 默认构造：使用全局唯一 {@link GameManager} 单例。 */
    public MainController() {
        this(GameManager.getInstance());
    }

    /** 允许注入 GameManager（单测用，避免触碰真实 SQLite 存档）。 */
    MainController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /** 本次会话是否已完成装配（防止重复点击重复装配）。 */
    private boolean assembled = false;

    /** 上次记录的游戏日（跨天成长推进基准；-1 表示尚未初始化）。 */
    private int lastGrowthDay = -1;

    @FXML
    private void initialize() {
        welcomeText.setText("欢迎来到田野故事农场！");
    }

    /** 开始新游戏：无视历史存档，从新档开始。 */
    @FXML
    protected void onNewGameButtonClick() {
        if (rejectIfRunning()) {
            return;
        }
        assembleGame(true);
    }

    /** 读取存档：从数据库恢复上次退出瞬间的进度；无存档时只提示。 */
    @FXML
    protected void onLoadButtonClick() {
        if (rejectIfRunning()) {
            return;
        }
        if (!gameManager.hasSavedGame()) {
            setStatusMessage("没有找到存档，请先点击“开始新游戏”。");
            return;
        }
        assembleGame(false);
    }

    /** 已在游戏中则提示并返回 true，避免重复装配。 */
    private boolean rejectIfRunning() {
        if (assembled) {
            setStatusMessage("游戏已在运行中。");
            return true;
        }
        return false;
    }

    /**
     * 装配游戏闭环。
     *
     * @param newGame true=强制新档；false=读取存档
     */
    private void assembleGame(boolean newGame) {
        GameState state = newGame ? gameManager.startNewGame() : gameManager.start();
        Player player = state.getPlayer();

        Farm farm = new BasicFarm();
        FarmGameModel model = new FarmGameModel();
        model.setFarm(farm);

        // 恢复 A 的土地/作物快照与 D 的游戏日。
        FarmStateAdapter.restore(state, farm);
        restoreGameDay(state, model);

        // E 存档前统一回填运行态。
        // GameState.gameDay 是从 0 起的已结算天数（新档 = 0），而 GameClock.getGameDay() 从第 1 天起，
        // 两者相差 1；此处必须减 1，与「读取存档」的还原口径（见 restoreGameDay）保持一致，
        // 否则新档会被写成第 1 天，且玩过 N 天后重开会退回一天。
        gameManager.setBeforeSaveHook(() -> {
            FarmStateAdapter.capture(state, farm);
            state.setGameDay(model.getGameClock().getGameDay() - 1L);
        });

        // B P0 经济入口保持唯一 Player。
        // 新游戏严格保持 Player/GameManager 的正式初始状态：500 金币、三种种子库存均为 0。
        // 不得用 buySeed() “赠送”起始种子，否则会真实扣款 135 金币，导致 500 -> 365。
        EconomyService economy = new EconomyServiceImpl(player);

        // A/C/D 已有服务保持当前主干实现。
        LandService land = new BasicLandService(economy);
        PlantingService planting = new BasicPlantingService(economy, model.getGameClock());
        WateringService watering = new BasicWateringService();
        GrowthService growth = new BasicGrowthService(watering);
        HarvestService harvest = new BasicHarvestService(economy, land);
        WitherService wither = new BasicWitherService();

        // A 的 FarmView 不改；B 装饰通过透明覆盖层扩展 CENTER。
        FarmViewController farmViewController = new FarmViewController(
                farm, land, planting, watering, harvest, model.getGameClock());
        FarmView farmView = farmViewController.getView();

        // B P1：装饰状态直接绑定当前 GameState；E 的 SqliteSaveService 负责最终落盘。
        DecorationService decorationService = new BasicDecorationService(farm, state);
        BuffService buffService = new BasicBuffService(decorationService);
        ShopService shopService = new BasicShopService(economy, decorationService);

        DecorationController decorationController =
                new DecorationController(decorationService, buffService);
        ShopController shopController = new ShopController(shopService);

        DecorationOverlayView decorationOverlay =
                new DecorationOverlayView(farm, farmView, decorationController);
        decorationOverlay.setMessageSink(this::setStatusMessage);
        SceneManager.getInstance().mount(SceneManager.Slot.CENTER, decorationOverlay);

        // 购买/放置/移动/收回成功后自动保存；B 不直接写 SQL。
        shopController.addOnPurchaseSucceeded(gameManager::saveNow);
        decorationController.addOnChanged(gameManager::saveNow);

        BusinessToolbarView businessToolbar = new BusinessToolbarView(
                shopController, decorationController, decorationOverlay);

        // D 状态栏保持原实现；B 经营入口作为独立节点由 E 装配。
        StatusView statusView = new StatusView(model, player);
        buildTopBar(statusView, businessToolbar);

        // 主循环：沿用当前主干的天气 + 枯萎 + 成长流程。
        lastGrowthDay = model.getGameClock().getGameDay();
        FarmController farmLoop = new FarmController(model, statusView);
        farmLoop.setOnDayChanged(() -> applyDailyGrowth(
                farm, growth, wither, farmViewController, model));

        // “开始新游戏”完成装配后立即建立正式存档。
        // GameManager.startNewGame() 本身只创建内存状态；若依赖窗口正常关闭才保存，
        // 在 IDE 直接 Stop、异常退出等情况下，下一次“读取存档”会找不到这局。
        // 此处保存时 beforeSaveHook 已注册，会把初始 Farm + 第 0 天一起写入 SQLite。
        if (newGame) {
            gameManager.saveNow();
        }

        farmLoop.startGameLoop();

        assembled = true;
        setStatusMessage("点击农田开始：开垦 → 播种 → 浇水 → 等待成长。");
    }

    /**
     * 读档还原游戏天数；P1 仍按该日 06:00 恢复。
     *
     * <p>{@link GameState#getGameDay()} 为从 0 起的已结算天数（新档 = 0）：第 d 天（d 从 1 起）对应
     * {@code gameDay = d - 1}，恢复成该日 06:00 的时钟总分钟数 {@code gameDay * MINUTES_PER_DAY + DAY_START}。
     * 新档 {@code gameDay = 0} 时保持时钟初值（第 1 天 06:00）。
     */
    private static void restoreGameDay(GameState state, FarmGameModel model) {
        long savedDay = state.getGameDay();
        if (savedDay > 0) {
            int totalMinutes = (int) (savedDay * GameConstants.MINUTES_PER_DAY
                    + GameConstants.DAY_START);
            model.restoreWorldTime(totalMinutes);
        }
    }

    /**
     * 跨天：滚动天气 → 记录天气并判定枯萎 → 推进幸存作物成长 → 刷新农场。
     */
    private void applyDailyGrowth(Farm farm,
                                  GrowthService growth,
                                  WitherService wither,
                                  FarmViewController farmViewController,
                                  FarmGameModel model) {
        int currentDay = model.getGameClock().getGameDay();
        double elapsedDays = currentDay - lastGrowthDay;

        if (elapsedDays > 0) {
            WeatherType today = model.getWeatherService().rollDailyWeather(currentDay);
            double weatherRate = model.getWeatherService().getGrowthRate(today);
            long worldTime = currentDay * 24L + model.getGameClock().getGameHour();

            for (Soil soil : farm.getSoils()) {
                Crop crop = soil.getCrop();
                if (crop == null) {
                    continue;
                }

                wither.recordDailyWeather(crop, today, currentDay, worldTime);
                wither.judgeWither(
                        crop,
                        today,
                        currentDay,
                        BasicWitherService.WITHER_MITIGATION_P1,
                        RandomProvider.nextDouble());

                if (crop.getGrowthStage() != GrowthStage.MATURE
                        && crop.getGrowthStage() != GrowthStage.WITHERED) {
                    growth.applyGrowth(crop, elapsedDays, weatherRate);
                }
            }
        }

        lastGrowthDay = currentDay;
        farmViewController.getView().setCurrentGameDay(currentDay);
        farmViewController.getView().refreshAll();
    }

    /** 手动存档入口。 */
    @FXML
    protected void onSaveButtonClick() {
        try {
            gameManager.saveNow();
            setStatusMessage("进度已保存！");
        } catch (IllegalStateException e) {
            setStatusMessage("尚无进行中的游戏，请先点击“开始新游戏”。");
        }
    }

    /** 兼容既有测试/调用：仅状态栏 + 保存入口。 */
    void buildTopBar(StatusView statusView) {
        buildTopBar(statusView, null);
    }

    /**
     * 构建常驻顶栏：D 状态栏 + B 经营入口 + E 保存按钮 + 提示。
     */
    void buildTopBar(StatusView statusView, Node businessToolbar) {
        Button saveButton = new Button("保存进度");
        saveButton.setOnAction(event -> onSaveButtonClick());

        topHintLabel = new Label();
        HBox topBar = new HBox(16);
        topBar.getChildren().add(statusView);
        if (businessToolbar != null) {
            topBar.getChildren().add(businessToolbar);
        }
        topBar.getChildren().addAll(saveButton, topHintLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(6, 12, 6, 12));

        SceneManager.getInstance().mount(SceneManager.Slot.TOP, topBar);
    }

    /** 统一提示输出，主菜单与开局后的常驻顶栏都可见。 */
    private void setStatusMessage(String message) {
        if (welcomeText != null) {
            welcomeText.setText(message);
        }
        if (topHintLabel != null) {
            topHintLabel.setText(message);
        }
    }
}
