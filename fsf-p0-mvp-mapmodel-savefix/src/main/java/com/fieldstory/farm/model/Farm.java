package com.fieldstory.farm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 完整12x12地图Model。
 *
 * 地图坐标：0..11。
 * 中心8x8种植区：地图坐标2..9。
 * Soil自身row/column继续使用种植区局部坐标0..7，便于P0现有Service与测试保持稳定。
 */
public class Farm {
    public static final int MAP_SIZE = 12;
    public static final int FARM_SIZE = 8;
    public static final int FARM_OFFSET = 2;

    private List<MapTile> tiles = new ArrayList<>();

    public Farm() {}

    public static Farm createP0Farm() {
        Farm farm = new Farm();
        long soilId = 1L;

        for (int mapRow = 0; mapRow < MAP_SIZE; mapRow++) {
            for (int mapCol = 0; mapCol < MAP_SIZE; mapCol++) {
                if (isFarmMapCoordinate(mapRow, mapCol)) {
                    int farmRow = mapRow - FARM_OFFSET;
                    int farmCol = mapCol - FARM_OFFSET;
                    Soil soil = new Soil(soilId++, farmRow, farmCol);
                    farm.tiles.add(new MapTile(
                            mapRow,
                            mapCol,
                            FarmPlot.FARM_PLOT,
                            soil
                    ));
                } else {
                    /*
                     * 文档只固定“外围2格宽为功能区”，未固定P0中SHOP/SHOWCASE的精确坐标。
                     * 因此P0统一按不可交互功能区占位，不擅自发明正式布局。
                     * P1/P3确定地图布局后，只需要调整MapTile.plotType，不需要重构Farm/Soil。
                     */
                    farm.tiles.add(new MapTile(
                            mapRow,
                            mapCol,
                            FarmPlot.DECORATION_AREA,
                            null
                    ));
                }
            }
        }

        return farm;
    }

    public List<MapTile> getTiles() {
        return tiles;
    }

    public void setTiles(List<MapTile> tiles) {
        this.tiles = tiles == null ? new ArrayList<>() : tiles;
    }

    /** 根据12x12全局地图坐标读取格子。 */
    public MapTile getTile(int mapRow, int mapColumn) {
        checkMapCoordinate(mapRow, mapColumn);
        int index = mapRow * MAP_SIZE + mapColumn;

        if (tiles.size() == MAP_SIZE * MAP_SIZE) {
            MapTile candidate = tiles.get(index);
            if (candidate.getRow() == mapRow && candidate.getColumn() == mapColumn) {
                return candidate;
            }
        }

        return tiles.stream()
                .filter(tile -> tile.getRow() == mapRow && tile.getColumn() == mapColumn)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "missing map tile at (" + mapRow + "," + mapColumn + ")"
                ));
    }

    /**
     * 根据中心8x8种植区的局部坐标读取Soil。
     * P0旧调用 getSoil(0,0)..getSoil(7,7) 无需改变。
     */
    public Soil getSoil(int farmRow, int farmColumn) {
        if (farmRow < 0 || farmRow >= FARM_SIZE || farmColumn < 0 || farmColumn >= FARM_SIZE) {
            throw new IllegalArgumentException("farm coordinate must be within 0..7");
        }
        MapTile tile = getTile(farmRow + FARM_OFFSET, farmColumn + FARM_OFFSET);
        if (tile.getPlotType() != FarmPlot.FARM_PLOT || tile.getSoil() == null) {
            throw new IllegalStateException("farm plot must contain soil");
        }
        return tile.getSoil();
    }

    /** Service遍历全部农田时使用；返回只读列表，避免外部替换Farm内部结构。 */
    public List<Soil> allSoils() {
        List<Soil> soils = new ArrayList<>(FARM_SIZE * FARM_SIZE);
        for (MapTile tile : tiles) {
            if (tile.getPlotType() == FarmPlot.FARM_PLOT && tile.getSoil() != null) {
                soils.add(tile.getSoil());
            }
        }
        return Collections.unmodifiableList(soils);
    }

    public long countTiles(FarmPlot type) {
        return tiles.stream().filter(tile -> tile.getPlotType() == type).count();
    }

    public static boolean isFarmMapCoordinate(int mapRow, int mapColumn) {
        return mapRow >= FARM_OFFSET
                && mapRow < FARM_OFFSET + FARM_SIZE
                && mapColumn >= FARM_OFFSET
                && mapColumn < FARM_OFFSET + FARM_SIZE;
    }

    private static void checkMapCoordinate(int row, int column) {
        if (row < 0 || row >= MAP_SIZE || column < 0 || column >= MAP_SIZE) {
            throw new IllegalArgumentException("map coordinate must be within 0..11");
        }
    }
}
