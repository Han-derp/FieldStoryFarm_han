package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.ShopController;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPurchaseResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * B 模块 P1 装饰商店内容视图。
 */
public final class DecorationShopView extends VBox {

    private final ShopController controller;

    private final Label goldLabel =
            new Label();

    private final Label messageLabel =
            new Label();

    private final VBox listBox =
            new VBox(6);

    public DecorationShopView(
            ShopController controller) {

        this.controller =
                Objects.requireNonNull(
                        controller,
                        "controller"
                );

        setSpacing(10);
        setPadding(
                new Insets(12)
        );

        setAlignment(
                Pos.TOP_LEFT
        );

        setPrefWidth(360);
        setMinWidth(360);

        setStyle(
                "-fx-background-color: #FFF3DD;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #8B5E3C;"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 2;"
        );

        Label title =
                new Label(
                        "装饰品商店"
                );

        title.setStyle(
                "-fx-text-fill: #493526;"
                        + "-fx-font-size: 18;"
        );

        goldLabel.setStyle(
                "-fx-text-fill: #493526;"
                        + "-fx-font-size: 14;"
        );

        messageLabel.setStyle(
                "-fx-text-fill: #493526;"
                        + "-fx-font-size: 12;"
        );

        messageLabel.setWrapText(
                true
        );

        for (DecorationType type :
                DecorationType.values()) {

            listBox.getChildren()
                    .add(
                            createRow(type)
                    );
        }

        ScrollPane scroll =
                new ScrollPane(
                        listBox
                );

        scroll.setFitToWidth(
                true
        );

        scroll.setPrefHeight(
                340
        );

        scroll.setStyle(
                "-fx-background-color: transparent;"
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        getChildren().addAll(
                title,
                goldLabel,
                scroll,
                messageLabel
        );

        refresh();
    }

    private HBox createRow(
            DecorationType type) {

        Node icon =
                decorationIcon(type);

        VBox info =
                new VBox(2);

        Label name =
                new Label(
                        type.getDisplayName()
                );

        name.setStyle(
                "-fx-text-fill: #493526;"
                        + "-fx-font-size: 14;"
        );

        Label price =
                new Label(
                        type.getPrice()
                                + " 金币"
                );

        price.setStyle(
                "-fx-text-fill: #795548;"
                        + "-fx-font-size: 12;"
        );

        info.getChildren().addAll(
                name,
                price
        );

        HBox.setHgrow(
                info,
                Priority.ALWAYS
        );

        Button buy =
                new Button("购买");

        buy.setPrefSize(
                72,
                36
        );

        applyButtonStyle(
                buy,
                false
        );

        buy.setOnMouseEntered(
                e -> applyButtonStyle(
                        buy,
                        true
                )
        );

        buy.setOnMouseExited(
                e -> applyButtonStyle(
                        buy,
                        false
                )
        );

        buy.setOnAction(
                e -> {

                    DecorationPurchaseResult result =
                            controller.buyDecoration(
                                    type,
                                    1
                            );

                    messageLabel.setText(
                            ShopController.messageFor(
                                    result,
                                    type
                            )
                    );

                    refresh();
                }
        );

        HBox row =
                new HBox(
                        8,
                        icon,
                        info,
                        buy
                );

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(4)
        );

        return row;
    }

    private Node decorationIcon(
            DecorationType type) {

        String path =
                "/assets/decoration/"
                        + type.getAssetFileName();

        ImageView view =
                BImageAssets.view(
                        path,
                        48,
                        48
                );

        if (view != null) {
            return view;
        }

        Label placeholder =
                new Label("?");

        placeholder.setPrefSize(
                48,
                48
        );

        placeholder.setAlignment(
                Pos.CENTER
        );

        placeholder.setStyle(
                "-fx-background-color: #FFF3DD;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #795548;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #795548;"
        );

        return placeholder;
    }

    private void applyButtonStyle(
            Button button,
            boolean hover) {

        button.setStyle(
                "-fx-background-color: "
                        + (
                        hover
                                ? "#C28B5A"
                                : "#A97850"
                )
                        + ";"
                        + "-fx-background-radius: 10;"
                        + "-fx-text-fill: #FFF3DD;"
                        + "-fx-font-size: 14;"
        );
    }

    public void refresh() {

        goldLabel.setText(
                "金币："
                        + controller.getGold()
        );
    }

    public String getMessageText() {

        return messageLabel.getText();
    }
}