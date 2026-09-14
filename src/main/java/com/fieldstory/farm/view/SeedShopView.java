package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.ShopController;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.economy.PurchaseResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** P1 完整商店中的种子页；底层仍由 ShopService 委托 EconomyService.buySeed。 */
public final class SeedShopView extends VBox {

    private final ShopController controller;
    private final Label goldLabel = new Label();
    private final Label messageLabel = new Label();
    private final Map<CropType, Label> inventoryLabels = new EnumMap<>(CropType.class);

    public SeedShopView(ShopController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
        setSpacing(10);
        setPadding(new Insets(12));
        setAlignment(Pos.TOP_LEFT);
        setPrefWidth(320);
        setStyle("-fx-background-color: #FFF3DD;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #8B5E3C;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 2;");

        Label title = new Label("种子商店");
        title.setStyle("-fx-text-fill: #493526; -fx-font-size: 18;");
        goldLabel.setStyle("-fx-text-fill: #493526; -fx-font-size: 14;");
        messageLabel.setStyle("-fx-text-fill: #493526; -fx-font-size: 12;");

        getChildren().addAll(title, goldLabel);
        for (CropType type : CropType.values()) {
            getChildren().add(row(type));
        }
        getChildren().add(messageLabel);
        refresh();
    }

    private HBox row(CropType type) {
        Label name = new Label(type.getDisplayName());
        name.setPrefWidth(100);
        Label price = new Label(type.getSeedPrice() + " 金币");
        price.setPrefWidth(80);
        Label count = new Label();
        count.setPrefWidth(70);
        inventoryLabels.put(type, count);

        Button buy = new Button("购买");
        buy.setPrefSize(70, 36);
        buy.setStyle("-fx-background-color: #A97850;"
                + "-fx-background-radius: 10;"
                + "-fx-text-fill: #FFF3DD;"
                + "-fx-font-size: 14;");
        buy.setOnAction(e -> {
            PurchaseResult result = controller.buySeed(type, 1);
            messageLabel.setText(ShopController.messageFor(result, type));
            refresh();
        });
        HBox row = new HBox(6, name, price, count, buy);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public void refresh() {
        goldLabel.setText("金币：" + controller.getGold());
        for (Map.Entry<CropType, Label> entry : inventoryLabels.entrySet()) {
            entry.getValue().setText("×" + controller.getSeedCount(entry.getKey()));
        }
    }
}
