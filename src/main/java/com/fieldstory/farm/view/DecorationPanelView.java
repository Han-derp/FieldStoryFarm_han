package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.DecorationController;
import com.fieldstory.farm.model.Decoration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 装饰库存与选择面板（挂 LEFT）。
 *
 * <p>列出已拥有的装饰与放置状态；点击行可选中，选中后由外部放置回调处理。
 * 收回按钮直接调用 DecorationController.removeFromFarm。
 */
public final class DecorationPanelView extends VBox {

    private static final double PANEL_WIDTH = 200.0;
    private static final double BUTTON_WIDTH = 60.0;
    private static final double BUTTON_HEIGHT = 28.0;

    private static final String PANEL_STYLE =
            "-fx-background-color: #FFF3DD;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #795548;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 2;";

    private static final String TEXT_STYLE =
            "-fx-text-fill: #493526; -fx-font-size: 13;";

    private static final String TITLE_STYLE =
            "-fx-text-fill: #493526; -fx-font-size: 16;";

    private static final String BUTTON_STYLE =
            "-fx-background-color: #A97850;"
                    + "-fx-background-radius: 8;"
                    + "-fx-text-fill: #FFF3DD;"
                    + "-fx-font-size: 12;";

    private final DecorationController controller;
    private final Label messageLabel = new Label();
    private final VBox listBox = new VBox(4);

    /** 行点击回调：参数为 (decoration, isPlaced)，由外部决定选中/放置逻辑。 */
    private BiConsumer<Decoration, Boolean> onDecorationSelected = (d, p) -> {};

    public DecorationPanelView(DecorationController controller) {
        this.controller = Objects.requireNonNull(controller);
        configurePanel();
        buildContent();
        refresh();
    }

    private void configurePanel() {
        setSpacing(8);
        setPadding(new Insets(10));
        setAlignment(Pos.TOP_LEFT);
        setPrefWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setStyle(PANEL_STYLE);
    }

    private void buildContent() {
        Label title = new Label("我的装饰");
        title.setStyle(TITLE_STYLE);
        messageLabel.setStyle(TEXT_STYLE);
        messageLabel.setWrapText(true);

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, scroll, messageLabel);
    }

    /** 重新读取 DecorationService 状态刷新列表。 */
    public void refresh() {
        listBox.getChildren().clear();
        List<Decoration> owned = controller.getOwnedDecorations();
        if (owned.isEmpty()) {
            Label empty = new Label("（还没有装饰）");
            empty.setStyle(TEXT_STYLE);
            listBox.getChildren().add(empty);
            return;
        }
        for (Decoration d : owned) {
            listBox.getChildren().add(createRow(d));
        }
    }

    private HBox createRow(Decoration decoration) {
        boolean placed = decoration.isPlaced();
        Label nameLabel = new Label(
                decoration.getDecorationType().getDisplayName()
                        + (placed ? "（已放置）" : "（未放置）"));
        nameLabel.setStyle(TEXT_STYLE);
        nameLabel.setPrefWidth(120);

        Button actionButton = new Button(placed ? "收回" : "选择");
        actionButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        actionButton.setStyle(BUTTON_STYLE);

        actionButton.setOnAction(e -> {
            if (placed) {
                controller.removeFromFarm(decoration);
                messageLabel.setText("已收回" + decoration.getDecorationType().getDisplayName());
                refresh();
            } else {
                onDecorationSelected.accept(decoration, false);
                messageLabel.setText("已选择，请点击装饰区放置");
            }
        });

        HBox row = new HBox(4, nameLabel, actionButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** 注册行点击回调（未放置装饰被选择时触发）。 */
    public void setOnDecorationSelected(BiConsumer<Decoration, Boolean> callback) {
        if (callback != null) {
            this.onDecorationSelected = callback;
        }
    }

    public String getMessageText() {
        return messageLabel.getText();
    }
}