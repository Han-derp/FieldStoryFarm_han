package com.fieldstory.farm.persistence.dao;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.EventState;
import com.fieldstory.farm.model.EventType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 当前随机事件数据访问对象（E 模块 P2 DAO；验收规范 §九十一）。
 *
 * <p>正式业务 API 以 {@link EventState} 为边界；同时保留早期验收代码使用的
 * {@link ActiveEventRow} / insert / update 兼容入口。兼容层只做数据映射，
 * 不引入第二份事件状态，也不改变 active_event 单行表语义。
 */
public class ActiveEventDao {

    private static final int SINGLETON_ID = 1;

    private final Connection connection;

    public ActiveEventDao(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection 不能为空");
    }

    /** 正式 P2 入口：写入/覆盖当前事件快照。 */
    public void upsert(EventState state) throws SQLException {
        Objects.requireNonNull(state, "state 不能为空");
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO active_event(id, event_type, start_world_time, end_world_time,"
                        + " target_crop_type, payload) VALUES(?, ?, ?, ?, ?, ?)"
                        + " ON CONFLICT(id) DO UPDATE SET"
                        + " event_type = excluded.event_type,"
                        + " start_world_time = excluded.start_world_time,"
                        + " end_world_time = excluded.end_world_time,"
                        + " target_crop_type = excluded.target_crop_type,"
                        + " payload = excluded.payload")) {
            EventType type = state.getEventType() == null ? EventType.NONE : state.getEventType();
            ps.setInt(1, SINGLETON_ID);
            ps.setString(2, type.name());
            ps.setLong(3, state.getStartWorldTime());
            ps.setLong(4, state.getEndWorldTime());
            ps.setString(5, state.getTargetCropType() == null
                    ? null : state.getTargetCropType().name());
            ps.setString(6, state.getPayload());
            ps.executeUpdate();
        }
    }

    /**
     * 读取当前事件快照。
     *
     * <p>返回类型使用兼容行对象，但该对象同时实现 {@link EventState}，因此 E 的正式
     * {@code GameState.setActiveEvent(...)} 调用与旧测试的 row accessor 可同时工作。
     *
     * @return 事件快照；库中无记录返回 null
     */
    public ActiveEventRow find() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT event_type, start_world_time, end_world_time, target_crop_type, payload"
                        + " FROM active_event WHERE id = ?")) {
            ps.setInt(1, SINGLETON_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ActiveEventRow(
                        rs.getString("event_type"),
                        rs.getLong("start_world_time"),
                        rs.getLong("end_world_time"),
                        rs.getString("target_crop_type"),
                        rs.getString("payload"));
            }
        }
    }

    /** 兼容早期 DAO 测试：insert 与正式 upsert 等价。 */
    public void insert(ActiveEventRow row) throws SQLException {
        upsert(row);
    }

    /** 兼容早期 DAO 测试：单行表 update 与正式 upsert 等价。 */
    public void update(ActiveEventRow row) throws SQLException {
        upsert(row);
    }

    /** 删除事件快照（无事件 / 新档）。 */
    public void deleteAll() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM active_event")) {
            ps.executeUpdate();
        }
    }

    /**
     * P1/P2 早期验收使用的数据行兼容类型。
     *
     * <p>它实现 {@link EventState}，因此不是第二份领域模型，而只是 SQL 行与领域接口之间
     * 的可变适配器。{@code eventType()/targetCropType()} 保留字符串 accessor，
     * {@code getEventType()/getTargetCropType()} 提供正式枚举接口。
     */
    public static final class ActiveEventRow implements EventState {
        private String eventType;
        private long startWorldTime;
        private long endWorldTime;
        private String targetCropType;
        private String payload;

        public ActiveEventRow(String eventType, long startWorldTime, long endWorldTime,
                              String targetCropType, String payload) {
            this.eventType = eventType;
            this.startWorldTime = startWorldTime;
            this.endWorldTime = endWorldTime;
            this.targetCropType = targetCropType;
            this.payload = payload;
        }

        public String eventType() {
            return eventType;
        }

        public long startWorldTime() {
            return startWorldTime;
        }

        public long endWorldTime() {
            return endWorldTime;
        }

        public String targetCropType() {
            return targetCropType;
        }

        public String payload() {
            return payload;
        }

        @Override
        public EventType getEventType() {
            return parseEnum(EventType.class, eventType, EventType.NONE);
        }

        @Override
        public void setEventType(EventType eventType) {
            this.eventType = eventType == null ? null : eventType.name();
        }

        @Override
        public long getStartWorldTime() {
            return startWorldTime;
        }

        @Override
        public void setStartWorldTime(long startWorldTime) {
            this.startWorldTime = startWorldTime;
        }

        @Override
        public long getEndWorldTime() {
            return endWorldTime;
        }

        @Override
        public void setEndWorldTime(long endWorldTime) {
            this.endWorldTime = endWorldTime;
        }

        @Override
        public CropType getTargetCropType() {
            return parseEnum(CropType.class, targetCropType, null);
        }

        @Override
        public void setTargetCropType(CropType targetCropType) {
            this.targetCropType = targetCropType == null ? null : targetCropType.name();
        }

        @Override
        public String getPayload() {
            return payload;
        }

        @Override
        public void setPayload(String payload) {
            this.payload = payload;
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name, E fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name.trim());
        } catch (IllegalArgumentException unknown) {
            return fallback;
        }
    }
}
