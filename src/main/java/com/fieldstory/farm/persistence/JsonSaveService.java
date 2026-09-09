package com.fieldstory.farm.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.PlotState;
import com.fieldstory.farm.service.SaveService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * P0 JSON 临时存档实现（验收规范 §四十~§四十二；脚手架 §九 运行数据 data/）。
 *
 * <ul>
 *   <li>默认文件：{@code data/save.json}（.gitignore 已忽略 data/，本地存档不进仓库）；</li>
 *   <li>根节点保留 {@code version}（=1）与 {@code schema} 标识：P1 迁 SQLite 时
 *       按版本增量迁移、读旧 JSON 一次性导入，禁止原地改结构；</li>
 *   <li>保存内容：Player 经济（name/gold）、gameDay（对应 GameClock.getGameDay）、
 *       unlocked（已解锁内容）、plots（每块土地/作物状态，验收 §四十一）；</li>
 *   <li>状态值以字符串保存、作物以对象嵌套，不依赖 A/D 尚未交付的枚举类型；</li>
 *   <li>反序列化只恢复退出瞬间状态，不执行任何离线成长计算（离线模拟属 P2）。</li>
 * </ul>
 */
public class JsonSaveService implements SaveService {

    /** 当前存档结构版本；升级结构时必须递增并在加载时做兼容处理 */
    public static final int SAVE_VERSION = 1;

    /** 存档格式标识（区分未来 SQLite 正式存档） */
    public static final String SCHEMA = "P0-json";

    /** 默认存档文件（相对工程运行目录） */
    public static final String DEFAULT_SAVE_FILE = "data/save.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path saveFile;

    public JsonSaveService() {
        this(Paths.get(DEFAULT_SAVE_FILE));
    }

    public JsonSaveService(Path saveFile) {
        if (saveFile == null) {
            throw new IllegalArgumentException("saveFile 不能为空");
        }
        this.saveFile = saveFile;
    }

    /** 当前存档文件位置（供测试与 P1 迁移使用）。 */
    public Path getSaveFile() {
        return saveFile;
    }

    @Override
    public boolean hasSave() {
        return Files.isRegularFile(saveFile);
    }

    @Override
    public void save(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState 不能为空");
        }
        try {
            if (saveFile.getParent() != null) {
                Files.createDirectories(saveFile.getParent());
            }
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toRoot(state));
            Files.writeString(saveFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("存档写入失败: " + saveFile.toAbsolutePath(), e);
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
            throw new IllegalStateException("存档文件损坏或无法读取: " + saveFile.toAbsolutePath(), e);
        }
    }

    // ------------------------------------------------------------------
    // 序列化：GameState -> JSON
    // ------------------------------------------------------------------

    private ObjectNode toRoot(GameState state) {
        ObjectNode root = mapper.createObjectNode();
        root.put("version", SAVE_VERSION);
        root.put("schema", SCHEMA);
        root.put("savedAt", LocalDateTime.now().toString());
        root.put("gameDay", state.getGameDay());

        String worldTime = state.getCurrentWorldTime();
        if (worldTime == null) {
            root.putNull("currentWorldTime");
        } else {
            root.put("currentWorldTime", worldTime);
        }

        Player player = state.getPlayer();
        if (player != null) {
            ObjectNode playerNode = root.putObject("player");
            playerNode.put("name", player.getName());
            playerNode.put("gold", player.getGold());
        } else {
            root.putNull("player");
        }

        ArrayNode unlocked = root.putArray("unlocked");
        for (String key : state.getUnlocked()) {
            if (key != null) {
                unlocked.add(key);
            }
        }

        ArrayNode plots = root.putArray("plots");
        for (PlotState plot : state.getPlots()) {
            if (plot != null) {
                plots.add(toPlotNode(plot));
            }
        }
        return root;
    }

    private ObjectNode toPlotNode(PlotState plot) {
        ObjectNode node = mapper.createObjectNode();
        String plotId = (plot.getPlotId() == null || plot.getPlotId().isBlank())
                ? plot.getRow() + "," + plot.getColumn()
                : plot.getPlotId();
        node.put("plotId", plotId);
        node.put("row", plot.getRow());
        node.put("column", plot.getColumn());
        node.put("state", plot.getState());

        if (plot.hasCrop()) {
            ObjectNode crop = node.putObject("crop");
            crop.put("cropUuid", plot.getCropUuid());
            crop.put("cropType", plot.getCropType());
            crop.put("growthStage", plot.getGrowthStage());
            crop.put("growthProgress", plot.getGrowthProgress());
            crop.put("plantWorldTime", plot.getPlantWorldTime());
            crop.put("manualWaterCount", plot.getManualWaterCount());
            crop.put("lastManualWaterGameDay", plot.getLastManualWaterGameDay());
        } else {
            node.putNull("crop");
        }
        return node;
    }

    // ------------------------------------------------------------------
    // 反序列化：JSON -> GameState（仅恢复状态，不做任何成长计算）
    // ------------------------------------------------------------------

    private GameState toGameState(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new IllegalStateException("存档文件为空或不是 JSON 对象");
        }
        int version = root.path("version").asInt(-1);
        if (version != SAVE_VERSION) {
            throw new IllegalStateException("存档版本不兼容: 期望 version=" + SAVE_VERSION
                    + "，实际 version=" + version);
        }

        GameState state = new GameState();
        JsonNode playerNode = root.get("player");
        if (playerNode != null && playerNode.isObject()) {
            String name = playerNode.path("name").asText(null);
            int gold = playerNode.path("gold").asInt(0);
            state.setPlayer(new Player(name, gold));
        }
        state.setGameDay(root.path("gameDay").asLong(0L));

        JsonNode worldTime = root.get("currentWorldTime");
        if (worldTime != null && worldTime.isTextual()) {
            state.setCurrentWorldTime(worldTime.asText());
        }

        JsonNode unlocked = root.get("unlocked");
        if (unlocked != null && unlocked.isArray()) {
            for (JsonNode key : unlocked) {
                if (key != null && key.isTextual()) {
                    state.getUnlocked().add(key.asText());
                }
            }
        }

        JsonNode plots = root.get("plots");
        if (plots != null && plots.isArray()) {
            for (JsonNode plotNode : plots) {
                if (plotNode != null && plotNode.isObject()) {
                    state.getPlots().add(toPlotState(plotNode));
                }
            }
        }
        return state;
    }

    private PlotState toPlotState(JsonNode node) {
        PlotState plot = new PlotState();
        int row = node.path("row").asInt(0);
        int column = node.path("column").asInt(0);
        String plotId = node.path("plotId").asText(null);
        plot.setPlotId((plotId == null || plotId.isBlank()) ? row + "," + column : plotId);
        plot.setRow(row);
        plot.setColumn(column);
        plot.setState(node.path("state").asText(null));

        JsonNode crop = node.get("crop");
        if (crop != null && crop.isObject()) {
            plot.setCropUuid(crop.path("cropUuid").asText(null));
            plot.setCropType(crop.path("cropType").asText(null));
            plot.setGrowthStage(crop.path("growthStage").asText(null));
            plot.setGrowthProgress(crop.path("growthProgress").asDouble(0.0));
            plot.setPlantWorldTime(crop.path("plantWorldTime").asText(null));
            plot.setManualWaterCount(crop.path("manualWaterCount").asInt(0));
            plot.setLastManualWaterGameDay(crop.path("lastManualWaterGameDay").asText(null));
        }
        return plot;
    }
}
