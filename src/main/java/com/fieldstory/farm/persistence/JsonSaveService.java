package com.fieldstory.farm.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fieldstory.farm.model.Crop;
import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.GrowthStage;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import com.fieldstory.farm.service.SaveService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * P0 JSON 临时存档实现（验收规范 §39-§41）。
 *
 * <p>职责：
 * <ul>
 *   <li>把 {@link GameState} 写入 JSON（默认 {@code data/save.json}，脚手架 §九 运行数据目录）；</li>
 *   <li>从 JSON 恢复完整状态到退出瞬间；</li>
 *   <li>存档位置只属于本持久化实现，Controller 不得感知（§39）；</li>
 *   <li>P1 起由 SqliteSaveService 替换，业务层调用方式不变（§40）。</li>
 * </ul>
 *
 * <p>本实现不做任何离线作物成长计算——离线模拟属 P2，禁止提前实现（计划书 §P0禁止清单；
 * 验收规范 §42 “退出后不推进世界”）。
 */
public class JsonSaveService implements SaveService {

    /** 架构分层：Persistence 负责存档实现（计划书 §3.1） */
    private static final String SCHEMA = "P0-save";

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path saveFile;

    public JsonSaveService() {
        this(Paths.get("data", "save.json"));
    }

    public JsonSaveService(Path saveFile) {
        if (saveFile == null) {
            throw new IllegalArgumentException("saveFile 不能为空");
        }
        this.saveFile = saveFile;
    }

    /** 当前存档文件位置（供测试与迁移使用）。 */
    public Path getSaveFile() {
        return saveFile;
    }

