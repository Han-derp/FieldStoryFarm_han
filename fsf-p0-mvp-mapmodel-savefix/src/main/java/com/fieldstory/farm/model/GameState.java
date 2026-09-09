package com.fieldstory.farm.model;

import java.time.LocalDateTime;

public class GameState {
    private Player player;
    private Farm farm;
    private LocalDateTime currentWorldTime;

    public GameState() {}

    public GameState(Player player, Farm farm, LocalDateTime currentWorldTime) {
        this.player = player;
        this.farm = farm;
        this.currentWorldTime = currentWorldTime;
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public LocalDateTime getCurrentWorldTime() { return currentWorldTime; }
    public void setCurrentWorldTime(LocalDateTime currentWorldTime) { this.currentWorldTime = currentWorldTime; }
}
