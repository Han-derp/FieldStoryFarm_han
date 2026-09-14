package com.fieldstory.farm.view;

import com.fieldstory.farm.service.ShowcaseEntry;
import com.fieldstory.farm.service.ShowcaseService;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 展示台面板视图（C 模块 品质与传说域，P3；分级文档 P3 对应类）。
 *
 * <p>展示已收获的传说作物历史记录（验收规范 §一百二十三~一百二十四）：
 * 左侧选择器列出全部传说记忆，右侧 {@link MemoryCardView} 渲染选中条目
 * 的完整信息与生命故事；尚未收获任何传说时显示占位提示。
 *
 * <p>View 职责（脚手架 §七.5：只负责显示与输入交互）：本视图只调用
 * {@link ShowcaseService} 的查询方法读取条目，不写任何 Model/Service
 * 状态。颜色只用 UI规范 §14 主色表 7 色（面板 UI背景/木色边框/文字色），
 * 字号按 UI规范 §15。
 */
public class ShowcaseView extends VBox {

    /** 面板样式：UI背景 #FFF3DD + 木色 2px 边框 + 圆角 12（UI规范 §14、§16） */
    private static final String PANEL_STYLE =
            "-fx-background-color: #FFF3DD;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #8B5E3C;"
                    + "-fx-border-width: 2;"
                    + "-fx-border-radius: 12;";

    /** 面板标题：文字 #493526、18 号加粗（UI规范 §14 文字、§15 模块标题） */
    private static final String TITLE_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 18;"
                    + "-fx-font-weight: bold;";

    /** 普通文字：文字 #493526、14 号（UI规范 §14、§15） */
    private static final String TEXT_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 14;";

    /** 提示文字：文字 #493526、12 号（UI规范 §14、§15 提示） */
    private static final String TIP_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 12;";

    /** 选择器宽度（需容纳「传说名 · 作物」完整文案，宽于 UI规范 §13 按钮 120px） */
    private static final double SELECTOR_WIDTH = 220;

    /** 详情区滚动视口高度（展示台面板位于底部槽位，空间有限） */
    private static final double DETAIL_VIEWPORT_HEIGHT = 130;

    /** 数据源（只读查询） */
    private final ShowcaseService showcaseService;

    /** 传说记忆选择器 */
    private final ComboBox<String> selector = new ComboBox<>();

    /** 计数标签（"已收录传说记忆：N 株"） */
    private final Label countLabel = new Label();

    /** 空态占位提示 */
    private final Label emptyLabel = new Label();

    /** 详情滚动容器（包裹 MemoryCardView） */
    private final ScrollPane detailScroll = new ScrollPane();

    /** 当前条目列表（与选择器下标一一对应） */
    private final List<ShowcaseEntry> entries = new ArrayList<>();

    /**
     * 注入展示台服务，构建面板并完成首轮刷新。
     *
     * @param showcaseService 展示台服务（只读查询）
     */
    public ShowcaseView(ShowcaseService showcaseService) {
        this.showcaseService = Objects.requireNonNull(showcaseService, "展示台服务不能为空");
        setSpacing(6);
        setPadding(new Insets(10));
        setStyle(PANEL_STYLE);

        Label title = new Label("展示台");
        title.setStyle(TITLE_STYLE);

        countLabel.setStyle(TEXT_STYLE);
        emptyLabel.setStyle(TIP_STYLE);
        emptyLabel.setWrapText(true);

        selector.setPrefWidth(SELECTOR_WIDTH);
        selector.setStyle("-fx-font-size: 14;");
        selector.setOnAction(event -> renderSelected());

        detailScroll.setPrefViewportHeight(DETAIL_VIEWPORT_HEIGHT);
        detailScroll.setFitToWidth(true);
        detailScroll.setStyle("-fx-background-color: #FFF3DD;");
        HBox.setHgrow(detailScroll, Priority.ALWAYS);

        HBox content = new HBox(10, selector, detailScroll);
        getChildren().addAll(title, countLabel, content, emptyLabel);
        refresh();
    }

    /**
     * 重新从服务读取全部传说条目并刷新界面（收获新传说后由
     * Controller 调用）。
     */
    public void refresh() {
        entries.clear();
        entries.addAll(showcaseService.listLegendaryEntries());

        selector.getItems().clear();
        for (ShowcaseEntry entry : entries) {
            selector.getItems().add(selectorLabelFor(entry));
        }

        boolean hasLegendary = !entries.isEmpty();
        countLabel.setText("已收录传说记忆：" + entries.size() + " 株");
        selector.setVisible(hasLegendary);
        selector.setManaged(hasLegendary);
        detailScroll.setVisible(hasLegendary);
        detailScroll.setManaged(hasLegendary);
        emptyLabel.setVisible(!hasLegendary);
        emptyLabel.setManaged(!hasLegendary);
        if (!hasLegendary) {
            emptyLabel.setText("展示台空空如也——传说作物收获后，"
                    + "这里将陈列它的完整生命故事。");
            return;
        }
        selector.getSelectionModel().selectFirst();
        renderSelected();
    }

    /** 按当前选择下标渲染记忆卡片。 */
    private void renderSelected() {
        int index = selector.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= entries.size()) {
            return;
        }
        detailScroll.setContent(new MemoryCardView(entries.get(index)));
    }

    /**
     * 纯函数：选择器条目文案「传说名 · 作物」。
     *
     * @param entry 展示条目
     * @return 选择器显示文本
     */
    public static String selectorLabelFor(ShowcaseEntry entry) {
        Objects.requireNonNull(entry, "展示条目不能为空");
        return entry.getLegendaryName() + " · " + entry.getCropType().getDisplayName();
    }
}