    @Override
    public void save(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState 不能为空");
        }
        try {
            Files.createDirectories(saveFile.getParent());
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toRoot(state));
            Files.writeString(saveFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("存档写入失败: " + saveFile, e);
        }
    }

    @Override
    public GameState load() {
        if (!hasSave()) {
            return null;
        }
        try {
            JsonNode root = mapper.readTree(Files.readString(saveFile, StandardCharsets.UTF_8));
            return toGameState(root);
        } catch (IOException e) {
            throw new IllegalStateException("存档读取失败: " + saveFile, e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("存档文件损坏或版本不符: " + saveFile, e);
        }
    }

    @Override
    public boolean hasSave() {
        return Files.isRegularFile(saveFile);
    }

    // ------------------------------------------------------------------
    // 序列化：GameState -> JSON（字段以验收规范 §41 为准）
    // ------------------------------------------------------------------

    private ObjectNode toRoot(GameState state) {
        ObjectNode root = mapper.createObjectNode();
        root.put("schema", SCHEMA);
        if (state.getCurrentWorldTime() != null) {
            root.put("currentWorldTime", state.getCurrentWorldTime().toString());
        } else {
            root.putNull("currentWorldTime");
        }

        Player player = state.getPlayer();
        if (player != null) {
            root.set("player", toPlayerNode(player));
        }

        Farm farm = state.getFarm();
        if (farm != null) {
            ArrayNode soils = root.putArray("soils");
            for (Soil soil : farm.getSoils()) {
                if (soil != null) {
                    soils.add(toSoilNode(soil));
                }
            }
        }
        return root;
    }

    private ObjectNode toPlayerNode(Player player) {
        ObjectNode node = mapper.createObjectNode();
        node.put("name", player.getName());
        node.put("gold", player.getGold());
        ObjectNode seeds = node.putObject("seedInventory");
        Map<CropType, Integer> inventory = player.getSeedInventory();
        if (inventory != null) {
            for (Map.Entry<CropType, Integer> entry : inventory.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    seeds.put(entry.getKey().name(), entry.getValue());
                }
            }
        }
        return node;
    }

    private ObjectNode toSoilNode(Soil soil) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", soil.getId());
        node.put("row", soil.getRow());
        node.put("column", soil.getColumn());
        node.put("state", soil.getState().name());
        if (soil.getCrop() == null) {
            node.putNull("crop");
        } else {
            node.set("crop", toCropNode(soil.getCrop()));
        }
        return node;
    }

    private ObjectNode toCropNode(Crop crop) {
        ObjectNode node = mapper.createObjectNode();
        if (crop.getCropUuid() != null) {
            node.put("cropUuid", crop.getCropUuid().toString());
        }
        if (crop.getCropType() != null) {
            node.put("cropType", crop.getCropType().name());
        }
        if (crop.getGrowthStage() != null) {
            node.put("growthStage", crop.getGrowthStage().name());
        }
        node.put("growthProgress", crop.getGrowthProgress());
        if (crop.getPlantWorldTime() != null) {
            node.put("plantWorldTime", crop.getPlantWorldTime().toString());
        }
        node.put("manualWaterCount", crop.getManualWaterCount());
        if (crop.getLastManualWaterGameDay() != null) {
            node.put("lastManualWaterGameDay", crop.getLastManualWaterGameDay().toString());
        }
        return node;
    }

    // ------------------------------------------------------------------
    // 反序列化：JSON -> GameState（仅恢复状态，不执行任何成长计算）
    // ------------------------------------------------------------------

    private GameState toGameState(JsonNode root) {
        Player player = null;
        if (root.has("player") && root.get("player").isObject()) {
            player = toPlayer(root.get("player"));
        }
        Farm farm = toFarm(root.get("soils"));
        LocalDateTime worldTime = null;
        if (root.has("currentWorldTime") && root.get("currentWorldTime").isTextual()) {
            worldTime = LocalDateTime.parse(root.get("currentWorldTime").asText());
        }
        return new GameState(player, farm, worldTime);
    }

    private Player toPlayer(JsonNode node) {
        String name = node.path("name").asText(null);
        int gold = node.path("gold").asInt(0);
        Player player = new Player(name, gold);

        Map<CropType, Integer> seeds = new LinkedHashMap<>();
        JsonNode seedsNode = node.get("seedInventory");
        if (seedsNode != null && seedsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = seedsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                seeds.put(CropType.valueOf(field.getKey()), field.getValue().asInt(0));
            }
        }
        player.setSeedInventory(seeds);
        return player;
    }

    private Farm toFarm(JsonNode soilsNode) {
        Farm farm = new Farm();
        List<Soil> soils = farm.getSoils();
        soils.clear();
        if (soilsNode != null && soilsNode.isArray()) {
            for (JsonNode soilNode : soilsNode) {
                if (soilNode.isObject()) {
                    soils.add(toSoil(soilNode));
                }
            }
        }
        return farm;
    }

    private Soil toSoil(JsonNode node) {
        Soil soil = new Soil();
        soil.setId(node.path("id").asLong(0));
        soil.setRow(node.path("row").asInt(0));
        soil.setColumn(node.path("column").asInt(0));
        soil.setState(SoilState.valueOf(node.path("state").asText("EMPTY")));
        JsonNode cropNode = node.get("crop");
        if (cropNode != null && cropNode.isObject()) {
            soil.setCrop(toCrop(cropNode));
        }
        return soil;
    }

    private Crop toCrop(JsonNode node) {
        Crop crop = new Crop();
        String uuid = node.path("cropUuid").asText(null);
        if (uuid != null) {
            crop.setCropUuid(UUID.fromString(uuid));
        }
        String cropType = node.path("cropType").asText(null);
        if (cropType != null) {
            crop.setCropType(CropType.valueOf(cropType));
        }
        String stage = node.path("growthStage").asText(null);
        if (stage != null) {
            crop.setGrowthStage(GrowthStage.valueOf(stage));
        }
        crop.setGrowthProgress(node.path("growthProgress").asDouble(0.0));
        String plantTime = node.path("plantWorldTime").asText(null);
        if (plantTime != null) {
            crop.setPlantWorldTime(LocalDateTime.parse(plantTime));
        }
        crop.setManualWaterCount(node.path("manualWaterCount").asInt(0));
        String lastWaterDay = node.path("lastManualWaterGameDay").asText(null);
        if (lastWaterDay != null) {
            crop.setLastManualWaterGameDay(LocalDate.parse(lastWaterDay));
        }
        return crop;
    }
}
