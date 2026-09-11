package com.fieldstory.farm.controller;

import com.fieldstory.farm.model.BuffSnapshot;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import com.fieldstory.farm.service.BuffService;
import com.fieldstory.farm.service.DecorationService;

import java.util.List;
import java.util.Objects;

/**
 * B 模块 P1 装饰控制器。
 *
 * <p>新增回调：放置/移动/收回成功后触发，供装配层刷新地图装饰层与面板。
 */
public class DecorationController {

    private final DecorationService decorationService;
    private final BuffService buffService;

    /** 装饰状态变化回调（默认空）。 */
    private Runnable onChanged = () -> {};

    public DecorationController(DecorationService decorationService,
                                BuffService buffService) {
        this.decorationService = Objects.requireNonNull(decorationService);
        this.buffService = Objects.requireNonNull(buffService);
    }

    public void setOnChanged(Runnable callback) {
        if (callback != null) {
            this.onChanged = callback;
        }
    }

    public List<Decoration> getOwnedDecorations() {
        return decorationService.getOwnedDecorations();
    }

    public List<Decoration> getPlacedDecorations() {
        return decorationService.getPlacedDecorations();
    }

    public DecorationPlacementResult place(Decoration decoration, int row, int column) {
        DecorationPlacementResult result = decorationService.place(decoration, row, column);
        if (result == DecorationPlacementResult.SUCCESS) {
            onChanged.run();
        }
        return result;
    }

    public DecorationPlacementResult move(Decoration decoration, int newRow, int newColumn) {
        DecorationPlacementResult result = decorationService.move(decoration, newRow, newColumn);
        if (result == DecorationPlacementResult.SUCCESS) {
            onChanged.run();
        }
        return result;
    }

    public void removeFromFarm(Decoration decoration) {
        decorationService.removeFromFarm(decoration);
        onChanged.run();
    }

    public BuffSnapshot snapshot(int row, int column, CropType cropType) {
        return buffService.getSnapshot(row, column, cropType);
    }

    public static String messageFor(DecorationPlacementResult result) {
        switch (result) {
            case SUCCESS:
                return "已放置";
            case OUT_OF_BOUNDS:
                return "超出地图范围";
            case NOT_DECORATION_AREA:
                return "只能放在装饰区";
            case CELL_OCCUPIED:
                return "该位置已被占用";
            case INVALID_SIZE:
                return "装饰尺寸无效";
            case NOT_OWNED:
                return "尚未拥有该装饰";
            case ALREADY_PLACED:
                return "该装饰已经放置";
            default:
                return "放置失败";
        }
    }
}