package com.fieldstory.farm.model;

/**
 * 12x12地图格的逻辑类型。P0仅FARM_PLOT可交互，其余区域只显示占位。
 */
public enum FarmPlot {
    FARM_PLOT,
    DECORATION_AREA,
    SHOP,
    SHOWCASE
}
