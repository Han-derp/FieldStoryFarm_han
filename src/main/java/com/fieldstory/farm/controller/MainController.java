package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.manager.SceneManager;
import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropMemory;
import com.fieldstory.farm.model.EventState;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WeatherType;
import com.fieldstory.farm.model.impl.BasicFarm;
import com.fieldstory.farm.model.item.EventPriceRateProvider;
import com.fieldstory.farm.model.item.Inventory;
import com.fieldstory.farm.persistence.FarmStateAdapter;
import com.fieldstory.farm.persistence.SaveSlot;
import com.fieldstory.farm.persistence.SaveSlotInfo;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.service.GrowthService;
import com.fieldstory.farm.service.HarvestResult;
import com.fieldstory.farm.service.HarvestService;
import com.fieldstory.farm.service.HarvestTransactionService;
import com.fieldstory.farm.service.LandService;
import com.fieldstory.farm.service.LegendaryService;
import com.fieldstory.farm.service.MemoryService;
import com.fieldstory.farm.service.PlantingService;
import com.fieldstory.farm.service.QualityService;
import com.fieldstory.farm.service.ShopService;
import com.fieldstory.farm.service.WateringService;
import com.fieldstory.farm.service.WitherService;
import com.fieldstory.farm.service.economy.EconomyService;
import com.fieldstory.farm.service.economy.impl.EconomyServiceImpl;
import com.fieldstory.farm.service.impl.BasicBuffService;
import com.fieldstory.farm.service.impl.BasicDecorationService;
import com.fieldstory.farm.service.impl.BasicGrowthService;
import com.fieldstory.farm.service.impl.BasicHarvestTransactionService;
import com.fieldstory.farm.service.impl.BasicLandService;
import com.fieldstory.farm.service.impl.BasicLegendaryService;
import com.fieldstory.farm.service.impl.BasicMemoryService;
import com.fieldstory.farm.service.impl.BasicPlantingService;
import com.fieldstory.farm.service.impl.BasicQualityService;
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
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * 主界面控制器（E 场景组装：开始按钮装配 A/B/C/D 各模块，构成可玩最小闭环）。
 *
 * <p>本类只负责装配与调度：
 * A 提供农场/成长/浇水等能力，B 提供经济/商店/装饰/Buff，
 * C 提供收获（P2 起为完整收获事务，产出品质/传说/生命记忆），
 * D 提供时间/天气/随机事件，E 负责场景与存档。
 *
 * <p><b>P2 存档（三存档位）</b>：主菜单逐行展示三个存档位（空档 / 第 N 天 · 金币 /
 * 存档时间），玩家选一档「读取」或「新游戏」；进游戏后所有保存都落在该档。
 * 存档前回填钩子除地块外，还会把<b>世界时钟总分钟、天气、作物生命记忆、当前事件、
 * 背包物品</b>一起写进 {@link GameState}，使"关掉再打开"能回到退出瞬间而非当天 06:00。
 */
public class MainController {

    @FXML
    private Label welcomeText;

    /** P2：存档位列表容器（每行由 {@link #buildSlotRow} 装配）。 */
    @FXML
    private VBox slotList;

