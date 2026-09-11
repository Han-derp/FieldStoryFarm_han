package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.SeedQuickBuyController;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.economy.PurchaseResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * B 模块种子商店视图（弹窗内的“种子商店”）。
 */
public final class SeedQuickBuyView extends VBox {

    private static final double PANEL_WIDTH = 180.0;
    private static final double BUTTON_WIDTH = 120.0;
    private static final double BUTTON_HEIGHT = 36.0;

    private static final String PANEL_STYLE =
            "-fx-background-color: #FFF3DD;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #795548;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 2;";

    private static final String TEXT_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 14;";

    private static final String TITLE_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 18;";

    private static final String TIP_STYLE =
            "-fx-text-fill: #493526;"
                    + "-fx-font-size: 12;";

    private static final String BUTTON_NORMAL_STYLE =
            "-fx-background-color: #A97850;"
                    + "-fx-background-radius: 10;"
                    + "-fx-text-fill: #FFF3DD;"
                    + "-fx-font-size: 16;";

    private static final String BUTTON_HOVER_STYLE =
            "-fx-background-color: #C28B5A;"
                    + "-fx-background-radius: 10;"
                    + "-fx-text-fill: #FFF3DD;"
                    + "-fx-font-size: 16;";

    private final SeedQuickBuyController controller;

    private final Label goldLabel = new Label();
    private final Label messageLabel = new Label();
    private final Map<CropType, Label> seedCountLabels = new EnumMap<>(CropType.class);

    public SeedQuickBuyView(SeedQuickBuyController controller) {
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
        configurePanel();
        buildContent();
        refresh();
    }

    private void configurePanel() {
        setSpacing(10);
        setPadding(new Insets(12));
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setStyle(PANEL_STYLE);
    }

    private void buildContent() {
        Label title = new Label("种子商店");
        title.setStyle(TITLE_STYLE);

        goldLabel.setStyle(TEXT_STYLE);
        messageLabel.setStyle(TIP_STYLE);
        messageLabel.setWrapText(true);

        getChildren().add(title);
        getChildren().add(goldLabel);

        for (CropType type : CropType.values()) {
            getChildren().add(createSeedSection(type));
        }

        getChildren().add(messageLabel);
    }

    private VBox createSeedSection(CropType type) {
        Label nameLabel = new Label(type.getDisplayName() + "种子");
        nameLabel.setStyle(TEXT_STYLE);

        Label countLabel = new Label();
        countLabel.setStyle(TEXT_STYLE);
        seedCountLabels.put(type, countLabel);

        Button buyButton = new Button("购买 " + type.getSeedPrice() + "金币");
        configureButton(buyButton);
        buyButton.setOnAction(event -> buy(type));

        VBox box = new VBox(4, nameLabel, countLabel, buyButton);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void configureButton(Button button) {
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setStyle(BUTTON_NORMAL_STYLE);
        button.setOnMouseEntered(event -> button.setStyle(BUTTON_HOVER_STYLE));
        button.setOnMouseExited(event -> button.setStyle(BUTTON_NORMAL_STYLE));
        button.setOnMousePressed(event -> button.setStyle(BUTTON_HOVER_STYLE));
        button.setOnMouseReleased(event -> button.setStyle(BUTTON_NORMAL_STYLE));
    }

    private void buy(CropType type) {
        PurchaseResult result = controller.buyOneSeed(type);
        messageLabel.setText(SeedQuickBuyController.messageFor(result, type));
        refresh();
    }

    public void refresh() {
        goldLabel.setText("金币：" + controller.getGold());
        for (CropType type : CropType.values()) {
            Label label = seedCountLabels.get(type);
            if (label != null) {
                label.setText("库存：" + controller.getSeedCount(type));
            }
        }
    }

    public String getGoldText() {
        return goldLabel.getText();
    }

    public String getMessageText() {
        return messageLabel.getText();
    }
}