package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.SeedQuickBuyController;
import com.fieldstory.farm.controller.ShopController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

/**
 * 商城弹窗，含“种子商店 / 装饰品商店”两个 Tab。
 *
 * <p>种子商店复用 {@link SeedQuickBuyView}；装饰品商店使用 {@link DecorationShopView}。
 * 不创建 Stage / Scene，符合项目约束。
 */
public class ShopPopupView extends Popup {

    private final SeedQuickBuyView seedShopView;
    private final DecorationShopView decorationShopView;
    private final StackPane contentPane;

    public ShopPopupView(SeedQuickBuyController seedController,
                         ShopController shopController) {
        this.seedShopView = new SeedQuickBuyView(seedController);
        this.decorationShopView = new DecorationShopView(shopController);
        this.contentPane = new StackPane(seedShopView);

        Button seedTab = createTabButton("种子商店");
        Button decorationTab = createTabButton("装饰品商店");

        seedTab.setOnAction(e -> showSeedShop());
        decorationTab.setOnAction(e -> showDecorationShop());

        HBox tabBar = new HBox(8, seedTab, decorationTab);
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setPadding(new Insets(8));

        VBox root = new VBox(8, tabBar, contentPane);
        root.setPadding(new Insets(12));
        root.setStyle(
                "-fx-background-color: #FFF3DD;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #795548;"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 2;"
        );
        root.setPrefWidth(360);
        root.setPrefHeight(480);

        getContent().add(root);
        setAutoHide(true);
        showSeedShop();
    }

    private Button createTabButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(32);
        button.setStyle(
                "-fx-background-color: #A97850;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #FFF3DD;"
                        + "-fx-font-size: 14;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #C28B5A;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #FFF3DD;"
                        + "-fx-font-size: 14;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #A97850;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #FFF3DD;"
                        + "-fx-font-size: 14;"
        ));
        return button;
    }

    private void showSeedShop() {
        contentPane.getChildren().setAll(seedShopView);
    }

    private void showDecorationShop() {
        decorationShopView.refresh();
        contentPane.getChildren().setAll(decorationShopView);
    }

    /** 在指定节点下方弹出商城。 */
    public void showBelow(Node owner) {
        if (isShowing()) {
            return;
        }
        var bounds = owner.localToScreen(owner.getBoundsInLocal());
        show(owner, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    /** 已显示则隐藏，否则在指定节点下方弹出。 */
    public void toggleBelow(Node owner) {
        if (isShowing()) {
            hide();
        } else {
            showBelow(owner);
        }
    }
}