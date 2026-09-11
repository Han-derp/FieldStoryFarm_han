package com.fieldstory.farm.view;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmPlot;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 农场主画布（A 模块 P0 视图层；P1 增加装饰渲染与装饰区点击回调）。
 *
 * <p>P1 修复：
 * <ul>
 *   <li>tile 点击改用 {@code setOnMousePressed}，避免 Tooltip 悬停窗口抢占点击；</li>
 *   <li>{@code selectionRect} 初始不可见且不拦截鼠标；</li>
 *   <li>装饰层与作物块 {@code setMouseTransparent(true)}，不吞点击。</li>
 * </ul>
 */
public class FarmView extends Pane {

    public static final int MAP_SIZE = 12;
    public static final int TILE_SIZE = 44;
    public static final int MAP_PX = MAP_SIZE * TILE_SIZE;

    public static final Color COLOR_GRASS = Color.rgb(0x7F, 0xAE, 0x55);
    public static final Color COLOR_SOIL = Color.rgb(0xA9, 0x78, 0x50);
    public static final Color COLOR_UI_BG = Color.rgb(0xFF, 0xF3, 0xDD);
    public static final Color COLOR_WOOD = Color.rgb(0x8B, 0x5E, 0x3C);
    public static final Color COLOR_TEXT = Color.rgb(0x49, 0x35, 0x26);
    public static final Color COLOR_HIGHLIGHT = Color.rgb(0xE8, 0xC4, 0x5C);
    public static final Color COLOR_BTN_HOVER = Color.rgb(0xC2, 0x8B, 0x5A);
    public static final Color COLOR_BTN_DISABLED = Color.rgb(0xCC, 0xCC, 0xCC);

    private static final double BUTTON_WIDTH = 120;
    private static final double BUTTON_HEIGHT = 36;
    private static final double MENU_RADIUS = 12;
    private static final double MENU_GAP = 6;
    private static final Insets MENU_PADDING = new Insets(8);

    private static final String STYLE_BTN_NORMAL = "-fx-background-color: #A97850;"
            + "-fx-background-radius: 10;"
            + "-fx-text-fill: #FFF3DD;"
            + "-fx-font-size: 16;";

    private static final String STYLE_BTN_HOVER = "-fx-background-color: #C28B5A;"
            + "-fx-background-radius: 10;"
            + "-fx-text-fill: #FFF3DD;"
            + "-fx-font-size: 16;";

    private static final String STYLE_BTN_DISABLED = "-fx-background-color: #CCCCCC;"
            + "-fx-background-radius: 10;"
            + "-fx-text-fill: #FFF3DD;"
            + "-fx-font-size: 16;";

    /** 装饰图片缓存：路径 /assets/decoration/<枚举小写>.png */
    private static final Map<DecorationType, Image> DECORATION_ICON_CACHE =
            new EnumMap<>(DecorationType.class);

    private final Farm farm;
    private long currentGameDay = 0L;

    private final Rectangle[][] tiles = new Rectangle[MAP_SIZE][MAP_SIZE];
    private final Rectangle[][] cropBlocks = new Rectangle[MAP_SIZE][MAP_SIZE];
    private final Tooltip[][] tooltips = new Tooltip[MAP_SIZE][MAP_SIZE];

    /** 装饰渲染层：位于 tile 之上；整层 mouseTransparent。 */
    private final Pane decorationLayer = new Pane();

    private final Rectangle selectionRect = new Rectangle(TILE_SIZE, TILE_SIZE);
    private final VBox menuBox = new VBox(4);

    private Consumer<Soil> onTileSelected;

    /** 装饰区点击回调（P1）：参数 (row, column)。 */
    private BiConsumer<Integer, Integer> onDecorationAreaClicked = (r, c) -> {};

