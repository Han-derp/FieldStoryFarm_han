package com.fieldstory.farm.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * SQLite 结构版本迁移器（E 模块 P1；验收规范 §七十一~§七十五）。
 *
 * <p><b>为什么要它</b>：P1 起 {@code data/farm.db} 是唯一正式存档，若结构版本与字段/表随时间
 * 演进，旧存档必须能原样升级，否则玩家重建农场或丢档。这里用 SQLite 自带的
 * {@code PRAGMA user_version} 记录<b>当前结构版本</b>，
 * 启动时把低于程序版本的旧库逐级升级到新版（每步迁移只做增量 DDL，不重建已有表）。
 *
 * <p><b>设计约束</b>
 * <ul>
 *   <li>每个 {@link MigrationStep} 的 {@code version} 是「执行完该步后对应的版本号」；</li>
 *   <li>只执行版本号大于当前 {@code user_version} 的步骤，因此对同一库重复运行是幂等的；</li>
 *   <li>若库版本高于程序支持的 {@link #SCHEMA_VERSION}（用户装了更新程序），
 *       直接首次拒绝打开，避免用旧程序写入新结构。</li>
 * </ul>
 *
 * <p><b>v1 建表清单（验收规范 §七十三 最低表 + E 持久化内部表）</b>：
 * player / player_seed / unlocked / farm / soil / crop / decoration / world_state / meta。
 * 其中 player_seed、unlocked、meta 是 E 持久化内部表（种子背包、已解锁内容、迁移标记），
 * 不属于独立游戏系统。
 *
 * <p><b>v2 建表清单（P2 随机事件系统，验收规范 §九十一）</b>：
 * active_event（当前生效事件，退出重进不丢失）。
 */
public final class SchemaMigrator {

    /** 当前支持的数据库结构版本（新增表/字段时递增 +1 并追加迁移步骤）。 */
    public static final int SCHEMA_VERSION = 2;

    /** 全部迁移步骤，按版本递增。 */
    private static final List<MigrationStep> STEPS = List.of(stepToV1(), stepToV2());

    private SchemaMigrator() {
        // 工具类，禁止实例化
    }

    /** 读取数据库当前结构版本（{@code PRAGMA user_version}，全新库为 0）。 */
    public static int readVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** 将数据库升级到最新 {@link #SCHEMA_VERSION}，幂等（重复运行不做任何事）。 */
    public static void migrate(Connection connection) throws SQLException {
        int current = readVersion(connection);
        if (current > SCHEMA_VERSION) {
            throw new IllegalStateException("数据库结构版本(" + current + ")高于程序支持版本("
                    + SCHEMA_VERSION + ")，请使用更新版本的程序");
        }
        for (MigrationStep step : STEPS) {
            if (step.version() > current) {
                for (String ddl : step.statements()) {
                    try (Statement statement = connection.createStatement()) {
                        statement.executeUpdate(ddl);
                    }
                }
                setVersion(connection, step.version());
                current = step.version();
            }
        }
    }

    /** 写入结构版本号（{@code PRAGMA user_version} 不支持占位符，版本为 int 常量，无注入风险）。 */
    private static void setVersion(Connection connection, int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("PRAGMA user_version = " + version);
        }
    }

    /** v0 → v1：首次建立 P1 全表（IF NOT EXISTS 保证对半成品库也安全）。 */
    private static MigrationStep stepToV1() {
        return new MigrationStep(1, List.of(
                "CREATE TABLE IF NOT EXISTS player ("
                        + " id INTEGER PRIMARY KEY CHECK (id = 1),"
                        + " name TEXT,"
                        + " gold INTEGER NOT NULL CHECK (gold >= 0))",
                "CREATE TABLE IF NOT EXISTS player_seed ("
                        + " crop_type TEXT PRIMARY KEY,"
                        + " quantity INTEGER NOT NULL CHECK (quantity >= 0))",
                "CREATE TABLE IF NOT EXISTS unlocked ("
                        + " unlocked_key TEXT PRIMARY KEY)",
                "CREATE TABLE IF NOT EXISTS farm ("
                        + " id INTEGER PRIMARY KEY CHECK (id = 1),"
                        + " map_rows INTEGER NOT NULL,"
                        + " map_cols INTEGER NOT NULL)",
                "CREATE TABLE IF NOT EXISTS soil ("
                        + " soil_id INTEGER PRIMARY KEY,"
                        + " plot_id TEXT,"
                        + " row_index INTEGER NOT NULL,"
                        + " col_index INTEGER NOT NULL,"
                        + " state TEXT)",
                "CREATE TABLE IF NOT EXISTS crop ("
                        + " crop_uuid TEXT PRIMARY KEY,"
                        + " soil_id INTEGER NOT NULL REFERENCES soil(soil_id) ON DELETE CASCADE,"
                        + " crop_type TEXT,"
                        + " growth_stage TEXT,"
                        + " growth_progress REAL NOT NULL DEFAULT 0,"
                        + " plant_world_time TEXT,"
                        + " manual_water_count INTEGER NOT NULL DEFAULT 0,"
                        + " last_manual_water_game_day TEXT)",
                "CREATE TABLE IF NOT EXISTS decoration ("
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + " decoration_type TEXT NOT NULL,"
                        + " row_index INTEGER NOT NULL,"
                        + " col_index INTEGER NOT NULL)",
                // world_state 有固定五列（验收规范 §七十三）：current_world_time/last_real_time/
                // current_weather/current_day_index/random_seed，P2 离线模拟与天气复用此结构
                "CREATE TABLE IF NOT EXISTS world_state ("
                        + " id INTEGER PRIMARY KEY CHECK (id = 1),"
                        + " current_world_time TEXT,"
                        + " last_real_time TEXT,"
                        + " current_weather TEXT,"
                        + " current_day_index INTEGER NOT NULL DEFAULT 0,"
                        + " random_seed INTEGER)",
                // meta：E 持久化内部键值（JSON 一次性迁移标记等），不承载任何游戏业务
                "CREATE TABLE IF NOT EXISTS meta ("
                        + " meta_key TEXT PRIMARY KEY,"
                        + " meta_value TEXT NOT NULL)"));
    }

    /**
     * v1 → v2：P2 随机事件系统新增 {@code active_event} 表（验收规范 §九十一）。
     *
     * <p>五列与 {@code EventState} 一一对应：{@code event_type}（枚举 {@code name()}）、
     * {@code start_world_time}、{@code end_world_time}（游戏小时）、
     * {@code target_crop_type}（神秘商人指定作物，可空）、{@code payload}（附加数据，可空）。
     * 单人存档约定该表恒为 0 行或 1 行（{@code id = 1}）。
     */
    private static MigrationStep stepToV2() {
        return new MigrationStep(2, List.of(
                "CREATE TABLE IF NOT EXISTS active_event ("
                        + " id INTEGER PRIMARY KEY CHECK (id = 1),"
                        + " event_type TEXT,"
                        + " start_world_time INTEGER NOT NULL DEFAULT 0,"
                        + " end_world_time INTEGER NOT NULL DEFAULT 0,"
                        + " target_crop_type TEXT,"
                        + " payload TEXT)"));
    }

    /** 单步迁移：执行完 {@code version} 的 DDL 后，结构版本应等于 {@code version}。 */
    private record MigrationStep(int version, List<String> statements) {
    }
}
