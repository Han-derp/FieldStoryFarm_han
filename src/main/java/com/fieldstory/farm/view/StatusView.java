package com.fieldstory.farm.view;

import com.fieldstory.farm.model.FarmGameModel;
import com.fieldstory.farm.model.GameClock;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.WeatherState;
import com.fieldstory.farm.model.WeatherType;
import com.fieldstory.farm.service.WeatherService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * 顶部状态栏视图。
 *
 * <p>右上角两个按钮：📦 仓库（左）、🏪 商城（右）。
 */
public class StatusView extends HBox {

    private static final String WAREHOUSE_ICON_PATH = "/assets/icon/warehouse_button.png";
    private static final String SHOP_ICON_PATH = "/assets/icon/shop_button.png";
    private static final double ICON_SIZE = 72.0;

    private final FarmGameModel model;
    private final WeatherService weatherService;
    private final WeatherState weatherState;
    private final Player player;

    private final Label dayLabel;
    private final Label timeLabel;
    private final Label goldLabel;
    private final Label weatherLabel;

    private final Button warehouseButton;
    private final Button shopButton;

    public StatusView(FarmGameModel model) {
        this(model, null, null);
    }

    public StatusView(FarmGameModel model, Player player) {
        this(model, null, null, player);
    }

    public StatusView(FarmGameModel model, WeatherService weatherService, WeatherState weatherState) {
        this(model, weatherService, weatherState, null);
    }

    public StatusView(FarmGameModel model, WeatherService weatherService,
                      WeatherState weatherState, Player player) {
        this.model = model;
        this.weatherService = weatherService;
        this.weatherState = weatherState;
        this.player = player;

        this.dayLabel = new Label();
        this.timeLabel = new Label();
        this.goldLabel = new Label();
        this.weatherLabel = new Label();
        this.warehouseButton = createIconButton("📦", WAREHOUSE_ICON_PATH);
        this.shopButton = createIconButton("🏪", SHOP_ICON_PATH);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.setSpacing(12);
        this.getChildren().addAll(
                dayLabel, timeLabel, goldLabel, weatherLabel,
                spacer, warehouseButton, shopButton);

        update();
    }

    /**
     * 图标按钮：优先加载贴图；缺失时降级为 emoji 文字按钮。
     */
    private Button createIconButton(String fallbackText, String iconPath) {
        Button button = new Button();
        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent;"
                        + "-fx-padding: 0;"
                        + "-fx-cursor: hand;"
        );
        button.setMinSize(ICON_SIZE, ICON_SIZE);
        button.setPrefSize(ICON_SIZE, ICON_SIZE);
        button.setMaxSize(ICON_SIZE, ICON_SIZE);

        javafx.scene.image.Image icon = loadIcon(iconPath);
        if (icon != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(icon);
            iv.setFitWidth(ICON_SIZE);
            iv.setFitHeight(ICON_SIZE);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);
            button.setGraphic(iv);
        } else {
            button.setText(fallbackText);
            button.setStyle(
                    "-fx-background-color: #A97850;"
                            + "-fx-background-radius: 16;"
                            + "-fx-text-fill: #FFF3DD;"
                            + "-fx-font-size: 32;"
                            + "-fx-min-width: 72;"
                            + "-fx-min-height: 72;"
            );
        }

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.08);
            button.setScaleY(1.08);
            button.setOpacity(0.85);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setOpacity(1.0);
        });
        return button;
    }

    private static javafx.scene.image.Image loadIcon(String path) {
        try (var in = StatusView.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            javafx.scene.image.Image img = new javafx.scene.image.Image(in);
            return img.isError() ? null : img;
        } catch (Exception ex) {
            return null;
        }
    }

    public void update() {
        GameClock clock = model.getGameClock();
        dayLabel.setText("第 " + clock.getGameDay() + " 天");
        timeLabel.setText(getDaytimeIcon() + " " + clock.getTimeString());
        goldLabel.setText(player == null ? "金币 --" : "金币 " + player.getGold());
        weatherLabel.setText(buildWeatherText());
    }

    private String buildWeatherText() {
        if (weatherService == null || weatherState == null) {
            return "晴天";
        }
        WeatherType type = weatherState.getWeatherType();
        if (type == null) {
            return "晴天";
        }
        return weatherService.getIcon(type) + " " + weatherService.getDisplayName(type);
    }

    private String getDaytimeIcon() {
        return model.getGameClock().isDaytime() ? "\u2600" : "\uD83C\uDF19";
    }

    public String getDayText() { return dayLabel.getText(); }
    public String getTimeText() { return timeLabel.getText(); }
    public String getGoldText() { return goldLabel.getText(); }
    public String getWeatherText() { return weatherLabel.getText(); }

    public void setOnWarehouseButtonClick(Runnable action) {
        warehouseButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public Button getWarehouseButton() { return warehouseButton; }

    public void setOnShopButtonClick(Runnable action) {
        shopButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public Button getShopButton() { return shopButton; }
}