    public FarmView(Farm farm) {
        this.farm = farm;
        setPrefSize(MAP_PX, MAP_PX);
        setMinSize(MAP_PX, MAP_PX);
        setMaxSize(MAP_PX, MAP_PX);

        buildTiles();

        decorationLayer.setMouseTransparent(true);
        decorationLayer.setPickOnBounds(false);
        getChildren().add(decorationLayer);

        buildMenu();

        selectionRect.setFill(Color.TRANSPARENT);
        selectionRect.setStroke(COLOR_HIGHLIGHT);
        selectionRect.setStrokeWidth(3);
        selectionRect.setVisible(false);           // 初始不可见：原 bug 会挡住 (0,0) 格
        selectionRect.setMouseTransparent(true);   // 不拦截点击
        getChildren().add(selectionRect);

        getChildren().add(menuBox);
    }

    // ==================== 公开 API ====================

    public void setCurrentGameDay(long currentGameDay) {
        this.currentGameDay = currentGameDay;
    }

    public void setOnTileSelected(Consumer<Soil> onTileSelected) {
        this.onTileSelected = onTileSelected;
    }

    public void setOnDecorationAreaClicked(BiConsumer<Integer, Integer> callback) {
        if (callback != null) {
            this.onDecorationAreaClicked = callback;
        }
    }

    /**
     * 刷新装饰渲染层：清空后按已放置装饰逐个贴图。
     * 图片路径：/assets/decoration/<枚举小写>.png；缺失时降级为半透明棕色块。
     */
    public void refreshDecorations(List<Decoration> placed) {
        decorationLayer.getChildren().clear();
        if (placed == null) {
            return;
        }
        for (Decoration d : placed) {
            if (d == null || !d.isPlaced()) {
                continue;
            }
            int row = d.getRow();
            int col = d.getColumn();
            if (row < 0 || col < 0 || row >= MAP_SIZE || col >= MAP_SIZE) {
                continue;
            }
            DecorationType type = d.getDecorationType();
            if (type == null) {
                continue;
            }
            int w = type.getWidth() * TILE_SIZE;
            int h = type.getHeight() * TILE_SIZE;
            double x = col * TILE_SIZE;
            double y = row * TILE_SIZE;

            Image icon = loadDecorationIcon(type);
            if (icon != null) {
                ImageView iv = new ImageView(icon);
                iv.setFitWidth(w);
                iv.setFitHeight(h);
                iv.setPreserveRatio(false);
                iv.setSmooth(false);
                iv.setLayoutX(x);
                iv.setLayoutY(y);
                decorationLayer.getChildren().add(iv);
            } else {
                Rectangle placeholder = new Rectangle(x, y, w, h);
                placeholder.setFill(Color.rgb(0xA9, 0x78, 0x50, 0.55));
                decorationLayer.getChildren().add(placeholder);
            }
        }
    }

    // ==================== 纯静态函数 ====================

    public static Color tileColorFor(FarmPlot plotType, Soil soil) {
        if (plotType != FarmPlot.FARM_PLOT) {
            return COLOR_GRASS;
        }
        if (soil == null) {
            return COLOR_GRASS;
        }
        switch (soil.getState()) {
            case EMPTY:
                return COLOR_WOOD;
            case TILLED:
                return COLOR_SOIL;
            case PLANTED:
                Crop crop = soil.getCrop();
                if (crop != null && crop.getGrowthStage() == GrowthStage.MATURE) {
                    return COLOR_HIGHLIGHT;
                }
                return COLOR_SOIL;
            case LOCKED:
            default:
                return COLOR_WOOD;
        }
    }

    public static int cropBlockSizeFor(GrowthStage stage) {
        switch (stage) {
            case SEED:
                return 8;
            case SPROUT:
                return 16;
            case GROWING:
                return 24;
            case MATURE:
            case WITHERED:
            default:
                return 0;
        }
    }

