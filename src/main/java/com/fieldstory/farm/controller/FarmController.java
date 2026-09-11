package com.fieldstory.farm.controller;

import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.view.StatusView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * 农场主控制器（D 模块 P0：世界环境）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§七、《P0-P4功能实现与验收规范》§3.1。
 *
 * <p>职责：只负责调度（调用 Model 和 Service），不写业务逻辑。
 * 定时器必须使用 JavaFX {@link Timeline} + {@link KeyFrame}，禁止 {@code java.util.Timer}。
 */
public class FarmController {

    private final FarmGameModel model;
    private final StatusView statusView;
    private final Timeline gameLoopTimeline;

    /**
     * 注入模型和视图，初始化定时器。
     *
     * @param model      游戏模型
     * @param statusView 状态栏视图
     */
    public FarmController(FarmGameModel model, StatusView statusView) {
        this.model = model;
        this.statusView = statusView;
        this.gameLoopTimeline = initGameLoop();
    }

    /**
     * 创建 Timeline 和 KeyFrame，每秒触发 {@code model.tick()} 和 {@code statusView.update()}。
     *
     * <p>注意：作物生长更新由 Controller 协调调用 A 模块的 GrowthService，
     * 具体调用方式需与 A 模块协商（验收 §3.1）。
     *
     * @return 已配置的定时器
     */
    private Timeline initGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            model.tick();
            statusView.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
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
