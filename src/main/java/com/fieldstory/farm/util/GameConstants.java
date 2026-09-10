package com.fieldstory.farm.util;

/**
 * 全局常量类（D 模块 P0：世界环境）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§六、《游戏规则设计文档》§5.1/§8/§10.1/§63、
 * 《P0-P4功能实现与验收规范》§7/§10/§11/§15/§35。
 *
 * <p>约束：Controller/Service 中不得出现 1440、360、1080 等魔法数字，一律引用本类常量。
 * P0 固定倍率必须带 {@code _P0} 后缀，标识为占位值。
 */
public final class GameConstants {

    private GameConstants() {
        // 常量类禁止实例化
    }

    // ===== 地图与布局 =====

    /** 地图行数（规则 §10.1；验收 §11）。 */
    public static final int MAP_ROWS = 12;

    /** 地图列数（规则 §10.1；验收 §11）。 */
    public static final int MAP_COLS = 12;

    /** 中心种植区起始行（0-based，规则 §10.1；决策 D05/D10）。 */
    public static final int CENTER_START_ROW = 2;

    /** 中心种植区结束行（0-based，含）。 */
    public static final int CENTER_END_ROW = 9;

    /** 中心种植区起始列（0-based）。 */
    public static final int CENTER_START_COL = 2;

    /** 中心种植区结束列（0-based，含）。 */
    public static final int CENTER_END_COL = 9;

    /** 单元格像素尺寸（UI 布局规范）。 */
    public static final int TILE_SIZE = 64;

    // ===== 时间系统 =====

    /** 每次 tick 推进的游戏分钟数（规则 §5.1；验收 §25）。 */
    public static final int MINUTES_PER_TICK = 10;

    /** 一游戏日的分钟数（1 日 = 24 小时 × 60 分钟，规则 §5.1）。 */
    public static final int MINUTES_PER_DAY = 1440;

    /** 白天开始（06:00，规则 §5.1）。 */
    public static final int DAY_START = 360;

    /** 白天结束（18:00，规则 §5.1）。 */
    public static final int DAY_END = 1080;

    // ===== 经济系统 =====

    /** 初始金币（规则 §63；验收 §35）。 */
    public static final int INITIAL_GOLD = 500;

    /** 开垦消耗金币（规则 §12.1；验收 §15）。 */
    public static final int TILL_COST = 5;

    // ===== P0 固定倍率（占位，验收 §10） =====

    /** P0 固定天气倍率（验收 §10）。 */
    public static final double WEATHER_RATE_P0 = 1.0;

    /** P0 固定装饰倍率（验收 §10）。 */
    public static final double DECORATION_RATE_P0 = 1.0;

    /** P0 固定事件倍率（验收 §10）。 */
    public static final double EVENT_RATE_P0 = 1.0;
}
