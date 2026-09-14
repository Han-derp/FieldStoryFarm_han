package com.fieldstory.farm.persistence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * 当前生效事件数据访问对象（E 模块 P2 DAO；验收规范 §九十一 active_event）。
 *
 * <p>负责 {@code active_event} 表，固定承载验收规范 §九十一 要求的五列：
 * {@code event_type}、{@code start_world_time}、{@code end_world_time}、
 * {@code target_crop_type}、{@code payload}。
 *
 * <p>设计原则（验收规范 §九十一）：游戏内事件仅在期间退出重进，事件记录不凭据消失。
 * 因此存档装配时由本 DAO 读出的行恢复 D 模块的 {@code EventState}。
 *
 * <p>单人存档约定 {@code active_event} 表恒为 0 行或 1 行（id=1）。
 */
public class ActiveEventDao {

    private final Connection connection;

    public ActiveEventDao(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection 不能为空");
    }

    /**
     * 当前生效事件行。
     *
     * @param eventType       事件类型枚举名（可为 null，表示无事件）
     * @param startWorldTime  开始世界时间（游戏小时）
     * @param endWorldTime    结束世界时间（游戏小时）
     * @param targetCropType  神秘商人指定作物枚举名（可为 null）
     * @param payload         事件附加数据（可为 null）
     */
    public record ActiveEventRow(
            String eventType,
            long startWorldTime,
            long endWorldTime,
            String targetCropType,
            String payload) {
    }

    /** 插入当前事件行（id=1）。 */
    public void insert(ActiveEventRow row) throws SQLException {
        Objects.requireNonNull(row, "row 不能为空");
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO active_event(id, event_type, start_world_time, end_world_time,"
                        + " target_crop_type, payload) VALUES(1, ?, ?, ?, ?, ?)")) {
            bind(ps, row);
            ps.executeUpdate();
        }
    }

    /** 更新当前事件行。 */
    public void update(ActiveEventRow row) throws SQLException {
        Objects.requireNonNull(row, "row 不能为空");
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE active_event SET event_type = ?, start_world_time = ?, end_world_time = ?,"
                        + " target_crop_type = ?, payload = ? WHERE id = 1")) {
            bind(ps, row);
            ps.executeUpdate();
        }
    }

    /**
     * 读取当前事件。
     *
     * @return 当前事件行；无记录返回 {@code null}
     */
    public ActiveEventRow find() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT event_type, start_world_time, end_world_time, target_crop_type, payload"
                        + " FROM active_event WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
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

    /** 删除当前事件行。 */
    public void deleteAll() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM active_event")) {
            ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, ActiveEventRow row) throws SQLException {
        ps.setString(1, row.eventType());
        ps.setLong(2, row.startWorldTime());
        ps.setLong(3, row.endWorldTime());
        if (row.targetCropType() == null) {
            ps.setNull(4, Types.VARCHAR);
        } else {
            ps.setString(4, row.targetCropType());
        }
        ps.setString(5, row.payload());
    }
}
