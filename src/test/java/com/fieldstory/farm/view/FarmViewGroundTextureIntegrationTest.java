package com.fieldstory.farm.view;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.model.impl.BasicCrop;
import com.fieldstory.farm.model.impl.BasicFarm;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FarmView} 地面贴图层接入集成测试（A 模块 P1 地面 Tile 美化渲染接入卡）。
 *
 * <p>沿用 {@link FarmViewRestoreIntegrationTest} 的 Platform.startup + onFxThread
 * 模式，在 JavaFX 线程内走「农场模型 → FarmView 构造」真实链路：
 * 装饰区草地贴图（决策 D-G2 GRASS 变体）、耕地干/湿帧切换（决策 D-G2）、
 * 2× 放大格内居中（决策 D-G1）、NONE 变体隐藏（决策 D-G3）、
 * 去描边保留选中高亮（决策 D-G5）与 Z 序（tile → groundTexture → cropSprite）。
 *
 * <p>节点查找沿用 {@link FarmViewCropSpriteIntegrationTest} 的谓词遍历方式
 * （按位置/类型过滤 children），不依赖绝对索引。
 */
class FarmViewGroundTextureIntegrationTest {

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX 工具包初始化超时");
    }

    private static <T> T onFxThread(Supplier<T> supplier) throws InterruptedException {
        AtomicReference<T> ref = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ref.set(supplier.get());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX 任务执行超时");
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
        return ref.get();
    }

    /** 在指定格播种小麦（全局坐标，BasicFarm 中心种植区 2..9）。 */
    private static void plant(Farm farm, int row, int column, GrowthStage stage,
                              long lastManualWaterGameDay) {
        Soil soil = farm.getSoil(row, column);
        soil.setState(SoilState.PLANTED);
        Crop crop = new BasicCrop();
        crop.setCropType(CropType.WHEAT);
        crop.setGrowthStage(stage);
        crop.setGrowthProgress(50);
        crop.setLastManualWaterGameDay(lastManualWaterGameDay);
        soil.setCrop(crop);
    }

    /** 谓词遍历：该格居中的可见地面贴图（决策 D-G1：x/y = col*44+6 / row*44+6）。 */
    private static ImageView groundAt(FarmView view, int row, int column) {
        double x = column * FarmView.TILE_SIZE + (FarmView.TILE_SIZE - 32) / 2.0;
        double y = row * FarmView.TILE_SIZE + (FarmView.TILE_SIZE - 32) / 2.0;
        return view.getChildren().stream()
                .filter(ImageView.class::isInstance)
                .map(ImageView.class::cast)
                .filter(ImageView::isVisible)
                .filter(iv -> iv.getX() == x && iv.getY() == y)
                .findFirst()
                .orElse(null);
    }

    /** 谓词遍历：与指定格区域有交集的可见 ImageView（地面层 + 作物层）。 */
    private static List<ImageView> texturesIntersectingCell(FarmView view, int row, int column) {
        return view.getChildren().stream()
                .filter(ImageView.class::isInstance)
                .map(ImageView.class::cast)
                .filter(ImageView::isVisible)
                .filter(iv -> iv.getX() < (column + 1) * FarmView.TILE_SIZE
                        && iv.getX() + iv.getFitWidth() > column * FarmView.TILE_SIZE
                        && iv.getY() < (row + 1) * FarmView.TILE_SIZE
                        && iv.getY() + iv.getFitHeight() > row * FarmView.TILE_SIZE)
                .collect(Collectors.toList());
    }

    /** 谓词遍历：指定格 44×44 底色 tile（左上角定位、无居中偏移）。 */
    private static Rectangle tileAt(FarmView view, int row, int column) {
        return view.getChildren().stream()
                .filter(Rectangle.class::isInstance)
                .map(Rectangle.class::cast)
                .filter(r -> r.getX() == column * FarmView.TILE_SIZE
                        && r.getY() == row * FarmView.TILE_SIZE
                        && r.getWidth() == FarmView.TILE_SIZE)
                .findFirst()
                .orElse(null);
    }

    /** 断言 viewport 为 (x, y, 16, 16)（地面图集单帧尺寸）。 */
    private static void assertViewport(ImageView view, double x, double y) {
        assertNotNull(view);
        Rectangle2D viewport = view.getViewport();
        assertNotNull(viewport);
        assertEquals(x, viewport.getMinX(), 0.0);
        assertEquals(y, viewport.getMinY(), 0.0);
        assertEquals(16, viewport.getWidth(), 0.0);
        assertEquals(16, viewport.getHeight(), 0.0);
    }

    /** 装饰区格：构造即显示草地贴图，32×32 居中（决策 D-G1），GRASS 帧 (48,96,16,16)。 */
    @Test
    void decorationCellShowsCenteredGrassTexture() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            ImageView ground = groundAt(view, 0, 0); // (0,0) 为外围装饰区
            assertNotNull(ground, "装饰区格地面贴图可见");
            assertEquals(32, ground.getFitWidth(), 0.0);
            assertEquals(32, ground.getFitHeight(), 0.0);
            assertEquals(0 * FarmView.TILE_SIZE + 6, ground.getX(), 0.0);
            assertEquals(0 * FarmView.TILE_SIZE + 6, ground.getY(), 0.0);
            assertViewport(ground, 48, 96); // GRASS：col3×16=48, row6×16=96
            return null;
        });
    }

    /** TILLED 格（干）：显示耕地干帧 (48,16,16,16)（决策 D-G2：无作物仅按 wetToday=false）。 */
    @Test
    void tilledDryCellShowsTilledViewport() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            farm.getSoil(2, 2).setState(SoilState.TILLED);
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertViewport(groundAt(view, 2, 2), 48, 16); // TILLED：col3×16=48, row1×16=16
            return null;
        });
    }

    /** 同一 TILLED 格：setWetToday(true) + refreshAll 后切为湿地深色帧 (128,160,16,16)。 */
    @Test
    void setWetTodayTrueThenRefreshAllShowsWetViewport() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            farm.getSoil(2, 2).setState(SoilState.TILLED);
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertViewport(groundAt(view, 2, 2), 48, 16); // 湿前：干帧

            view.setWetToday(true);
            view.refreshAll();

            assertViewport(groundAt(view, 2, 2), 128, 160); // WET：col8×16=128, row10×16=160
            return null;
        });
    }

    /** EMPTY 格：地面贴图不可见（决策 D-G3 保持木色纯色语义）。 */
    @Test
    void emptyCellHidesGroundTexture() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertNull(groundAt(view, 2, 2), "EMPTY 格不铺地面贴图");
            return null;
        });
    }

    /** PLANTED + 作物今日已浇（lastManualWaterGameDay == currentGameDay）→ WET 帧。 */
    @Test
    void plantedWateredTodayShowsWetViewport() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            plant(farm, 2, 2, GrowthStage.GROWING, 0L); // 今日已浇（决策 D14 long 用 ==）
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertViewport(groundAt(view, 2, 2), 128, 160); // WET
            return null;
        });
    }

    /** PLANTED + MATURE：地面贴图不可见（决策 D-G3 保留整格高亮）。 */
    @Test
    void plantedMatureHidesGroundTexture() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            plant(farm, 2, 2, GrowthStage.MATURE, -1L);
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertNull(groundAt(view, 2, 2), "MATURE 格不铺地面贴图");
            return null;
        });
    }

    /** PLANTED + WITHERED：地面贴图不可见（决策 D-G3 保留整格枯萎色）。 */
    @Test
    void plantedWitheredHidesGroundTexture() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            plant(farm, 2, 2, GrowthStage.WITHERED, -1L);
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            assertNull(groundAt(view, 2, 2), "WITHERED 格不铺地面贴图");
            return null;
        });
    }

    /** 去描边（决策 D-G5）：FARM_PLOT 格 tile 无描边，选中高亮描边保留。 */
    @Test
    void farmPlotTilesHaveNoStrokeWhileSelectionRectKeepsHighlight() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            for (int row = BasicFarm.FARM_AREA_ORIGIN;
                 row < BasicFarm.FARM_AREA_ORIGIN + BasicFarm.FARM_AREA_SIZE; row++) {
                for (int column = BasicFarm.FARM_AREA_ORIGIN;
                     column < BasicFarm.FARM_AREA_ORIGIN + BasicFarm.FARM_AREA_SIZE; column++) {
                    Rectangle tile = tileAt(view, row, column);
                    assertNotNull(tile, "FARM_PLOT 格存在底色 tile");
                    assertNull(tile.getStroke(), "tile 无 1px 描边（决策 D-G5）");
                }
            }
            List<Rectangle> stroked = view.getChildren().stream()
                    .filter(Rectangle.class::isInstance)
                    .map(Rectangle.class::cast)
                    .filter(r -> r.getStroke() != null)
                    .collect(Collectors.toList());
            assertEquals(1, stroked.size(), "仅选中高亮描边存在");
            assertEquals(FarmView.COLOR_HIGHLIGHT, stroked.get(0).getStroke());
            assertEquals(3, stroked.get(0).getStrokeWidth(), 0.0);
            return null;
        });
    }

    /** Z 序：PLANTED 格 groundTexture 索引小于 cropSprite 索引（tile → ground → crop）。 */
    @Test
    void groundTextureRendersBelowCropSprite() throws InterruptedException {
        onFxThread(() -> {
            Farm farm = new BasicFarm();
            plant(farm, 2, 2, GrowthStage.GROWING, -1L); // 干：地面 TILLED + 作物 GROWING 均可见
            FarmView view = assertDoesNotThrow(() -> new FarmView(farm));

            List<ImageView> textures = texturesIntersectingCell(view, 2, 2);
            assertEquals(2, textures.size(), "该格可见地面层 + 作物层两张贴图");
            ImageView ground = textures.stream()
                    .filter(iv -> iv.getY() == 2 * FarmView.TILE_SIZE + 6)
                    .findFirst()
                    .orElse(null);
            ImageView crop = textures.stream()
                    .filter(iv -> iv != ground)
                    .findFirst()
                    .orElse(null);
            assertNotNull(ground, "找到地面贴图层");
            assertNotNull(crop, "找到作物贴图层");
            assertTrue(view.getChildren().indexOf(ground) < view.getChildren().indexOf(crop),
                    "地面贴图渲染在作物贴图之下");
            return null;
        });
    }
}
