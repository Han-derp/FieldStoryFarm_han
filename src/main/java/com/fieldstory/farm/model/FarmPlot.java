package com.fieldstory.farm.model;

/**
 * 地块类型枚举（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§6.1、决策 D04。
 */
public enum FarmPlot {

    /** 种植格（进入八宫格土壤）。 */
    FARM_PLOT,

    /** P0 外围 2 圈占位，P1 起装饰。 */
    DECORATION_AREA,

    /** 占位，P1/P3 确定后启用。 */
    SHOP,

    /** 占位，P1/P3 确定后启用。 */
    SHOWCASE
}
