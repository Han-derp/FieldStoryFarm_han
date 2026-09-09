package com.fieldstory.farm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 12x12农场地图中的一个逻辑格。
 *
 * P0约束：只有 FARM_PLOT 持有 Soil；外围功能区在P0仅展示、不可操作。
 */
@JsonIgnoreProperties(value = "farmPlot")
public class MapTile {
    private int row;
    private int column;
    private FarmPlot plotType;
    private Soil soil;

    public MapTile() {}

    public MapTile(int row, int column, FarmPlot plotType, Soil soil) {
        this.row = row;
        this.column = column;
        this.plotType = plotType;
        this.soil = soil;
    }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }

    public FarmPlot getPlotType() { return plotType; }
    public void setPlotType(FarmPlot plotType) { this.plotType = plotType; }

    public Soil getSoil() { return soil; }
    public void setSoil(Soil soil) { this.soil = soil; }

    @JsonIgnore
    public boolean isFarmPlot() {
        return plotType == FarmPlot.FARM_PLOT;
    }
}
