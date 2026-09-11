package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.DecorationController;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 仓库弹窗：显示拥有的装饰列表与状态（未放置 / 已放置）。
 *
 * <p>未放置 → 提供"选择"按钮，选中后关闭弹窗，由外部处理装饰区点击放置。
 * 已放置 → 提供"收回"按钮，调用 {@link DecorationController#removeFromFarm}。
 */
public class WarehousePopupView extends Popup {

    private static final double ICON_SIZE = 32.0;

    private final DecorationController controller;
    private final Consumer<Decoration> onSelect;
    private final Label messageLabel = new Label();
    private final VBox listBox = new VBox(6);

    public WarehousePopupView(DecorationController controller,
                              Consumer<Decoration> onSelect) {
        this.controller = Objects.requireNonNull(controller);
        this.onSelect = Objects.requireNonNull(onSelect);

        Label title = new Label("我的仓库");
        title.setStyle("-fx-text-fill: #493526; -fx-font-size: 18;");

        messageLabel.setStyle("-fx-text-fill: #493526; -fx-font-size: 12;");
        messageLabel.setWrapText(true);

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox root = new VBox(8, title, scroll, messageLabel);
        root.setPadding(new Insets(12));
        root.setStyle(
                "-fx-background-color: #FFF3DD;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #795548;"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 2;"
        );
        root.setPrefWidth(300);
        root.setPrefHeight(440);

        getContent().add(root);
        setAutoHide(true);

        refresh();
    }

    /** 从 DecorationController 重新读取状态，重建列表。 */
    public void refresh() {
        listBox.getChildren().clear();
        List<Decoration> owned = controller.getOwnedDecorations();
        if (owned.isEmpty()) {
            Label empty = new Label("（仓库暂无装饰）");
            empty.setStyle("-fx-text-fill: #493526; -fx-font-size: 13;");
            listBox.getChildren().add(empty);
            return;
        }
        for (Decoration d : owned) {
            listBox.getChildren().add(createRow(d));
        }
    }

    private HBox createRow(Decoration decoration) {
        DecorationType type = decoration.getDecorationType();
        boolean placed = decoration.isPlaced();

        Node iconNode = buildIcon(type);

        Label nameLabel = new Label(
                type.getDisplayName() + (placed ? "（已放置）" : "（未放置）"));
        nameLabel.setStyle("-fx-text-fill: #493526; -fx-font-size: 13;");
        nameLabel.setPrefWidth(150);

        Button action = new Button(placed ? "收回" : "选择");
        action.setPrefSize(60, 28);
        action.setStyle(
                "-fx-background-color: #A97850;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #FFF3DD;"
                        + "-fx-font-size: 12;"
        );

        if (placed) {
            action.setOnAction(e -> {
                controller.removeFromFarm(decoration);
                messageLabel.setText("已收回" + type.getDisplayName());
                refresh();
            });
        } else {
            action.setOnAction(e -> {
                onSelect.accept(decoration);
                messageLabel.setText("已选择" + type.getDisplayName() + "，请点击装饰区放置");
                hide();
            });
        }

        HBox row = new HBox(6, iconNode, nameLabel, action);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildIcon(DecorationType type) {
        Image icon = loadIcon(type);
        if (icon != null) {
            ImageView iv = new ImageView(icon);
            iv.setFitWidth(ICON_SIZE);
            iv.setFitHeight(ICON_SIZE);
            iv.setSmooth(false);
            return iv;
        }
        Label placeholder = new Label("?");
        placeholder.setStyle("-fx-text-fill: #A97850; -fx-font-size: 16;");
        placeholder.setPrefSize(ICON_SIZE, ICON_SIZE);
        placeholder.setAlignment(Pos.CENTER);
        return placeholder;
    }

    private static Image loadIcon(DecorationType type) {
        String path = "/assets/decoration/" + type.name().toLowerCase(Locale.ROOT) + ".png";
        try (InputStream in = WarehousePopupView.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            Image img = new Image(in);
            return img.isError() ? null : img;
        } catch (Exception ex) {
            return null;
        }
    }

    /** 在指定节点下方弹出仓库。 */
    public void showBelow(Node owner) {
        if (isShowing()) {
            hide();
            return;
        }
        refresh();
        var bounds = owner.localToScreen(owner.getBoundsInLocal());
        show(owner, bounds.getMinX() - 220, bounds.getMaxY() + 4);
    }

    public void toggleBelow(Node owner) {
        if (isShowing()) {
            hide();
        } else {
            showBelow(owner);
        }
    }
}