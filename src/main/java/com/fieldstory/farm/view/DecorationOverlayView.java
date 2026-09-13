package com.fieldstory.farm.view;

import com.fieldstory.farm.controller.DecorationController;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmPlot;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * B 模块 P1 装饰覆盖层。
 *
 * <p>不修改 A 的 FarmView：
 * FarmView 为底层，装饰图片为透明覆盖层。
 */
public final class DecorationOverlayView
        extends StackPane {

    private final Farm farm;

    private final FarmView farmView;

    private final DecorationController controller;

    private final Pane decorationLayer =
            new Pane();

    private Decoration pendingDecoration;

    private Consumer<String> messageSink =
            message -> {
            };

    public DecorationOverlayView(
            Farm farm,
            FarmView farmView,
            DecorationController controller) {

        this.farm =
                Objects.requireNonNull(
                        farm,
                        "farm"
                );

        this.farmView =
                Objects.requireNonNull(
                        farmView,
                        "farmView"
                );

        this.controller =
                Objects.requireNonNull(
                        controller,
                        "controller"
                );

        setPrefSize(
                FarmView.MAP_PX,
                FarmView.MAP_PX
        );

        setMinSize(
                FarmView.MAP_PX,
                FarmView.MAP_PX
        );

        setMaxSize(
                FarmView.MAP_PX,
                FarmView.MAP_PX
        );

        decorationLayer.setMouseTransparent(
                true
        );

        decorationLayer.setPickOnBounds(
                false
        );

        decorationLayer.setPrefSize(
                FarmView.MAP_PX,
                FarmView.MAP_PX
        );

        getChildren().addAll(
                farmView,
                decorationLayer
        );

        farmView.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                this::handleFarmClick
        );

        refresh();
    }

    public void setMessageSink(
            Consumer<String> sink) {

        messageSink =
                sink == null
                        ? message -> {
                }
                        : sink;
    }

    public void selectForPlacement(
            Decoration decoration) {

        pendingDecoration =
                decoration;

        if (
                decoration != null
                        && decoration
                        .getDecorationType()
                        != null
        ) {

            messageSink.accept(
                    "已选择"
                            + decoration
                            .getDecorationType()
                            .getDisplayName()
                            + "，请点击外围装饰区放置"
            );
        }
    }

    public void clearSelection() {

        pendingDecoration =
                null;
    }

    public void refresh() {

        decorationLayer
                .getChildren()
                .clear();

        for (
                Decoration decoration :
                controller
                        .getPlacedDecorations()
        ) {

            render(decoration);
        }
    }

    private void handleFarmClick(
            MouseEvent event) {

        if (
                pendingDecoration == null
        ) {
            return;
        }

        int column =
                (int) (
                        event.getX()
                                / FarmView.TILE_SIZE
                );

        int row =
                (int) (
                        event.getY()
                                / FarmView.TILE_SIZE
                );

        if (
                row < 0
                        || row >= FarmView.MAP_SIZE
                        || column < 0
                        || column >= FarmView.MAP_SIZE
        ) {
            return;
        }

        if (
                farm.getPlotType(
                        row,
                        column
                )
                        != FarmPlot.DECORATION_AREA
        ) {
            return;
        }

        DecorationPlacementResult result =
                controller.place(
                        pendingDecoration,
                        row,
                        column
                );

        messageSink.accept(
                DecorationController.messageFor(
                        result
                )
        );

        if (
                result
                        == DecorationPlacementResult.SUCCESS
        ) {

            pendingDecoration =
                    null;

            refresh();
        }

        event.consume();
    }

    private void render(
            Decoration decoration) {

        DecorationType type =
                decoration.getDecorationType();

        if (type == null) {
            return;
        }

        double x =
                decoration.getColumn()
                        * FarmView.TILE_SIZE;

        double y =
                decoration.getRow()
                        * FarmView.TILE_SIZE;

        double width =
                type.getWidth()
                        * FarmView.TILE_SIZE;

        double height =
                type.getHeight()
                        * FarmView.TILE_SIZE;

        String path =
                "/assets/decoration/"
                        + type.getAssetFileName();

        ImageView view =
                BImageAssets.view(
                        path,
                        width,
                        height
                );

        if (view != null) {

            /*
             * 地图格需要完整覆盖 footprint，
             * 因此这里不保持原图宽高比。
             */
            view.setPreserveRatio(
                    false
            );

            view.setLayoutX(
                    x
            );

            view.setLayoutY(
                    y
            );

            decorationLayer
                    .getChildren()
                    .add(view);

            return;
        }

        /*
         * 图片资源缺失时使用已有土地色进行 fallback，
         * 避免 UI 完全看不到装饰。
         */
        Rectangle fallback =
                new Rectangle(
                        x,
                        y,
                        width,
                        height
                );

        fallback.setFill(
                Color.rgb(
                        0xA9,
                        0x78,
                        0x50,
                        0.55
                )
        );

        fallback.setMouseTransparent(
                true
        );

        decorationLayer
                .getChildren()
                .add(
                        fallback
                );
    }
}