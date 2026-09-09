package com.fieldstory.farm.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.PlotState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P0 JsonSaveService 测试（验收规范 §四十三 至少 JsonSaveServiceTest）。
 */
class JsonSaveServiceTest {

    @TempDir
    Path tempDir;

    private JsonSaveService service(String name) {
        return new JsonSaveService(tempDir.resolve(name));
    }

    @Test
    void loadReturnsNullWhenNoSaveExists() {
        JsonSaveService svc = service("missing/save.json");
        assertFalse(svc.hasSave());
        assertNull(svc.load());
    }

    @Test
    void saveCreatesFileWithVersionAndSchema() throws Exception {
        JsonSaveService svc = service("save.json");
        GameState state = new GameState(new Player("农夫A", 500), 3L);
        svc.save(state);

        Path file = tempDir.resolve("save.json");
        assertTrue(svc.hasSave());
        assertTrue(Files.isRegularFile(file));

        JsonNode root = new ObjectMapper().readTree(Files.readString(file, StandardCharsets.UTF_8));
        assertEquals(1, root.path("version").asInt());
        assertEquals("P0-json", root.path("schema").asText());
        assertEquals(3L, root.path("gameDay").asLong());
        assertEquals("农夫A", root.path("player").path("name").asText());
        assertEquals(500, root.path("player").path("gold").asInt());
    }

    @Test
    void saveCreatesParentDirectories() {
        JsonSaveService svc = service("nested/dir/save.json");
        GameState state = new GameState(new Player("农夫", 500), 0L);
        svc.save(state);
        assertTrue(Files.isRegularFile(tempDir.resolve("nested/dir/save.json")));
    }

    @Test
    void roundTripPreservesPlayerDayUnlockedAndPlots() throws Exception {
        JsonSaveService svc = service("roundtrip.json");

        GameState state = new GameState(new Player("测试农夫", 888), 12L);
        state.setCurrentWorldTime("2026-09-09T08:30:00");
        state.getUnlocked().add("shop");
        state.getUnlocked().add("land-2x2");

        PlotState growing = new PlotState();
        growing.setPlotId("special-1");
        growing.setRow(5);
        growing.setColumn(6);
        growing.setState("GROWING");
        growing.setCropUuid("uuid-1");
        growing.setCropType("WHEAT");
        growing.setGrowthStage("SPROUT");
        growing.setGrowthProgress(0.35);
        growing.setPlantWorldTime("2026-09-09T10:00:00");
        growing.setManualWaterCount(1);
        growing.setLastManualWaterGameDay("2");
        state.getPlots().add(growing);

        // 无 plotId：反序列化后按 "row,column" 补全
        PlotState idle = new PlotState();
        idle.setRow(2);
        idle.setColumn(2);
        idle.setState("EMPTY");
        state.getPlots().add(idle);

        svc.save(state);
        GameState loaded = svc.load();

        // Player
        assertEquals("测试农夫", loaded.getPlayer().getName());
        assertEquals(888, loaded.getPlayer().getGold());
        // 天数、世界时间与已解锁
        assertEquals(12L, loaded.getGameDay());
        assertEquals("2026-09-09T08:30:00", loaded.getCurrentWorldTime());
        assertEquals(java.util.Set.of("shop", "land-2x2"), loaded.getUnlocked());
        // 土地快照
        assertEquals(2, loaded.getPlots().size());

        PlotState p0 = loaded.getPlots().get(0);
        assertEquals("special-1", p0.getPlotId());
        assertEquals(5, p0.getRow());
        assertEquals(6, p0.getColumn());
        assertEquals("GROWING", p0.getState());
        assertTrue(p0.hasCrop());
        assertEquals("uuid-1", p0.getCropUuid());
        assertEquals("WHEAT", p0.getCropType());
        assertEquals("SPROUT", p0.getGrowthStage());
        assertEquals(0.35, p0.getGrowthProgress(), 1e-9);
        assertEquals("2026-09-09T10:00:00", p0.getPlantWorldTime());
        assertEquals(1, p0.getManualWaterCount());
        assertEquals("2", p0.getLastManualWaterGameDay());

        PlotState p1 = loaded.getPlots().get(1);
        assertEquals("2,2", p1.getPlotId());
        assertEquals(2, p1.getRow());
        assertEquals(2, p1.getColumn());
        assertEquals("EMPTY", p1.getState());
        assertFalse(p1.hasCrop());
    }

    @Test
    void roundTripOfEmptyState() throws Exception {
        JsonSaveService svc = service("empty.json");
        GameState state = new GameState();
        state.setGameDay(0);
        svc.save(state);

        GameState loaded = svc.load();
        assertNull(loaded.getPlayer());
        assertEquals(0L, loaded.getGameDay());
        assertNull(loaded.getCurrentWorldTime());
        assertTrue(loaded.getUnlocked().isEmpty());
        assertTrue(loaded.getPlots().isEmpty());
    }

    @Test
    void saveRejectsNullState() {
        JsonSaveService svc = service("null.json");
        assertThrows(IllegalArgumentException.class, () -> svc.save(null));
    }

    @Test
    void loadCorruptFileThrows() throws Exception {
        Path file = tempDir.resolve("corrupt.json");
        Files.writeString(file, "not-json{{{", StandardCharsets.UTF_8);
        JsonSaveService svc = new JsonSaveService(file);
        assertTrue(svc.hasSave());
        assertThrows(IllegalStateException.class, svc::load);
    }

    @Test
    void loadUnsupportedVersionThrows() throws Exception {
        Path file = tempDir.resolve("future.json");
        Files.writeString(file, "{\"version\":2,\"schema\":\"P0-json\",\"player\":null}", StandardCharsets.UTF_8);
        JsonSaveService svc = new JsonSaveService(file);
        IllegalStateException ex = assertThrows(IllegalStateException.class, svc::load);
        assertTrue(ex.getMessage().contains("版本"));
    }
}
