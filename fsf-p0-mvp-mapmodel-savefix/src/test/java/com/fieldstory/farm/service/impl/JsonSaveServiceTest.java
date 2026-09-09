package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSaveServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripRestoresPlayerFarmCropAndClock() {
        Path save = tempDir.resolve("save.json");
        JsonSaveService service = new JsonSaveService(save);

        Player player = new Player();
        player.setGold(420);
        player.getSeedInventory().put(CropType.CARROT, 3);

        Farm farm = Farm.createP0Farm();
        Soil soil = farm.getSoil(0, 0);
        soil.setState(SoilState.PLANTED);
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 12, 0);
        soil.setCrop(new Crop(CropType.CORN, now.minusHours(10)));

        service.save(new GameState(player, farm, now));
        GameState restored = service.load();

        assertEquals(420, restored.getPlayer().getGold());
        assertEquals(3, restored.getPlayer().getSeedInventory().get(CropType.CARROT));
        assertEquals(now, restored.getCurrentWorldTime());
        assertEquals(144, restored.getFarm().getTiles().size());
        assertEquals(64, restored.getFarm().countTiles(com.fieldstory.farm.model.FarmPlot.FARM_PLOT));
        assertNull(restored.getFarm().getTile(0, 0).getSoil());
        assertEquals(SoilState.PLANTED, restored.getFarm().getSoil(0, 0).getState());
        assertNotNull(restored.getFarm().getSoil(0, 0).getCrop());
        assertEquals(CropType.CORN, restored.getFarm().getSoil(0, 0).getCrop().getCropType());
    }
    @Test
    void legacySoilsJsonShouldMigrateToFullMapTiles() throws Exception {
        Path save = tempDir.resolve("legacy-save.json");
        String legacyJson = """
                {
                  "player": {
                    "name": "Player",
                    "gold": 495,
                    "seedInventory": {"WHEAT": 1, "CORN": 0, "CARROT": 0}
                  },
                  "farm": {
                    "soils": [
                      {"id": 1, "row": 0, "column": 0, "state": "TILLED", "crop": null}
                    ]
                  },
                  "currentWorldTime": "2026-01-01T09:00:00"
                }
                """;
        Files.writeString(save, legacyJson);

        GameState restored = new JsonSaveService(save).load();

        assertEquals(144, restored.getFarm().getTiles().size());
        assertEquals(64, restored.getFarm().allSoils().size());
        assertEquals(SoilState.TILLED, restored.getFarm().getSoil(0, 0).getState());
        assertNull(restored.getFarm().getTile(0, 0).getSoil());
        assertEquals(495, restored.getPlayer().getGold());
    }

    @Test
    void serializedMapTileShouldUseOnlyPersistedFields() throws Exception {
        Path save = tempDir.resolve("map-tile-schema.json");
        JsonSaveService service = new JsonSaveService(save);

        service.save(new GameState(
                new Player(),
                Farm.createP0Farm(),
                LocalDateTime.of(2026, 1, 1, 8, 0)
        ));

        String json = Files.readString(save);

        assertTrue(json.contains("\"plotType\""));
        assertFalse(json.contains("\"farmPlot\""),
                "isFarmPlot() is a convenience query and must not become a JSON property");

        GameState restored = service.load();
        assertEquals(144, restored.getFarm().getTiles().size());
        assertEquals(64, restored.getFarm().countTiles(com.fieldstory.farm.model.FarmPlot.FARM_PLOT));
    }

    @Test
    void saveProducedByPreviousMapModelVersionWithFarmPlotFieldShouldStillLoad() throws Exception {
        Path save = tempDir.resolve("previous-mapmodel-save.json");
        JsonSaveService service = new JsonSaveService(save);

        service.save(new GameState(
                new Player(),
                Farm.createP0Farm(),
                LocalDateTime.of(2026, 1, 1, 8, 0)
        ));

        String json = Files.readString(save);
        json = json.replaceFirst(
                "\"plotType\"\s*:\s*\"DECORATION_AREA\"",
                "\"plotType\" : \"DECORATION_AREA\", \"farmPlot\" : false"
        );
        Files.writeString(save, json);

        GameState restored = service.load();

        assertEquals(144, restored.getFarm().getTiles().size());
        assertEquals(64, restored.getFarm().countTiles(com.fieldstory.farm.model.FarmPlot.FARM_PLOT));
    }

}
