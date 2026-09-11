package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.FarmPlot;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import com.fieldstory.farm.model.impl.BasicDecoration;
import com.fieldstory.farm.service.DecorationService;
import com.fieldstory.farm.util.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DecorationService 默认实现。
 *
 * <p>地图尺寸统一取 {@link GameConstants#MAP_ROWS} / {@link GameConstants#MAP_COLS}
 * （D 模块公共常量唯一数据源，B 不重复定义 12）。
 */
public class BasicDecorationService implements DecorationService {

    private final Farm farm;
    private final List<Decoration> ownedDecorations = new ArrayList<>();

    public BasicDecorationService(Farm farm) {
        this.farm = Objects.requireNonNull(farm);
    }

    @Override
    public List<Decoration> getOwnedDecorations() {
        return List.copyOf(ownedDecorations);
    }

    @Override
    public List<Decoration> getPlacedDecorations() {
        return ownedDecorations.stream().filter(Decoration::isPlaced).toList();
    }

    @Override
    public int getOwnedCount(DecorationType type) {
        return (int) ownedDecorations.stream()
                .filter(d -> d.getDecorationType() == type)
                .count();
    }

    @Override
    public int getPlacedCount(DecorationType type) {
        return (int) ownedDecorations.stream()
                .filter(Decoration::isPlaced)
                .filter(d -> d.getDecorationType() == type)
                .count();
    }

    @Override
    public List<Decoration> addPurchasedDecoration(DecorationType type, int quantity) {
        List<Decoration> created = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Decoration decoration = new BasicDecoration(type);
            ownedDecorations.add(decoration);
            created.add(decoration);
        }
        return created;
    }

    @Override
    public boolean canPlace(Decoration decoration, int row, int column) {
        return placeCheck(decoration, row, column) == DecorationPlacementResult.SUCCESS;
    }

    @Override
    public DecorationPlacementResult place(Decoration decoration, int row, int column) {
        DecorationPlacementResult result = placeCheck(decoration, row, column);
        if (result != DecorationPlacementResult.SUCCESS) {
            return result;
        }
        decoration.setRow(row);
        decoration.setColumn(column);
        decoration.setPlaced(true);
        return DecorationPlacementResult.SUCCESS;
    }

    @Override
    public DecorationPlacementResult move(Decoration decoration, int newRow, int newColumn) {
        if (!ownedDecorations.contains(decoration)) {
            return DecorationPlacementResult.NOT_OWNED;
        }
        boolean oldPlaced = decoration.isPlaced();
        int oldRow = decoration.getRow();
        int oldColumn = decoration.getColumn();

        decoration.setPlaced(false);
        decoration.setRow(-1);
        decoration.setColumn(-1);

        DecorationPlacementResult result = placeCheck(decoration, newRow, newColumn);
        if (result != DecorationPlacementResult.SUCCESS) {
            decoration.setPlaced(oldPlaced);
            decoration.setRow(oldRow);
            decoration.setColumn(oldColumn);
            return result;
        }
        decoration.setRow(newRow);
        decoration.setColumn(newColumn);
        decoration.setPlaced(true);
        return DecorationPlacementResult.SUCCESS;
    }

    @Override
    public void removeFromFarm(Decoration decoration) {
        decoration.setPlaced(false);
        decoration.setRow(-1);
        decoration.setColumn(-1);
    }

    @Override
    public boolean isDecorationArea(int row, int column) {
        if (row < 0 || column < 0
                || row >= GameConstants.MAP_ROWS
                || column >= GameConstants.MAP_COLS) {
            return false;
        }
        return farm.getPlotType(row, column) == FarmPlot.DECORATION_AREA;
    }

    @Override
    public boolean isOccupied(int row, int column, Decoration ignore) {
        for (Decoration d : ownedDecorations) {
            if (!d.isPlaced() || d == ignore) {
                continue;
            }
            int w = d.getDecorationType().getWidth();
            int h = d.getDecorationType().getHeight();
            if (row >= d.getRow() && row < d.getRow() + h
                    && column >= d.getColumn() && column < d.getColumn() + w) {
                return true;
            }
        }
        return false;
    }

    private DecorationPlacementResult placeCheck(Decoration decoration, int row, int column) {
        if (decoration == null || !ownedDecorations.contains(decoration)) {
            return DecorationPlacementResult.NOT_OWNED;
        }
        DecorationType type = decoration.getDecorationType();
        if (type == null) {
            return DecorationPlacementResult.INVALID_SIZE;
        }
        int w = type.getWidth();
        int h = type.getHeight();

        if (row < 0 || column < 0
                || row + h > GameConstants.MAP_ROWS
                || column + w > GameConstants.MAP_COLS) {
            return DecorationPlacementResult.OUT_OF_BOUNDS;
        }
        for (int r = row; r < row + h; r++) {
            for (int c = column; c < column + w; c++) {
                if (!isDecorationArea(r, c)) {
                    return DecorationPlacementResult.NOT_DECORATION_AREA;
                }
                if (isOccupied(r, c, decoration)) {
                    return DecorationPlacementResult.CELL_OCCUPIED;
                }
            }
        }
        return DecorationPlacementResult.SUCCESS;
    }
}