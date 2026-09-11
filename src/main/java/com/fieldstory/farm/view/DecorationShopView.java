package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.ShopController;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * 装饰商店视图（弹窗内“装饰品商店”Tab 内容）。
 *
 * <p>列出 14 种装饰与价格，点击购买。价格只读 DecorationType.getPrice()。
 */
public final class DecorationShopView extends VBox {

    private static final double PANEL_WIDTH = 320.0;
    private static final double BUTTON_WIDTH = 90.0;
    private static final double BUTTON_HEIGHT = 32.0;

    private static final String PANEL_STYLE =
            "-fx-background-color: #FFF3DD;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #795548;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 2;";

    private static final String TEXT_STYLE =
            "-fx-text-fill: #493526; -fx-font-size: 14;";

    private static final String TITLE_STYLE =
            "-fx-text-fill: #493526; -fx-font-size: 18;";

    private static final String TIP_STYLE =
            "-fx-text-fill: #493526; -fx-font-size: 12;";

    private static final String BUTTON_NORMAL_STYLE =
            "-fx-background-color: #A97850;"
                    + "-fx-background-radius: 10;"
                    + "-fx-text-fill: #FFF3DD;"
                    + "-fx-font-size: 14;";

    private static final String BUTTON_HOVER_STYLE =
            "-fx-background-color: #C28B5A;"
                    + "-fx-background-radius: 10;"
                    + "-fx-text-fill: #FFF3DD;"
                    + "-fx-font-size: 14;";

    private final ShopController controller;
    private final Label goldLabel = new Label();
    private final Label messageLabel = new Label();
    private final VBox listBox = new VBox(6);

    public DecorationShopView(ShopController controller) {
        this.controller = Objects.requireNonNull(controller);
        configurePanel();
        buildContent();
        refresh();
    }

    private void configurePanel() {
        setSpacing(10);
        setPadding(new Insets(12));
        setAlignment(Pos.TOP_LEFT);
        setPrefWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setStyle(PANEL_STYLE);
    }

    private void buildContent() {
        Label title = new Label("装饰品商店");
        title.setStyle(TITLE_STYLE);

        goldLabel.setStyle(TEXT_STYLE);
        messageLabel.setStyle(TIP_STYLE);
        messageLabel.setWrapText(true);

        getChildren().addAll(title, goldLabel);

        for (DecorationType type : DecorationType.values()) {
            listBox.getChildren().add(createItemRow(type));
        }

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(280);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(scroll, messageLabel);
    }

    private HBox createItemRow(DecorationType type) {
        Label nameLabel = new Label(type.getDisplayName());
        nameLabel.setStyle(TEXT_STYLE);
        nameLabel.setPrefWidth(120);

        Label priceLabel = new Label(type.getPrice() + " 金币");
        priceLabel.setStyle(TEXT_STYLE);
        priceLabel.setPrefWidth(80);

        Button buy = new Button("购买");
        configureButton(buy);
        buy.setOnAction(e -> buy(type));

        HBox row = new HBox(6, nameLabel, priceLabel, buy);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void configureButton(Button button) {
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setStyle(BUTTON_NORMAL_STYLE);
        button.setOnMouseEntered(e -> button.setStyle(BUTTON_HOVER_STYLE));
        button.setOnMouseExited(e -> button.setStyle(BUTTON_NORMAL_STYLE));
    }

    private void buy(DecorationType type) {
        DecorationPurchaseResult result = controller.buyDecoration(type, 1);
        messageLabel.setText(ShopController.messageFor(result, type));
        refresh();
    }

    /** 重新读取金币显示。 */
    public void refresh() {
        goldLabel.setText("金币：" + controller.getGold());
    }

    public String getMessageText() {
        return messageLabel.getText();
    }
}