package com.fieldstory.farm.util;

public final class GameConstants {

    private GameConstants() {}

    // ===== 地图与布局 =====
    public static final int MAP_ROWS = 12;
    public static final int MAP_COLS = 12;
    public static final int CENTER_START_ROW = 2;
    public static final int CENTER_END_ROW = 9;
    public static final int CENTER_START_COL = 2;
    public static final int CENTER_END_COL = 9;
    public static final int TILE_SIZE = 64;

    // ===== 时间系统 =====
    public static final int MINUTES_PER_TICK = 10;
    public static final int MINUTES_PER_DAY = 1440;
    public static final int DAY_START = 360;    // 06:00
    public static final int DAY_END = 1080;     // 18:00

    // ===== 经济系统 =====
    public static final int INITIAL_GOLD = 500;
    public static final int TILL_COST = 5;

    // ===== P0 固定倍率 =====
    public static final double WEATHER_RATE_P0 = 1.0;
    public static final double DECORATION_RATE_P0 = 1.0;
    public static final double EVENT_RATE_P0 = 1.0;

}
