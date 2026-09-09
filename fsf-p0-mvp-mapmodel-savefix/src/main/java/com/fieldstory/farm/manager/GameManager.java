package com.fieldstory.farm.manager;

import com.fieldstory.farm.model.*;
import com.fieldstory.farm.service.*;
import com.fieldstory.farm.service.impl.*;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;

/**
 * 项目级游戏状态组装与世界推进入口。
 *
 * P0关键约束：advanceWorld() 是唯一允许推进 GameClock 的生产代码入口。
 */
public class GameManager {
    public static final LocalDateTime P0_START_WORLD_TIME =
            LocalDateTime.of(2026, 1, 1, 8, 0);

    private Player player;
    private Farm farm;
    private GameClock clock;

    private EconomyService economyService;
    private LandService landService;
    private PlantingService plantingService;
    private WateringService wateringService;
    private GrowthService growthService;
    private BasicHarvestService harvestService;

    private final SaveService saveService;
    private final Function<LocalDateTime, GameClock> clockFactory;

    private long worldAdvanceCount;
    private long lastAdvanceGameMinutes;

    /** 正式/演示运行默认使用P0 JSON存档 + DemoGameClock。 */
    public GameManager() {
        this(
                new JsonSaveService(Path.of("data", "save-p0.json")),
                DemoGameClock::new
        );
    }

    /**
     * 依赖注入构造器。
     * 生产环境可替换SaveService/Clock实现；自动测试使用临时JSON + TestGameClock。
     */
    public GameManager(
            SaveService saveService,
            Function<LocalDateTime, GameClock> clockFactory) {

        this.saveService = Objects.requireNonNull(saveService);
        this.clockFactory = Objects.requireNonNull(clockFactory);
    }

    public void start() {
        if (saveService.hasSave()) load();
        else newGame();
    }

    public void newGame() {
        this.player = new Player();
        this.farm = Farm.createP0Farm();
        this.clock = clockFactory.apply(P0_START_WORLD_TIME);
        this.worldAdvanceCount = 0;
        this.lastAdvanceGameMinutes = 0;
        wireServices();
        save();
    }

    public void load() {
        GameState state = saveService.load();
        this.player = state.getPlayer();
        this.farm = state.getFarm();
        this.clock = clockFactory.apply(state.getCurrentWorldTime());
        this.worldAdvanceCount = 0;
        this.lastAdvanceGameMinutes = 0;
        wireServices();
    }

    private void wireServices() {
        economyService = new BasicEconomyService(player);
        landService = new BasicLandService(economyService);
        plantingService = new BasicPlantingService(economyService, clock);
        wateringService = new BasicWateringService(clock);
        growthService = new BasicGrowthService(clock);
        harvestService = new BasicHarvestServiceImpl(economyService, landService);
    }

    /**
     * 唯一世界推进入口。
     * App的统一Timeline调用它；按钮、Controller、View、save()都不得调用。
     */
    public void advanceWorld(Duration elapsed) {
        Objects.requireNonNull(elapsed);
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("elapsed must not be negative");
        }

        LocalDateTime before = clock.now();
        clock.advance(elapsed);
        LocalDateTime after = clock.now();

        growthService.update(farm);

        worldAdvanceCount++;
        lastAdvanceGameMinutes = Duration.between(before, after).toMinutes();
    }

    /** save() 只持久化当前快照，不推进时间、不触发成长。 */
    public void save() {
        saveService.save(new GameState(player, farm, clock.now()));
    }

    public Player getPlayer() { return player; }
    public Farm getFarm() { return farm; }
    public GameClock getClock() { return clock; }
    public EconomyService getEconomyService() { return economyService; }
    public LandService getLandService() { return landService; }
    public PlantingService getPlantingService() { return plantingService; }
    public WateringService getWateringService() { return wateringService; }
    public GrowthService getGrowthService() { return growthService; }
    public BasicHarvestService getHarvestService() { return harvestService; }
    public long getWorldAdvanceCount() { return worldAdvanceCount; }
    public long getLastAdvanceGameMinutes() { return lastAdvanceGameMinutes; }
}
