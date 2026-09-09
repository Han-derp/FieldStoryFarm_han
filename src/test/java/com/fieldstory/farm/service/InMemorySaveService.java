package com.fieldstory.farm.service;

import com.fieldstory.farm.model.GameState;

/**
 * 测试用内存存档实现：不落盘，仅验证 GameManager 生命周期编排。
 */
public class InMemorySaveService implements SaveService {

    private GameState stored;
    private boolean hasSave;

    public InMemorySaveService(boolean hasSave) {
        this.hasSave = hasSave;
    }

    public InMemorySaveService() {
        this(false);
    }

    @Override
    public void save(GameState state) {
        this.stored = state;
        this.hasSave = true;
    }

    @Override
    public GameState load() {
        return stored;
    }

    @Override
    public boolean hasSave() {
        return hasSave;
    }

    /** 最近一次保存的状态。 */
    public GameState lastSaved() {
        return stored;
    }
}
