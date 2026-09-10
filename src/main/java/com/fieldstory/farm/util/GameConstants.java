package com.fieldstory.farm.util;

/**
 * 《田野物语 · 三韵集》全局游戏常量。
 *
 * <p>
 * 本类用于集中维护全项目共享且在当前版本中固定不变的数值，
 * 避免 Controller、Service、Model 中散落魔法数字。
 * </p>
 *
 * <p>
 * P0阶段主要包括：
 * 1. 地图与布局常量
 * 2. 世界时间常量
 * 3. 玩家与经济常量
 * 4. P0固定倍率
 * </p>
 *
 * <p>
 * 注意：
 * 本类只保存真正的全局固定常量。
 * 作物种子价格和基础售价不放在这里，
 * 由 CropType 作为唯一数据源负责。
 * </p>
 */
public final class GameConstants {

    // =========================================================
    // 地图与布局
    // =========================================================

    /**
     * 农场地图总行数。
     *
     * P0地图固定为12×12。
     */
    public static final int MAP_ROWS = 12;

    /**
     * 农场地图总列数。
     */
    public static final int MAP_COLS = 12;

    /**
     * 中心8×8农田区域起始行。
     *
     * 使用0-based坐标：
     * 中心区域为第2~9行。
     */
    public static final int CENTER_START_ROW = 2;

    /**
     * 中心8×8农田区域结束行。
     */
    public static final int CENTER_END_ROW = 9;

    /**
     * 中心8×8农田区域起始列。
     */
    public static final int CENTER_START_COL = 2;

    /**
     * 中心8×8农田区域结束列。
     */
    public static final int CENTER_END_COL = 9;

    /**
     * 单个地图格子的默认像素尺寸。
     *
     * 属于当前P0界面基础布局参数。
     * 后续统一UI时如果调整，应由全组统一修改。
     */
    public static final int TILE_SIZE = 64;


    // =========================================================
    // 世界时间
    // =========================================================

    /**
     * 每次基础游戏时钟tick推进的游戏分钟数。
     *
     * 当前D模块P0设计约定：
     * 每次tick推进10游戏分钟。
     */
    public static final int MINUTES_PER_TICK = 10;

    /**
     * 一个游戏日包含的游戏分钟数。
     *
     * 24 × 60 = 1440。
     */
    public static final int MINUTES_PER_DAY = 1440;

    /**
     * 默认游戏日开始时间。
     *
     * 360分钟 = 06:00。
     */
    public static final int DAY_START = 360;

    /**
     * 昼夜显示中的白天结束时间。
     *
     * 1080分钟 = 18:00。
     */
    public static final int DAY_END = 1080;


    // =========================================================
    // 玩家与经济
    // =========================================================

    /**
     * 新游戏初始金币。
     *
     * 正式规则：
     * 玩家新建游戏时拥有500金币。
     *
     * GameManager创建新游戏时应引用此常量，
     * 不应再次单独硬编码500。
     */
    public static final int INITIAL_GOLD = 500;

    /**
     * 开垦一格EMPTY土地所需金币。
     *
     * A模块开垦土地时通过EconomyService扣除该费用。
     */
    public static final int TILL_COST = 5;


    // =========================================================
    // P0固定倍率
    // =========================================================

    /**
     * P0天气倍率。
     *
     * P0尚未正式启用天气系统，
     * 因此固定为1.0。
     *
     * P1开始由WeatherService提供实际倍率。
     */
    public static final double WEATHER_RATE_P0 = 1.0;

    /**
     * P0装饰倍率。
     *
     * P0尚未启用装饰Buff，
     * 因此固定为1.0。
     */
    public static final double DECORATION_RATE_P0 = 1.0;

    /**
     * P0随机事件倍率。
     *
     * P0尚未启用随机事件，
     * 因此固定为1.0。
     *
     * P2由EventService正式提供事件效果。
     */
    public static final double EVENT_RATE_P0 = 1.0;


    /**
     * 工具类禁止实例化。
     */
    private GameConstants() {
    }
}