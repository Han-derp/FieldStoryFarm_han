package com.fieldstory.farm.model;

/**
 * 存档聚合（E 存档模块持有）。
 *
 * <p>P0 JSON 存档（验收规范 §七十~§七十三）以本类为「加载单元」，避免覆盖
 * Player 引用（含种子背包，见 {@link Player}）、
 * 游戏天数（GameClock.getGameDay()）、每块土地的状态
 * （空闲/已耕种/成长中）+ 作物进度（{@link PlotState}）、已解锁内容。
 *
 * <p>本类只保存「世界是什么状态」，不含任何游戏计算（统一 Model 原则）。
 * 种子背包的<b>唯一</b>真源是 {@link Player#getSeedInventory()}（B 模块 §6.2），
 * 本类不另存一份，避免两份数据不一致。
 */
public class GameState {

    /** 玩家（含金币与种子背包，B 模块 Player 模型）。 */
    private Player player;

    /** 游戏天数（对应 GameClock.getGameDay()，D 模块时钟结算时写入）。 */
    private long gameDay;

    /** 存档时 GameClock 的世界时间（ISO-8601 字符串，验收 §七十一）。
     *  D 模块时钟结算前未落盘时为 null。 */
    private String currentWorldTime;

    /** 当前天气枚举名（D 模块 P1 天气系统装配点写入；验收 §七十三
     *  {@code world_state.current_weather}，新档/未启用时为 null）。 */
    private String currentWeather;

    /** 当前事件类型枚举名（D 模块 P2 随机事件系统装配点写入；验收 §九十一
     *  {@code active_event.event_type}，新档/无事件时为 null）。 */
    private String currentEventType;

    /** 当前事件开始世界时间（游戏小时；验收 §九十一 {@code active_event.start_world_time}）。 */
    private long eventStartWorldTime;

    /** 当前事件结束世界时间（游戏小时；验收 §九十一 {@code active_event.end_world_time}）。 */
    private long eventEndWorldTime;

    /** 神秘商人指定作物枚举名（验收 §九十一 {@code active_event.target_crop_type}，可空）。 */
    private String eventTargetCropType;

    /** 事件附加数据（验收 §九十一 {@code active_event.payload}，可空）。 */
    private String eventPayload;

    /** 已解锁内容标识集合（P3 用于解锁/商店，P0 默认为空）。 */
    private final java.util.Set<String> unlocked = new java.util.LinkedHashSet<>();

    /** 全地图格子状态快照（A Farm 由本快照重建，Farm/Soil/Crop 派生）。 */
    private final java.util.List<PlotState> plots = new java.util.ArrayList<>();

    /** 装饰快照（P1 装饰系统，由 B 写入；P0 为空，对应 SQLite decoration 表）。 */
    private final java.util.List<DecorationState> decorations = new java.util.ArrayList<>();

    public GameState() {
        this(null, 0L);
    }

    public GameState(Player player, long gameDay) {
        this.player = player;
        this.gameDay = gameDay;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /** 当前游戏天数（新档为 0，由 D 模块 GameClock 在每日结算时写入）。 */
    public long getGameDay() {
        return gameDay;
    }

    public void setGameDay(long gameDay) {
        this.gameDay = gameDay;
    }

    /** 存档时 GameClock 的世界时间（ISO-8601），时钟记录时间前为 null。 */
    public String getCurrentWorldTime() {
        return currentWorldTime;
    }

    public void setCurrentWorldTime(String currentWorldTime) {
        this.currentWorldTime = currentWorldTime;
    }

    /**
     * 当前天气枚举名（{@code SUNNY}/{@code RAIN}/{@code DROUGHT}/{@code GREEN_RAIN}）。
     *
     * <p>对应 {@code world_state.current_weather}（验收 §七十三），以枚举名字符串存取，
     * 防止改动（D 模块 P1 文档 §4.1）。未启用天气系统或新档时为 null。
     *
     * @return 天气枚举名，可能为 null
     */
    public String getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(String currentWeather) {
        this.currentWeather = currentWeather;
    }

    /**
     * 当前事件类型枚举名（{@code METEOR_SHOWER}/{@code MYSTERY_MERCHANT}/
     * {@code ANIMAL_VISIT}/{@code RAINBOW_DAY}/{@code NONE}）。
     *
     * <p>对应 {@code active_event.event_type}（验收 §九十一），以枚举名字符串存取。
     * 无事件或新档时为 null。
     *
     * @return 事件枚举名，可能为 null
     */
    public String getCurrentEventType() {
        return currentEventType;
    }

    public void setCurrentEventType(String currentEventType) {
        this.currentEventType = currentEventType;
    }

    /** 当前事件开始世界时间（游戏小时；验收 §九十一）。 */
    public long getEventStartWorldTime() {
        return eventStartWorldTime;
    }

    public void setEventStartWorldTime(long eventStartWorldTime) {
        this.eventStartWorldTime = eventStartWorldTime;
    }

    /** 当前事件结束世界时间（游戏小时；验收 §九十一）。 */
    public long getEventEndWorldTime() {
        return eventEndWorldTime;
    }

    public void setEventEndWorldTime(long eventEndWorldTime) {
        this.eventEndWorldTime = eventEndWorldTime;
    }

    /** 神秘商人指定作物枚举名（验收 §九十一，可空）。 */
    public String getEventTargetCropType() {
        return eventTargetCropType;
    }

    public void setEventTargetCropType(String eventTargetCropType) {
        this.eventTargetCropType = eventTargetCropType;
    }

    /** 事件附加数据（验收 §九十一，可空）。 */
    public String getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(String eventPayload) {
        this.eventPayload = eventPayload;
    }

    /** 已解锁内容标识集合。 */
    public java.util.Set<String> getUnlocked() {
        return unlocked;
    }

    /** 全地图格子状态快照。 */
    public java.util.List<PlotState> getPlots() {
        return plots;
    }

    /** 装饰快照（P1 装饰系统对应 SQLite decoration 表，P0 为空）。 */
    public java.util.List<DecorationState> getDecorations() {
        return decorations;
    }
}