    public static String tooltipTextFor(Soil soil, long currentGameDay) {
        if (soil == null) {
            return "装饰区（可点击放置装饰）";
        }
        switch (soil.getState()) {
            case EMPTY:
                return "未开垦";
            case TILLED:
                return "已开垦，可播种";
            case PLANTED:
                Crop crop = soil.getCrop();
                if (crop != null && crop.getGrowthStage() == GrowthStage.MATURE) {
                    return "已成熟，可收获";
                }
                if (crop == null) {
                    return "已播种";
                }
                boolean wateredToday = crop.getLastManualWaterGameDay() == currentGameDay;
                return crop.getCropType().getDisplayName() + " 成长"
                        + (int) crop.getGrowthProgress() + "% 今日"
                        + (wateredToday ? "已浇" : "未浇");
            case LOCKED:
            default:
                return "未解锁";
        }
    }

    // ==================== 渲染 ====================

    private void buildTiles() {
        for (int row = 0; row < MAP_SIZE; row++) {
            for (int column = 0; column < MAP_SIZE; column++) {
                FarmPlot plotType = farm.getPlotType(row, column);
                Soil soil = farm.getSoil(row, column);

                Rectangle tile = new Rectangle(column * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                tile.setFill(tileColorFor(plotType, soil));
                tile.setStroke(COLOR_TEXT);
                tile.setStrokeWidth(1);

                Tooltip tooltip = new Tooltip(tooltipTextFor(soil, currentGameDay));
                // 缩短 Tooltip 悬停延迟，避免悬停后弹出抢占点击
                tooltip.setShowDelay(Duration.millis(200));
                tooltip.setHideDelay(Duration.millis(80));
                Tooltip.install(tile, tooltip);

                int clickedRow = row;
                int clickedColumn = column;
                // 改用 mousePressed：按下立即响应，不被 Tooltip 悬停窗口干扰
                tile.setOnMousePressed(event -> handleTileClick(clickedRow, clickedColumn));

                getChildren().add(tile);
                tiles[row][column] = tile;
                tooltips[row][column] = tooltip;

                Rectangle cropBlock = new Rectangle();
                cropBlock.setFill(COLOR_GRASS);
                cropBlock.setVisible(false);
                cropBlock.setMouseTransparent(true); // 不拦截点击
                cropBlocks[row][column] = cropBlock;
                getChildren().add(cropBlock);
                updateCropBlock(row, column, plotType, soil);
            }
        }
    }

    private void updateCropBlock(int row, int column, FarmPlot plotType, Soil soil) {
        Rectangle cropBlock = cropBlocks[row][column];
        Crop crop = soil == null ? null : soil.getCrop();
        boolean visible = plotType == FarmPlot.FARM_PLOT
                && soil != null
                && soil.getState() == SoilState.PLANTED
                && crop != null
                && crop.getGrowthStage() != GrowthStage.MATURE;
        if (!visible) {
            cropBlock.setVisible(false);
            return;
        }
        int size = cropBlockSizeFor(crop.getGrowthStage());
        cropBlock.setWidth(size);
        cropBlock.setHeight(size);
        cropBlock.setX(column * TILE_SIZE + (TILE_SIZE - size) / 2.0);
        cropBlock.setY(row * TILE_SIZE + (TILE_SIZE - size) / 2.0);
        cropBlock.setVisible(true);
    }

    public void refreshTile(Soil soil) {
        if (soil == null) {
            return;
        }
        int row = soil.getRow();
        int column = soil.getColumn();
        FarmPlot plotType = farm.getPlotType(row, column);
        tiles[row][column].setFill(tileColorFor(plotType, soil));
        updateCropBlock(row, column, plotType, soil);
        refreshTooltip(soil);
    }

    public void refreshAll() {
        for (Soil soil : farm.getSoils()) {
            refreshTile(soil);
            refreshTooltip(soil);
        }
    }

    // ==================== 选中与菜单 ====================

    public void selectTile(Soil soil) {
        refreshTooltip(soil);
        if (soil == null) {
            selectionRect.setVisible(false);
            return;
        }
        selectionRect.setX(soil.getColumn() * TILE_SIZE);
        selectionRect.setY(soil.getRow() * TILE_SIZE);
        selectionRect.setVisible(true);
    }

    public void showMenuFor(Soil soil, List<Node> buttons) {
        menuBox.getChildren().setAll(buttons);
        double menuWidth = BUTTON_WIDTH + MENU_PADDING.getLeft() + MENU_PADDING.getRight();
        double menuHeight = MENU_PADDING.getTop() + MENU_PADDING.getBottom()
                + buttons.size() * BUTTON_HEIGHT
                + Math.max(0, buttons.size() - 1) * menuBox.getSpacing();

        double x = (soil.getColumn() + 1) * TILE_SIZE + MENU_GAP;
        double y = soil.getRow() * TILE_SIZE;
        if (x + menuWidth > MAP_PX) {
            x = soil.getColumn() * TILE_SIZE - menuWidth - MENU_GAP;
        }
        if (y + menuHeight > MAP_PX) {
            y = MAP_PX - menuHeight;
        }
        menuBox.setLayoutX(Math.max(0, x));
        menuBox.setLayoutY(Math.max(0, y));
        menuBox.setVisible(true);
    }

    public void hideMenu() {
        menuBox.setVisible(false);
    }

    public void showTip(Soil soil, String message) {
        if (soil == null) {
            return;
        }
        int row = soil.getRow();
        int column = soil.getColumn();
        tooltips[row][column].setText(message);
        tooltips[row][column].show(tiles[row][column], TILE_SIZE / 2.0, TILE_SIZE / 2.0);
    }

    // ==================== 按钮 ====================

    public Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setOnMouseEntered(event -> {
            if (!button.isDisabled()) {
                button.setStyle(STYLE_BTN_HOVER);
            }
        });
        button.setOnMouseExited(event -> applyButtonStyle(button, button.isDisabled()));
        button.disabledProperty().addListener((observable, oldValue, disabled) ->
                applyButtonStyle(button, disabled));
        applyButtonStyle(button, false);
        return button;
    }

