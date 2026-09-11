package com.fieldstory.farm.model.impl;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;

import java.util.Objects;
import java.util.UUID;

/**
 * Decoration 基础实现。
 */
public class BasicDecoration implements Decoration {

    private UUID decorationUuid;
    private DecorationType decorationType;
    private int row = -1;
    private int column = -1;
    private boolean placed = false;

    /** 供 E 反序列化使用。 */
    public BasicDecoration() {
    }

    public BasicDecoration(DecorationType type) {
        this.decorationUuid = UUID.randomUUID();
        this.decorationType = Objects.requireNonNull(type);
    }

    @Override
    public UUID getDecorationUuid() { return decorationUuid; }

    @Override
    public void setDecorationUuid(UUID decorationUuid) { this.decorationUuid = decorationUuid; }

    @Override
    public DecorationType getDecorationType() { return decorationType; }

    @Override
    public void setDecorationType(DecorationType decorationType) { this.decorationType = decorationType; }

    @Override
    public int getRow() { return row; }

    @Override
    public void setRow(int row) { this.row = row; }

    @Override
    public int getColumn() { return column; }

    @Override
    public void setColumn(int column) { this.column = column; }

    @Override
    public boolean isPlaced() { return placed; }

    @Override
    public void setPlaced(boolean placed) { this.placed = placed; }
}