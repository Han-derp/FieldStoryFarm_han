package com.fieldstory.farm.persistence;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.EventType;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.impl.BasicEventState;
import com.fieldstory.farm.persistence.dao.ActiveEventDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L8.3 存档验收：{@code active_event} 表字段与 E 模块对接可用，退出重进事件状态正确恢复。
 *
 * <p>覆盖验收规范 §九十一：{@code active_event} 五列
 * （{@code event_type}/{@code start_world_time}/{@code end_world_time}/
 * {@code target_crop_type}/{@code payload}）与 {@code EventState} 一一映射，
 * 且「游戏内事件仅在期间退出重进，事件记录不凭据消失」。
 */
class ActiveEventPersistenceTest {

    @TempDir
    Path tempDir;

    private DatabaseService database(String name) {
        return new DatabaseService(tempDir.resolve(name));
    }

    @Test
    void activeEventTableHasFiveSpecifiedColumns() throws Exception {
        DatabaseService db = database("schema.db");
        try (Connection connection = db.openConnection()) {
            assertTrue(columnExists(connection, "active_event", "event_type"));
            assertTrue(columnExists(connection, "active_event", "start_world_time"));
            assertTrue(columnExists(connection, "active_event", "end_world_time"));
            assertTrue(columnExists(connection, "active_event", "target_crop_type"));
            assertTrue(columnExists(connection, "active_event", "payload"));
        }
    }

    @Test
    void activeEventDaoRoundTripsAllFields() throws Exception {
        DatabaseService db = database("dao.db");
        try (Connection connection = db.openConnection()) {
            ActiveEventDao dao = new ActiveEventDao(connection);
            assertNull(dao.find(), "空表应返回 null");

            dao.insert(new ActiveEventDao.ActiveEventRow(
                    EventType.MYSTERY_MERCHANT.name(), 50L, 62L, CropType.CORN.name(), "reward"));
            ActiveEventDao.ActiveEventRow row = dao.find();
            assertNotNull(row);
            assertEquals("MYSTERY_MERCHANT", row.eventType());
            assertEquals(50L, row.startWorldTime());
            assertEquals(62L, row.endWorldTime());
            assertEquals("CORN", row.targetCropType());
            assertEquals("reward", row.payload());

            dao.update(new ActiveEventDao.ActiveEventRow(
                    EventType.METEOR_SHOWER.name(), 100L, 124L, null, null));
            ActiveEventDao.ActiveEventRow updated = dao.find();
            assertEquals("METEOR_SHOWER", updated.eventType());
            assertEquals(100L, updated.startWorldTime());
            assertEquals(124L, updated.endWorldTime());
            assertNull(updated.targetCropType());
            assertNull(updated.payload());

            dao.deleteAll();
            assertNull(dao.find());
        }
    }

    @Test
    void eventStateSurvivesExitAndRestart() {
        DatabaseService db = database("event.db");
        SqliteSaveService save = new SqliteSaveService(db, null);

        GameState state = new GameState(new Player("农夫", 500), 3L);
        state.setCurrentEventType(EventType.METEOR_SHOWER.name());
        state.setEventStartWorldTime(72L);
        state.setEventEndWorldTime(96L);
        save.save(state);

        // ---------- 退出重进 ----------
        SqliteSaveService reopened = new SqliteSaveService(db, null);
        GameState loaded = reopened.load();
        assertNotNull(loaded);
        assertEquals("METEOR_SHOWER", loaded.getCurrentEventType(),
                "事件类型应跨退出重进恢复（验收 §九十一）");
        assertEquals(72L, loaded.getEventStartWorldTime());
        assertEquals(96L, loaded.getEventEndWorldTime());
    }

    @Test
    void mysteryMerchantTargetCropSurvivesRestart() {
        DatabaseService db = database("merchant.db");
        SqliteSaveService save = new SqliteSaveService(db, null);

        GameState state = new GameState(new Player("农夫", 500), 4L);
        state.setCurrentEventType(EventType.MYSTERY_MERCHANT.name());
        state.setEventStartWorldTime(96L);
        state.setEventEndWorldTime(108L);
        state.setEventTargetCropType(CropType.CARROT.name());
        state.setEventPayload("double_price");
        save.save(state);

        GameState loaded = new SqliteSaveService(db, null).load();
        assertEquals("MYSTERY_MERCHANT", loaded.getCurrentEventType());
        assertEquals("CARROT", loaded.getEventTargetCropType(),
                "神秘商人指定作物应跨退出重进恢复");
        assertEquals("double_price", loaded.getEventPayload());
    }

    @Test
    void noEventWritesNoRowAndLoadsAsNull() {
        DatabaseService db = database("none.db");
        SqliteSaveService save = new SqliteSaveService(db, null);

        GameState state = new GameState(new Player("农夫", 500), 1L);
        // 不设置事件（默认无事件）
        save.save(state);

        GameState loaded = new SqliteSaveService(db, null).load();
        assertNull(loaded.getCurrentEventType(), "无事件时不应写入 active_event 行");
    }

    @Test
    void restoredEventStateDrivesEventService() {
        DatabaseService db = database("drive.db");
        SqliteSaveService save = new SqliteSaveService(db, null);

        GameState state = new GameState(new Player("农夫", 500), 3L);
        state.setCurrentEventType(EventType.RAINBOW_DAY.name());
        state.setEventStartWorldTime(48L);
        state.setEventEndWorldTime(72L);
        save.save(state);

        GameState loaded = new SqliteSaveService(db, null).load();

        // 用存档字段重建 EventState，验证与 D 模块 EventService 对接可用
        BasicEventState eventState = new BasicEventState(
                EventType.valueOf(loaded.getCurrentEventType()),
                loaded.getEventStartWorldTime(),
                loaded.getEventEndWorldTime());
        assertEquals(EventType.RAINBOW_DAY, eventState.getEventType());
        assertEquals(24L, eventState.getEndWorldTime() - eventState.getStartWorldTime(),
                "彩虹日持续 24 游戏小时（验收 §八十二）");
    }

    private static boolean columnExists(Connection connection, String table, String column)
            throws SQLException {
        try (var ps = connection.prepareStatement("PRAGMA table_info(" + table + ")");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