    // ==================== 内部辅助 ====================

    private void handleTileClick(int row, int column) {
        Soil soil = farm.getSoil(row, column);
        if (soil == null) {
            FarmPlot plot = farm.getPlotType(row, column);
            if (plot == FarmPlot.DECORATION_AREA) {
                onDecorationAreaClicked.accept(row, column);
            }
            if (onTileSelected != null) {
                onTileSelected.accept(null);
            }
            return;
        }
        if (onTileSelected != null) {
            onTileSelected.accept(soil);
        }
    }

    private void refreshTooltip(Soil soil) {
        if (soil == null) {
            return;
        }
        tooltips[soil.getRow()][soil.getColumn()].setText(tooltipTextFor(soil, currentGameDay));
    }

    private void buildMenu() {
        menuBox.setPadding(MENU_PADDING);
        menuBox.setStyle("-fx-background-color: #FFF3DD;"
                + "-fx-background-radius: " + MENU_RADIUS + ";");
        menuBox.setVisible(false);
        menuBox.setMouseTransparent(false);
    }

    private void applyButtonStyle(Button button, boolean disabled) {
        button.setStyle(disabled ? STYLE_BTN_DISABLED : STYLE_BTN_NORMAL);
    }

    /** 加载装饰图片；缺失时返回 null。 */
    private static Image loadDecorationIcon(DecorationType type) {
        return DECORATION_ICON_CACHE.computeIfAbsent(type, t -> {
            String path = "/assets/decoration/" + t.name().toLowerCase(Locale.ROOT) + ".png";
            try (InputStream in = FarmView.class.getResourceAsStream(path)) {
                if (in == null) {
                    return null;
                }
                Image img = new Image(in);
                return img.isError() ? null : img;
            } catch (Exception ex) {
                return null;
            }
        });
    }
}