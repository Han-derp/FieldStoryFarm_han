package com.fieldstory.farm.model;

/**
 * 农田地图接口（A 模块 P0 交付物，D 模块仅引用其类型）。
 *
 * <p>依据《A模块 P0 接口与类设计文档》§7.1。实现类 {@code BasicFarm} 由 A 模块提供。
 *
 * <p><b>归属说明</b>：本接口归 A 模块所有。D 模块的 {@code FarmGameModel} 按团队裁决 ①
 * 统一以本接口作为土地字段类型，故此处先行声明以解除编译依赖；A 模块交付后应以其版本为准。
 */
public interface Farm {

    /**
     * 获取地图宽度（列数）。
     *
     * @return 宽度，P0 为 12
     */
    int getWidth();

    /**
     * 获取地图高度（行数）。
     *
     * @return 高度，P0 为 12
     */
    int getHeight();

    /**
     * 获取指定坐标的地块类型。
     *
     * @param row 行（0-based）
     * @param col 列（0-based）
     * @return 地块类型
     */
    FarmPlot getPlot(int row, int col);

    /**
     * 获取指定坐标的土壤；非 FARM_PLOT 返回 null。
     *
     * @param row 行（0-based）
     * @param col 列（0-based）
     * @return 土壤，非种植格返回 null
     */
    Soil getSoil(int row, int col);

    /**
     * 判断指定坐标是否为种植格。
     *
     * @param row 行（0-based）
     * @param col 列（0-based）
     * @return 是种植格返回 true
     */
    boolean isFarmPlot(int row, int col);
}
