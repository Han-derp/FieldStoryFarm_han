package com.fieldstory.farm.model;

/**
 * 玩家模型（占位示例，后续按业务扩展）。
 */
public class Player {

    /** 玩家姓名 */
    private String name;

    /** 金币数量 */
    private int gold;

    public Player() {
        this("农夫", 100);
    }

    public Player(String name, int gold) {
        this.name = name;
        this.gold = gold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
