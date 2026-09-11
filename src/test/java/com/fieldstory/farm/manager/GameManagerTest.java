package com.fieldstory.farm.manager;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.PlotState;
import com.fieldstory.farm.persistence.JsonSaveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P0 GameManager 测试：状态机切换、新游戏/读档、保存/退出生命周期。
 */
class GameManagerTest {

    @TempDir
    Path tempDir;

    private JsonSaveService jsonService(String name) {
        return new JsonSaveService(tempDir.resolve(name));
    }

    @Test
    void getInstanceReturnsSameSingleton() {
        assertSame(GameManager.getInstance(), GameManager.getInstance());
        assertNotNull(GameManager.getInstance());
    }

    @Test
    void freshManagerStaysInMainMenuUntilStarted() {
        GameManager gm = new GameManager(jsonService("a.json"));
        assertEquals(GamePhase.MAIN_MENU, gm.currentPhase());
        assertThrows(IllegalStateException.class, gm::currentState);
    }

    @Test
    void startWithoutSaveCreatesNewGameWithInitialGold() {
        GameManager gm = new GameManager(jsonService("none.json"));
        GameState state = gm.start();

        assertEquals(GamePhase.PLAYING, gm.currentPhase());
        assertNotNull(state.getPlayer());
        assertEquals(GameManager.INITIAL_GOLD, state.getPlayer().getGold());
        assertEquals(0L, state.getGameDay());
        assertTrue(state.getPlots().isEmpty());
        // 唯一种子库存归 Player（B §6.2），新档为空且非 null
        assertNotNull(state.getPlayer().getSeedInventory());
        assertTrue(state.getPlayer().getSeedInventory().isEmpty());
    }

    @Test
    void staticNewGameProvidesInitialState() {
        GameState state = GameManager.newGame();
        assertEquals(GameManager.INITIAL_GOLD, state.getPlayer().getGold());
        assertEquals(0L, state.getGameDay());
    }

    @Test
    void saveAndExitPersistsAndRestartRestores() {
        Path file = tempDir.resolve("persist.json");
        GameManager gm = new GameManager(new JsonSaveService(file));
        GameState state = gm.start();

        state.getPlayer().setGold(321);
        state.getPlayer().getSeedInventory().put(CropType.CARROT, 7);
        state.setGameDay(9L);
        PlotState plot = new PlotState();
        plot.setRow(0);
        plot.setColumn(0);
        plot.setState("TILLED");
        state.getPlots().add(plot);

        gm.saveAndExit();
        assertEquals(GamePhase.EXITING, gm.currentPhase());

        // 重启：新管理器 + 同一存档文件，应恢复到退出瞬间状态
        GameManager restarted = new GameManager(new JsonSaveService(file));
        assertEquals(GamePhase.MAIN_MENU, restarted.currentPhase());
        assertTrue(restarted.hasSavedGame());

        GameState loaded = restarted.start();
        assertEquals(GamePhase.PLAYING, restarted.currentPhase());
        assertEquals(321, loaded.getPlayer().getGold());
        assertEquals(9L, loaded.getGameDay());
        assertEquals(7, loaded.getPlayer().getSeedInventory().get(CropType.CARROT));
        assertEquals(1, loaded.getPlots().size());
        assertEquals("TILLED", loaded.getPlots().get(0).getState());
    }

    @Test
    void corruptSaveFallsBackToNewGameOnStart() throws Exception {
        Path file = tempDir.resolve("corrupt.json");
        Files.writeString(file, "{{{corrupt", StandardCharsets.UTF_8);
        GameManager gm = new GameManager(new JsonSaveService(file));

        GameState state = gm.start();
        assertEquals(GameManager.INITIAL_GOLD, state.getPlayer().getGold());
        assertEquals(GamePhase.PLAYING, gm.currentPhase());
    }

    @Test
    void saveNowBeforeStartThrows() {
        GameManager gm = new GameManager(jsonService("x.json"));
        assertThrows(IllegalStateException.class, gm::saveNow);
    }

    @Test
    void pauseResumeFollowStateMachine() {
        GameManager gm = new GameManager(jsonService("y.json"));
        // 主菜单不可直接暂停
        assertThrows(IllegalStateException.class, gm::pause);

        gm.start();
        gm.pause();
        assertEquals(GamePhase.PAUSED, gm.currentPhase());
        // 重复暂停非法
        assertThrows(IllegalStateException.class, gm::pause);

        gm.resume();
        assertEquals(GamePhase.PLAYING, gm.currentPhase());
        // 游戏中不可 resume
        assertThrows(IllegalStateException.class, gm::resume);

        // 暂停中退出也自动存档
        gm.pause();
        gm.saveAndExit();
        assertEquals(GamePhase.EXITING, gm.currentPhase());
    }
}
