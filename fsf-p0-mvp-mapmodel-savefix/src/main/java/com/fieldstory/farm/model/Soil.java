package com.fieldstory.farm.model;

public class Soil {
    private long id;
    private int row;
    private int column;
    private SoilState state = SoilState.EMPTY;
    private Crop crop;

    public Soil() {}

    public Soil(long id, int row, int column) {
        this.id = id;
        this.row = row;
        this.column = column;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
    public SoilState getState() { return state; }
    public void setState(SoilState state) { this.state = state; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
}