    /** P2：主菜单「新建存档」按钮（FXML 注入）。 */
    @FXML
    private Button newSaveButton;

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
        buildSlotList();
    }

    // ==================================================================
    // P2 无限存档位：主菜单
    // ==================================================================

    /** 装配存档位列表：只列出磁盘上已存在的存档（保存了几个就显示几个）。 */
    private void buildSlotList() {
        if (slotList == null) {
            return;
        }
        slotList.getChildren().clear();
        List<SaveSlotInfo> infos = gameManager.allSlotInfos();
        if (infos.isEmpty()) {
            slotList.getChildren().add(new Label("还没有存档，点击「新建存档」开始游戏。"));
            return;
        }
        for (SaveSlotInfo info : infos) {
            slotList.getChildren().add(buildSlotRow(info));
        }
    }

    /** 单个存档位行：`存档 N：摘要（存档时间）  [读取] [新游戏]`。 */
    private HBox buildSlotRow(SaveSlotInfo info) {
        StringBuilder text = new StringBuilder()
                .append(info.slot().displayName())
                .append("：")
                .append(info.describe());
        if (info.savedAt() != null) {
            text.append("（").append(info.savedAt()).append("）");
        }
        Label summary = new Label(text.toString());
        summary.setMinWidth(260);

        Button loadButton = new Button("读取");
        loadButton.setDisable(!info.occupied());
        loadButton.setOnAction(event -> onSlotLoad(info.slot()));

        Button newGameButton = new Button("新游戏");
        newGameButton.setOnAction(event -> onSlotNewGame(info.slot()));

        HBox row = new HBox(12, summary, loadButton, newGameButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** 新建存档：占用下一个空存档位并开始新游戏（兼容入口，供旧测试调用）。 */
    @FXML
    protected void onNewGameButtonClick() {
        onNewSaveButtonClick();
    }

    /** 新建存档：占用下一个空存档位并开始新游戏（主菜单「新建存档」入口）。 */
    @FXML
    protected void onNewSaveButtonClick() {
        if (rejectIfRunning()) {
            return;
        }
        assembleGame(gameManager.nextSlot(), true);
    }

    /** 读取存档：读取当前存档位（兼容入口，供旧测试与 FXML 调用）。 */
    @FXML
    protected void onLoadButtonClick() {
        onSlotLoad(gameManager.currentSlot());
    }

    /** 在指定存档位开始新游戏：无视该档历史进度（玩家显式选择该档，不做二次确认）。 */
    protected void onSlotNewGame(SaveSlot slot) {
        if (rejectIfRunning()) {
            return;
        }
        assembleGame(slot, true);
    }

    /** 读取指定存档位；空档只提示不进入。 */
    protected void onSlotLoad(SaveSlot slot) {
        if (rejectIfRunning()) {
            return;
        }
        if (!gameManager.hasSavedGame(slot)) {
            setStatusMessage(slot.displayName() + " 是空档，请先点击该档的「新游戏」。");
            buildSlotList();
            return;
        }
        assembleGame(slot, false);
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
     * @param slot    目标存档位（读取或新建都作用于它，之后的保存也落回它）
     * @param newGame true=强制新档；false=读取存档
     */
    private void assembleGame(SaveSlot slot, boolean newGame) {
        GameState state = newGame ? gameManager.startNewGame(slot) : gameManager.start(slot);
        Player player = state.getPlayer();

        Farm farm = new BasicFarm();
        FarmGameModel model = new FarmGameModel();
        model.setFarm(farm);

        // 恢复 A 的土地/作物快照、D 的时钟（精确到分钟）、天气。
        FarmStateAdapter.restore(state, farm);
        restoreClock(state, model);
        restoreWeather(state, model);

        // P2：背包 = 存档聚合里的同一个实例（读档时已由 SqliteSaveService 填好），
        // 收获肥料奖励会直接进它，保存时也直接取它，不存在第二份背包。
        Inventory inventory = state.getInventory();

        // P2：生命记忆（C）与随机事件（D）——读档时先灌回内存服务/状态，
        // 之后收获、跨日都在这份"继续的记录"上累加。
        MemoryService memoryService = new BasicMemoryService();
        for (CropMemory memory : state.getMemories()) {
            if (memory != null) {
                memoryService.save(memory);
            }
        }
        restoreEvent(state, model);

        // E 存档前统一回填运行态。
        // GameState.gameDay 是从 0 起的已结算天数（新档 = 0），而 GameClock.getGameDay() 从第 1 天起，
        // 两者相差 1；此处必须减 1，与「读取存档」的还原口径（见 restoreClock）保持一致，
        // 否则新档会被写成第 1 天，且玩过 N 天后重开会退回一天。
        gameManager.setBeforeSaveHook(() -> {
            FarmStateAdapter.capture(state, farm);
            state.setGameDay(model.getGameClock().getGameDay() - 1L);
            state.setWorldTotalMinutes(model.getWorldTimeTotalMinutes());
            state.setCurrentWeather(model.getWeatherState().getWeatherType());
            state.setWeatherDayIndex(model.getWeatherState().getDayIndex());
            captureMemories(state, memoryService);
            state.setActiveEvent(model.getEventState());
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
        HarvestService harvest = buildHarvestService(
                economy, land, model, memoryService, inventory);
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
     * 读档还原时钟。
     *
     * <p>P2 起优先用 {@link GameState#getWorldTotalMinutes()} 精确恢复（存档前由回填钩子写入），
     * 因此重开回到的是<b>退出那一刻</b>；旧档没有该值（-1）时退回 P1 的按天口径：
     * {@code gameDay} 为从 0 起的已结算天数，第 d 天对应时钟总分钟
     * {@code gameDay * MINUTES_PER_DAY + DAY_START}（即该日 06:00）。
     */
    private static void restoreClock(GameState state, FarmGameModel model) {
        long savedMinutes = state.getWorldTotalMinutes();
        if (savedMinutes >= 0) {
            model.restoreWorldTime((int) savedMinutes);
            return;
        }
        long savedDay = state.getGameDay();
        if (savedDay > 0) {
            int totalMinutes = (int) (savedDay * GameConstants.MINUTES_PER_DAY
                    + GameConstants.DAY_START);
            model.restoreWorldTime(totalMinutes);
        }
    }

    /** 读档还原天气（未记录则保持初值 SUNNY / 第 1 天）。 */
    private static void restoreWeather(GameState state, FarmGameModel model) {
        WeatherType weather = state.getCurrentWeather();
        if (weather != null) {
            model.getWeatherState().setWeatherType(weather);
            model.getWeatherState().setDayIndex(
                    Math.max(1, state.getWeatherDayIndex()));
        }
    }

    /** 读档还原当前随机事件（事件期间退出，回来不能凭空消失，验收 §九十一）。 */
    private static void restoreEvent(GameState state, FarmGameModel model) {
        EventState saved = state.getActiveEvent();
        if (saved == null) {
            return;
        }
        EventState live = model.getEventState();
        live.setEventType(saved.getEventType());
        live.setStartWorldTime(saved.getStartWorldTime());
        live.setEndWorldTime(saved.getEndWorldTime());
        live.setTargetCropType(saved.getTargetCropType());
        live.setPayload(saved.getPayload());
    }

    /** 存档前回填生命记忆：内存服务是唯一权威来源，快照整体重建避免残留。 */
    private static void captureMemories(GameState state, MemoryService memoryService) {
        state.getMemories().clear();
        state.getMemories().addAll(memoryService.listAll());
    }

    /**
     * 装配 P2 完整收获事务（验收规范 §一百零三），并适配为 A 视图依赖的 {@link HarvestService}。
     *
     * <p>为什么需要这层适配：A 的 {@code FarmViewController} 只依赖 P0 的
     * {@link HarvestService#harvest(com.fieldstory.farm.model.Soil)}（返回结果码用于提示），
     * 而 P2 事务（品质/传说/记忆/肥料/事件倍率）是 {@link HarvestTransactionService}。
     * 装配层负责把两者接起来，A 的视图代码一行都不用改（决策 D09：不越层改别人的类）。
     *
     * <p>肥料奖励直接进 {@code GameState} 里的同一个背包实例，随存档往返。
     */
    private static HarvestService buildHarvestService(EconomyService economy,
                                                      LandService land,
                                                      FarmGameModel model,
                                                      MemoryService memoryService,
                                                      Inventory inventory) {
        QualityService qualityService = new BasicQualityService();
        LegendaryService legendaryService = new BasicLegendaryService();
        // 神秘商人：事件期间目标作物售价 ×2（规则 §四十九），倍率由 D 的状态决定、C 只读取
        EventPriceRateProvider priceRateProvider = cropType -> {
            EventState event = model.getEventState();
            if (cropType != null
                    && event != null
                    && model.getEventService().isMysteryMerchant(event.getEventType())
                    && cropType == event.getTargetCropType()) {
                return GameConstants.EVENT_MYSTERY_MERCHANT_PRICE_RATE;
            }
            return 1.0;
        };
        HarvestTransactionService transaction = new BasicHarvestTransactionService(
                economy, land, qualityService, legendaryService, memoryService,
                model.getGameClock(), priceRateProvider);

        return new HarvestService() {
            @Override
            public boolean canHarvest(Soil soil) {
                return transaction.canHarvest(soil);
            }

            @Override
            public HarvestResult harvest(Soil soil) {
                return transaction.harvest(soil, inventory).getResult();
            }
        };
    }

    /**
     * 跨天：滚动天气与随机事件 → 记录天气并判定枯萎 → 推进幸存作物成长 → 刷新农场。
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
            // P2：每天抽取当天事件（D 的规则入口；一天最多 1 个，验收 §九十）。
            // 事件状态随后由存档回填钩子写入 active_event，重开时不会凭空消失。
            model.getEventService().rollDailyEvent(currentDay);
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
