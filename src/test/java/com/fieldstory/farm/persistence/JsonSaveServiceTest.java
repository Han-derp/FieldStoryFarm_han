package com.fieldstory.farm.persistence;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JsonSaveService 测试（验收规范 §四十三 必须包含 JsonSaveServiceTest）。
 *
 * <p>验证验收标准：
 * （1）操作完成后调用 SaveService.save() 将全部游戏状态写入 save.json；
 * （2）JSON 覆盖金币、种子库存、世界时间、土地作物全部字段（§41）；
 * （3）load() 恢复到退出瞬间，字段级往返一致。
 *
 * <p>依赖契约（待 A/B 模块合入后测试自动可用）：
 * A 模块 Farm/Soil/Crop 提供无参构造 + getter/setter（与后续 SQLite DAO 同一套 POJO 约定）；
 * B 模块 Player 提供 seedInventory 的 getter/setter（验收规范 §十八）。
 */
class JsonSaveServiceTest {

    private static final LocalDateTime WORLD_TIME =
            LocalDateTime.of(2026, 9, 9, 10, 30, 0);

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_roundTripKeepsEveryDocumentedField() {
        Path saveFile = tempDir.resolve("save.json");
        JsonSaveService service = new JsonSaveService(saveFile);
        GameState original = fixtureState();

        service.save(original);

        assertTrue(service.hasSave());
        assertTrue(Files.exists(saveFile));

        GameState restored = service.load();
        assertNotNull(restored);
        assertTrue(restored.samePlayerState(original));
        assertTrue(restored.sameWorldTime(original));
        assertFarmRestored(restored, original);
    }

    @Test
    void saveFileContainsAllRequiredJsonFields() throws IOException {
        Path saveFile = tempDir.resolve("save.json");
        JsonSaveService service = new JsonSaveService(saveFile);
        service.save(fixtureState());

        String json = Files.readString(saveFile, StandardCharsets.UTF_8);
        // 验收规范 §41 的 JSON 必须覆盖字段
        assertTrue(json.contains("\"gold\""));
        assertTrue(json.contains("\"seedInventory\""));
        assertTrue(json.contains("\"currentWorldTime\""));
        assertTrue(json.contains("\"cropUuid\""));
        assertTrue(json.contains("\"cropType\""));
        assertTrue(json.contains("\"growthStage\""));
        assertTrue(json.contains("\"growthProgress\""));
        assertTrue(json.contains("\"plantWorldTime\""));
        assertTrue(json.contains("\"manualWaterCount\""));
        assertTrue(json.contains("\"lastManualWaterGameDay\""));
        assertTrue(json.contains("\"state\""));
        assertTrue(json.contains("\"soils\""));
    }

    @Test
    void hasSave_falseAndLoadNull_whenFileMissing() {
        JsonSaveService service = new JsonSaveService(tempDir.resolve("no-save.json"));

        assertFalse(service.hasSave());
        assertNull(service.load());
    }

    @Test
    void save_createsMissingParentDirectories() {
        Path nested = tempDir.resolve("nested/data/save.json");
        JsonSaveService service = new JsonSaveService(nested);

        service.save(fixtureState());

        assertTrue(Files.exists(nested));
        assertTrue(service.hasSave());
    }

    // ------------------------------------------------------------------
    // fixtures
    // ------------------------------------------------------------------

    private GameState fixtureState() {
        Player player = new Player("农夫", 500);
        Map<CropType, Integer> seeds = new LinkedHashMap<>();
        seeds.put(CropType.WHEAT, 3);
        seeds.put(CropType.CORN, 2);
        player.setSeedInventory(seeds);

        Farm farm = new Farm();
        List<Soil> soils = farm.getSoils();
        soils.clear();

        Soil empty = new Soil();
        empty.setId(1L);
        empty.setRow(0);
        empty.setColumn(0);
        empty.setState(SoilState.EMPTY);
        empty.setCrop(null);
        soils.add(empty);

        Soil planted = new Soil();
        planted.setId(2L);
        planted.setRow(1);
        planted.setColumn(1);
        planted.setState(SoilState.PLANTED);
        planted.setCrop(fixtureCrop());
        soils.add(planted);

        return new GameState(player, farm, WORLD_TIME);
    }

    private Crop fixtureCrop() {
        Crop crop = new Crop();
        crop.setCropUuid(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        crop.setCropType(CropType.CARROT);
        crop.setGrowthStage(GrowthStage.SPROUT);
        crop.setGrowthProgress(25.5);
        crop.setPlantWorldTime(LocalDateTime.of(2026, 9, 8, 8, 0));
        crop.setManualWaterCount(1);
        crop.setLastManualWaterGameDay(LocalDate.of(2026, 9, 8));
        return crop;
    }

    private void assertFarmRestored(GameState restored, GameState original) {
        List<Soil> originalSoils = original.getFarm().getSoils();
        List<Soil> restoredSoils = restored.getFarm().getSoils();
        assertEquals(originalSoils.size(), restoredSoils.size());

        Soil o1 = originalSoils.get(0);
        Soil r1 = restoredSoils.get(0);
        assertNull(o1.getCrop());
        assertNull(r1.getCrop());
        assertEquals(o1.getId(), r1.getId());
        assertEquals(o1.getRow(), r1.getRow());
        assertEquals(o1.getColumn(), r1.getColumn());
        assertEquals(o1.getState(), r1.getState());

        Soil o2 = originalSoils.get(1);
        Soil r2 = restoredSoils.get(1);
        assertEquals(o2.getId(), r2.getId());
        assertEquals(o2.getRow(), r2.getRow());
        assertEquals(o2.getColumn(), r2.getColumn());
        assertEquals(o2.getState(), r2.getState());

        Crop oc = o2.getCrop();
        Crop rc = r2.getCrop();
        assertNotNull(oc);
        assertNotNull(rc);
        assertEquals(oc.getCropUuid(), rc.getCropUuid());
        assertEquals(oc.getCropType(), rc.getCropType());
        assertEquals(oc.getGrowthStage(), rc.getGrowthStage());
        assertEquals(oc.getGrowthProgress(), rc.getGrowthProgress(), 0.0001);
        assertEquals(oc.getPlantWorldTime(), rc.getPlantWorldTime());
        assertEquals(oc.getManualWaterCount(), rc.getManualWaterCount());
        assertEquals(oc.getLastManualWaterGameDay(), rc.getLastManualWaterGameDay());
    }
}
