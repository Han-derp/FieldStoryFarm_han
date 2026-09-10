package com.fieldstory.farm.view;

import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.GameClock;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * 顶部状态栏视图（D 模块 P0：世界环境）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§五、《P0-P4功能实现与验收规范》§36/§76。
 *
 * <p>显示：游戏日、游戏时间、金币、天气（P1 预留，P0 固定"晴天"）。
 * 只读原则：只能通过 Getter 读取数据，不得调用任何 Service 写方法（验收 §3.1）。
 */
public class StatusView extends HBox {

    /** 数据来源（只读）。 */
    private final FarmGameModel model;

    private final Label dayLabel;
    private final Label timeLabel;
    private final Label goldLabel;
    private final Label weatherLabel;

    /**
     * 注入模型，初始化 UI 组件并调用 {@link #update()}。
     *
     * @param model 游戏模型
     */
    public StatusView(FarmGameModel model) {
        this.model = model;
        this.dayLabel = new Label();
        this.timeLabel = new Label();
        this.goldLabel = new Label();
        this.weatherLabel = new Label();
        this.setSpacing(16);
        this.getChildren().addAll(dayLabel, timeLabel, goldLabel, weatherLabel);
        update();
    }

    /**
     * 刷新显示（由 Controller 定时调用，每秒一次，规则 §5.1）。
     */
    public void update() {
        GameClock clock = model.getGameClock();
        dayLabel.setText("第 " + clock.getGameDay() + " 天");
        timeLabel.setText(getDaytimeIcon() + " " + clock.getTimeString());
        goldLabel.setText("金币 --");
        weatherLabel.setText("晴天");
    }

    /**
     * 根据 {@code isDaytime()} 返回白天/夜晚图标。
     *
     * @return 白天返回太阳图标，否则返回月亮图标
     */
    private String getDaytimeIcon() {
        return model.getGameClock().isDaytime() ? "\u2600" : "\uD83C\uDF19";
    }

    /** 供测试读取游戏日文本。 */
    public String getDayText() {
        return dayLabel.getText();
    }

    /** 供测试读取时间文本。 */
    public String getTimeText() {
        return timeLabel.getText();
    }

    /** 供测试读取金币文本。 */
    public String getGoldText() {
        return goldLabel.getText();
    }

    /** 供测试读取天气文本。 */
    public String getWeatherText() {
        return weatherLabel.getText();
    }
}
