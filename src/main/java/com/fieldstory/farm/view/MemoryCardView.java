package com.fieldstory.farm.view;

import com.fieldstory.farm.service.ShowcaseEntry;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * 记忆卡片视图（C 模块 品质与传说域，P3 展示台；分级文档 P3 对应类）。
 *
 * <p>渲染单个 {@link ShowcaseEntry}，覆盖验收规范 §一百二十四 要求的
 * 九项内容：传说名称、作物类型、品质、种植时间、收获时间、关键天气、
 * 关键事件、玩家操作、完整生命故事。
 *
 * <p>View 职责（脚手架 §七.5：只负责显示与输入交互），只读取条目文本，
 * 不修改任何 Model/Service 状态；颜色只用 UI规范 §14 主色表（高亮传说名、
 * 文字色、木色分隔），字号按 UI规范 §15（模块标题 18 / 普通文字 14 /
 * 提示 12）。
 */
public class MemoryCardView extends VBox {

    /** 传说名高亮 #E8C45C（UI规范 §14 高亮） */
    private static final String LEGENDARY_TITLE_STYLE =
            "-fx-text-fill: #E8C45C;"
                    + "-fx-font-size: 18;"
                    + "-fx-font-weight: bold;";

    /** 普通信息行：文字 #493526、14 号（UI规范 §14 文字、§15 普通文字） */
    private static final String INFO_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 14;";

    /** 故事小节标题：文字 #493526、14 号加粗 */
    private static final String STORY_HEADING_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 14;"
                    + "-fx-font-weight: bold;";

    /** 生命故事正文：文字 #493526、14 号（UI规范 §14、§15） */
    private static final String STORY_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 14;";

    /**
     * 渲染展示条目。
     *
     * @param entry 展示台条目（已格式化的传说记忆）
     */
    public MemoryCardView(ShowcaseEntry entry) {
        Objects.requireNonNull(entry, "展示条目不能为空");
        setSpacing(4);
        setPadding(new Insets(8));
        setStyle("-fx-background-color: #FFF3DD;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #8B5E3C;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 12;");

        Label nameLabel = new Label(entry.getLegendaryName());
        nameLabel.setStyle(LEGENDARY_TITLE_STYLE);

        Label typeQualityLabel = new Label(infoLine("作物", entry.getCropType().getDisplayName())
                + "　" + infoLine("品质", entry.getQuality().getDisplayName()));
        typeQualityLabel.setStyle(INFO_STYLE);

        Label timeLabel = new Label(infoLine("种植时间", entry.getPlantTimeText())
                + "　" + infoLine("收获时间", entry.getHarvestTimeText()));
        timeLabel.setStyle(INFO_STYLE);

        Label weatherLabel = new Label(infoLine("关键天气", entry.getWeatherSummary()));
        weatherLabel.setStyle(INFO_STYLE);

        Label eventLabel = new Label(infoLine("关键事件", entry.getEventSummary()));
        eventLabel.setStyle(INFO_STYLE);

        Label actionLabel = new Label(infoLine("玩家操作", entry.getActionSummary()));
        actionLabel.setStyle(INFO_STYLE);

        Label storyHeading = new Label("生命故事");
        storyHeading.setStyle(STORY_HEADING_STYLE);

        Label storyLabel = new Label(entry.getFullStory());
        storyLabel.setStyle(STORY_STYLE);
        storyLabel.setWrapText(true);

        getChildren().addAll(nameLabel, typeQualityLabel, timeLabel,
                weatherLabel, eventLabel, actionLabel, storyHeading, storyLabel);
    }

    /**
     * 纯函数：信息行「标签：值」文本（验收规范 §一百二十四 信息字段格式）。
     *
     * @param label 字段标签
     * @param value 字段值
     * @return 「标签：值」
     */
    public static String infoLine(String label, String value) {
        return label + "：" + value;
    }
}
