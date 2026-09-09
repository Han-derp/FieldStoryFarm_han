package com.fieldstory.farm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fieldstory.farm.model.Farm;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.service.SaveService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JsonSaveService implements SaveService {
    private final Path savePath;
    private final ObjectMapper mapper;

    public JsonSaveService(Path savePath) {
        this.savePath = savePath;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(GameState state) {
        try {
            if (savePath.getParent() != null) Files.createDirectories(savePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(savePath.toFile(), state);
        } catch (IOException e) {
            throw new IllegalStateException("save failed", e);
        }
    }

    @Override
    public GameState load() {
        try {
            JsonNode root = mapper.readTree(savePath.toFile());
            migrateLegacyP0FarmIfNeeded(root);
            return mapper.treeToValue(root, GameState.class);
        } catch (IOException e) {
            throw new IllegalStateException("load failed", e);
        }
    }

    /**
     * 兼容早期备用MVP的 `farm.soils` JSON结构。
     * 数据迁移属于Persistence职责，不放进Farm Model。
     */
    private void migrateLegacyP0FarmIfNeeded(JsonNode root) {
        JsonNode farmNode = root.get("farm");
        if (!(farmNode instanceof ObjectNode farmObject)) return;
        if (farmObject.has("tiles") || !farmObject.has("soils")) return;

        List<Soil> legacySoils = mapper.convertValue(
                farmObject.get("soils"),
                new TypeReference<List<Soil>>() {}
        );

        Farm migrated = Farm.createP0Farm();
        for (Soil soil : legacySoils) {
            if (soil == null) continue;
            int row = soil.getRow();
            int column = soil.getColumn();
            if (row < 0 || row >= Farm.FARM_SIZE || column < 0 || column >= Farm.FARM_SIZE) continue;
            migrated.getTile(row + Farm.FARM_OFFSET, column + Farm.FARM_OFFSET).setSoil(soil);
        }

        farmObject.remove("soils");
        farmObject.set("tiles", mapper.valueToTree(migrated.getTiles()));
    }

    @Override
    public boolean hasSave() {
        return Files.isRegularFile(savePath);
    }
}
