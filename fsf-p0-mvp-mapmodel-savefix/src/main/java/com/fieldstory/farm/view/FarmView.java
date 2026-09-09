package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.FarmController;
import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FarmView {
    private final GameManager game;
    private final FarmController controller;
    private final BorderPane root = new BorderPane();

    private final Label goldLabel = new Label();
    private final Label dayLabel = new Label();
    private final Label timeLabel = new Label();
    private final Label inventoryLabel = new Label();
    private final Label worldAdvanceLabel = new Label();
    private final Label mapSummaryLabel = new Label();

    private final Label selectedLabel = new Label("未选择土地");
    private final Label progressTextLabel = new Label("成长推进进度：未选择作物");
    private final ProgressBar growthProgressBar = new ProgressBar(0.0);
    private final Label messageLabel = new Label();
    private final ComboBox<CropType> cropBox = new ComboBox<>();
    private final Map<Soil, Button> soilButtons = new HashMap<>();
    private Soil selectedSoil;

    public FarmView(GameManager game, FarmController controller) {
        this.game = game;
        this.controller = controller;
        build();
        refresh();
    }

    private void build() {
        VBox statusBox = new VBox(4,
                new HBox(20, goldLabel, dayLabel, timeLabel, inventoryLabel),
                new HBox(20, worldAdvanceLabel, mapSummaryLabel));
        statusBox.setPadding(new Insets(10));
        root.setTop(statusBox);

        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(10));

        /*
         * View不再自己判断“中心8×8”。
         * 12×12结构、格子类型以及是否拥有Soil全部来自Farm/MapTile Model。
         */
        for (MapTile tile : game.getFarm().getTiles()) {
            Node node = createTileNode(tile);
            grid.add(node, tile.getColumn(), tile.getRow());
        }
        root.setCenter(new ScrollPane(grid));

        cropBox.getItems().addAll(CropType.values());
        cropBox.setValue(CropType.WHEAT);

        Button buy = new Button("购买1颗种子");
        buy.setOnAction(e -> {
            messageLabel.setText(controller.buySeed(cropBox.getValue()));
            refresh();
        });

        Button reclaim = new Button("开垦");
        reclaim.setOnAction(e -> act(() -> controller.reclaim(selectedSoil)));

        Button plant = new Button("播种");
        plant.setOnAction(e -> act(() -> controller.plant(selectedSoil, cropBox.getValue())));

        Button water = new Button("浇水");
        water.setOnAction(e -> act(() -> controller.water(selectedSoil)));

        Button harvest = new Button("收获");
        harvest.setOnAction(e -> act(() -> controller.harvest(selectedSoil)));

        growthProgressBar.setPrefWidth(280);
        growthProgressBar.setProgress(0.0);

        Label wateringHint = new Label(
                "浇水规则：SEED不可浇；SPROUT/GROWING/MATURE可浇；每游戏日1次，单株最多5次。"
        );
        wateringHint.setWrapText(true);

        Label mapHint = new Label(
                "P0地图：12×12全部来自MapTile Model；只有FARM_PLOT持有Soil。外围功能区可见但不可操作。"
        );
        mapHint.setWrapText(true);

        Label clockHint = new Label(
                "世界时间只由统一世界推进循环改变；购买/开垦/播种/浇水/收获/保存/刷新均不会推进时钟。"
        );
        clockHint.setWrapText(true);

        VBox side = new VBox(10,
                new Label("P0 操作"), cropBox, buy,
                new Separator(), selectedLabel,
                progressTextLabel, growthProgressBar,
                reclaim, plant, water, harvest,
                new Separator(), wateringHint, mapHint, clockHint, messageLabel);
        side.setPadding(new Insets(10));
        side.setPrefWidth(360);
        root.setRight(side);
    }

    private Node createTileNode(MapTile tile) {
        if (tile.getPlotType() == FarmPlot.FARM_PLOT && tile.getSoil() != null) {
            Soil soil = tile.getSoil();
            Button button = new Button();
            button.setPrefSize(82, 54);
            button.setOnAction(e -> {
                selectedSoil = soil;
                refresh();
            });
            soilButtons.put(soil, button);
            return button;
        }

        Label placeholder = new Label(outerTileText(tile.getPlotType()));
        placeholder.setMinSize(82, 54);
        placeholder.setStyle("-fx-alignment:center; -fx-background-color:#eeeeee;");
        return placeholder;
    }

    private String outerTileText(FarmPlot type) {
        return switch (type) {
            case DECORATION_AREA -> "功能区\nP0占位";
            case SHOP -> "商店\nP0占位";
            case SHOWCASE -> "展示台\nP0占位";
            case FARM_PLOT -> "农田";
        };
    }

    private void act(java.util.function.Supplier<String> action) {
        if (selectedSoil == null) {
            messageLabel.setText("请先选择FARM_PLOT土地");
        } else {
            messageLabel.setText(action.get());
        }
        refresh();
    }

    /** 只读取并显示已经提交的状态，绝不推进世界。 */
    public void refresh() {
        LocalDateTime now = game.getClock().now();
        LocalDate currentGameDay = now.toLocalDate();

        goldLabel.setText("金币: " + game.getPlayer().getGold());
        dayLabel.setText("游戏日: " + currentGameDay);
        timeLabel.setText("游戏时间: " + now.toLocalTime().withNano(0));
        inventoryLabel.setText("种子 W:" + game.getEconomyService().getSeedCount(CropType.WHEAT)
                + " C:" + game.getEconomyService().getSeedCount(CropType.CORN)
                + " R:" + game.getEconomyService().getSeedCount(CropType.CARROT));
        worldAdvanceLabel.setText("世界推进: " + game.getWorldAdvanceCount()
                + " 次 | 最近一次 +" + game.getLastAdvanceGameMinutes() + " 游戏分钟");
        mapSummaryLabel.setText("地图: " + game.getFarm().getTiles().size()
                + "格 | 农田:" + game.getFarm().countTiles(FarmPlot.FARM_PLOT)
                + " | 功能区:" + (game.getFarm().getTiles().size()
                - game.getFarm().countTiles(FarmPlot.FARM_PLOT)));

        for (Map.Entry<Soil, Button> e : soilButtons.entrySet()) {
            Soil soil = e.getKey();
            Button button = e.getValue();

            String text = soil.getState().name();
            if (soil.getCrop() != null) {
                Crop crop = soil.getCrop();
                text = crop.getCropType().name()
                        + "\n" + stageText(crop.getGrowthStage())
                        + String.format(" %.0f%%", crop.getGrowthProgress());
            }
            button.setText(text);
        }

        refreshSelectedInfo(currentGameDay);
    }

    private void refreshSelectedInfo(LocalDate currentGameDay) {
        if (selectedSoil == null) {
            selectedLabel.setText("未选择土地");
            progressTextLabel.setText("成长推进进度：未选择作物");
            growthProgressBar.setProgress(0.0);
            return;
        }

        if (selectedSoil.getCrop() == null) {
            selectedLabel.setText("土地: " + selectedSoil.getState());
            progressTextLabel.setText("成长推进进度：当前土地无作物");
            growthProgressBar.setProgress(0.0);
            return;
        }

        Crop crop = selectedSoil.getCrop();
        boolean wateredToday = currentGameDay.equals(crop.getLastManualWaterGameDay());
        long remainingHours = game.getGrowthService().estimateRemainingGameHours(crop);
        String maturity = crop.getGrowthStage() == GrowthStage.MATURE
                ? "已成熟，可收获"
                : "约 " + remainingHours + " 游戏小时";

        selectedLabel.setText("土地: " + selectedSoil.getState()
                + "\n作物: " + crop.getCropType()
                + "\n阶段: " + stageText(crop.getGrowthStage()) + " (" + crop.getGrowthStage() + ")"
                + String.format("\n成长: %.1f%%", crop.getGrowthProgress())
                + "\n主动浇水: " + crop.getManualWaterCount() + "/5"
                + "\n今日已浇水: " + (wateredToday ? "是" : "否")
                + "\n预计成熟: " + maturity);

        double fraction = Math.max(0.0, Math.min(1.0, crop.getGrowthProgress() / 100.0));
        growthProgressBar.setProgress(fraction);
        progressTextLabel.setText(String.format(
                "成长推进进度：%.1f%% | %s → MATURE",
                crop.getGrowthProgress(), stageText(crop.getGrowthStage())
        ));
    }

    private String stageText(GrowthStage stage) {
        return switch (stage) {
            case SEED -> "种子";
            case SPROUT -> "发芽";
            case GROWING -> "成长";
            case MATURE -> "成熟";
            case WITHERED -> "枯萎";
        };
    }

    public Parent root() {
        return root;
    }
}
