package com.fieldstory.farm.service;

import com.fieldstory.farm.model.GameState;

public interface SaveService {
    void save(GameState state);
    GameState load();
    boolean hasSave();
}
