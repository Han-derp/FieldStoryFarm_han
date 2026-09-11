package com.fieldstory.farm.controller;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.service.GrowthService;
import com.fieldstory.farm.view.StatusView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

import static com.fieldstory.farm.util.GameConstants.GAME_DAYS_PER_TICK;

/**
 * 农场主控制器（D 模块 P0：世界环境）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§七、《P0-P4功能实现与验收规范》§3.1。
 *
 * <p>职责：只负责调度（调用 Model 和 Service），不写业务逻辑。
 * 定时器必须使用 JavaFX {@link Timeline} + {@link KeyFrame}，禁止 {@code java.util.Timer}。
 *
 * <p><b>成长协调（验收规范 §3.1）：</b>D 负责"什么时候推进"，
 * A 负责"怎么成长"。本类在每次 tick 后按经过游戏天数遍历当前 Farm 的作物，
 * 调用 A 模块 {@link GrowthService#applyGrowth(Crop, double)}；
 * 成长公式不在本类重复实现。
 */
public class FarmController {

    private final FarmGameModel model;
    private final StatusView statusView;

    /**
     * 成长服务（A 模块）；未注入时为 null，此时主循环只推进时间、不协调成长
     * （P0 早期装配前保持向后兼容）。
     */
    private final GrowthService growthService;

    private final Timeline gameLoopTimeline;

    /**
     * 注入模型和视图，初始化定时器（不协调成长，向后兼容）。
     *
     * @param model      游戏模型
     * @param statusView 状态栏视图
     */
    public FarmController(FarmGameModel model, StatusView statusView) {
        this(model, statusView, null);
    }

    /**
     * 注入模型、视图与成长服务，初始化定时器。
     *
     * @param model         游戏模型
     * @param statusView    状态栏视图
     * @param growthService 成长服务（A 模块），可为 null（不协调成长）
     */
    public FarmController(FarmGameModel model, StatusView statusView,
                          GrowthService growthService) {
        this.model = model;
        this.statusView = statusView;
        this.growthService = growthService;
        this.gameLoopTimeline = initGameLoop();
    }

    /**
     * 创建 Timeline 和 KeyFrame，每秒触发一次：
     * {@code model.tick()} → 协调 {@link GrowthService} 推进作物 → {@code statusView.update()}。
     *
     * <p>成长协调：遍历当前 Farm 全部 Soil，对已播种（crop 非 null）的作物
     * 按 {@link com.fieldstory.farm.util.GameConstants#GAME_DAYS_PER_TICK}
     * 调用 {@code growthService.applyGrowth(crop, elapsedGameDays)}。
     * Farm 未装配或 GrowthService 未注入时跳过（P0 早期装配前）。
     *
     * @return 已配置的定时器
     */
    private Timeline initGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            model.tick();
            advanceCrops();
            statusView.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * 协调 A 模块成长服务推进当前 Farm 全部作物（D 只负责推进时机）。
     *
     * <p>Farm 未装配或 GrowthService 未注入时不做任何事。
     */
    private void advanceCrops() {
        advanceCrops(model.getFarm(), growthService, GAME_DAYS_PER_TICK);
    }

    /**
     * 纯函数：遍历 Farm 全部 Soil，对已播种作物按经过游戏天数调用成长服务。
     *
     * <p>D 只负责"什么时候推进"，成长公式由 A 模块 {@link GrowthService} 实现，
     * 本方法不重复任何成长规则（验收规范 §3.1）。
     *
     * <p>空安全：farm / growthService / soils / soil 任一为 null 时安全跳过，
     * 便于 P0 早期装配前调用与单元测试。
     *
     * @param farm             农田地图，可为 null
     * @param growthService    成长服务，可为 null
     * @param elapsedGameDays  本次经过的游戏天数
     */
    static void advanceCrops(Farm farm, GrowthService growthService,
                             double elapsedGameDays) {
        if (farm == null || growthService == null) {
            return;
        }
        List<Soil> soils = farm.getSoils();
        if (soils == null) {
            return;
        }
        for (Soil soil : soils) {
            if (soil == null) {
                continue;
            }
            Crop crop = soil.getCrop();
            if (crop != null) {
                growthService.applyGrowth(crop, elapsedGameDays);
            }
        }
    }

    /**
     * 启动定时器。
     */
    public void startGameLoop() {
        gameLoopTimeline.play();
    }

    /**
     * 停止定时器（退出时停止，非功能需求 §2.3）。
     */
    public void stopGameLoop() {
        gameLoopTimeline.pause();
    }
}
