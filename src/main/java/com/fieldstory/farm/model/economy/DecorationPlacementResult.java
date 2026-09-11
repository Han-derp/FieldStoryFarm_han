package com.fieldstory.farm.model.economy;

/**
 * 装饰放置/移动结果。
 */
public enum DecorationPlacementResult {
    SUCCESS,
    OUT_OF_BOUNDS,
    NOT_DECORATION_AREA,
    CELL_OCCUPIED,
    INVALID_SIZE,
    NOT_OWNED,
    ALREADY_PLACED
}