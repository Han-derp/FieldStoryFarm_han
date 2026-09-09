package com.fieldstory.farm.manager;

import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.service.InMemorySaveService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GameManager 生命周期测试（验收规范 §42、§44：新建→退出→重启恢复）。
 *
 * <p>对应验收标准：
 * （2）退出时保存金币、种子库存、GameClock 世界时间并记录退出世界时间；
 * （3）重启加载恢复到退出瞬间，不执行任何离线成长计算（本测试加载路径不触发任何成长逻辑）。
 */
class GameManagerTest {

    private static final LocalDateTime EXIT_WORLD_TIME =
            LocalDateTime.of(2026, 9, 9, 10, 30);

    @Test
    void startWithoutSave_createsNewGameWith500Gold() {
        InMemorySaveService saveService = new InMemorySaveService(false);
        GameManager manager = new GameManager(saveService);

        assertFalse(manager.hasSavedGame());
        GameState state = manager.start();

        // 验收流程 ②：新建游戏金币 = 500（验收规范 §三十五）
        assertNotNull(state);
        assertNotNull(state.getPlayer());
        assertEquals(500, state.getPlayer().getGold());
        // 新档土地应为空场（中心 8×8 EMPTY 由 A 模块 Farm 模型保证）
        assertNotNull(state.getFarm());
        // 新档尚无世界时间（时间由 D 模块 GameClock 启动后接管）
        assertNull(state.getCurrentWorldTime());
    }

    @Test
    void saveNow_recordsExitWorldTimeIntoState() {
        InMemorySaveService saveService = new InMemorySaveService(false);
        GameManager manager = new GameManager(saveService);
        manager.start();

        // 模拟“操作完成自动保存 / 退出保存”入口
        manager.saveAndExit(EXIT_WORLD_TIME);

        GameState saved = saveService.lastSaved();
        assertNotNull(saved);
        // 退出世界时间被记录
        assertTrue(saved.sameWorldTime(manager.currentState()));
        assertEquals(EXIT_WORLD_TIME, manager.currentState().getCurrentWorldTime());
    }

    @Test
    void restartWithSave_restoresExactlyToExitMoment() {
        InMemorySaveService saveService = new InMemorySaveService(false);
        GameManager first = new GameManager(saveService);
        first.start();
        first.saveAndExit(EXIT_WORLD_TIME);
        GameState exitState = first.currentState();

        // 重启：同一存档，走“有存档 → load()”路径
        GameManager second = new GameManager(saveService);
        assertTrue(second.hasSavedGame());
        GameState restored = second.start();

        assertNotNull(restored);
        assertTrue(restored.samePlayerState(exitState));
        assertTrue(restored.sameWorldTime(exitState));
        // 恢复即退出瞬间状态：不做离线成长，世界时间不前进
        assertEquals(EXIT_WORLD_TIME, restored.getCurrentWorldTime());
    }
}
