package com.fieldstory.farm.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * 玩家资源状态对象（B 模块 Player 模型，见 B 模块 §10）。
 *
 * <p>P0 至少保存：{@code gold} 金币与 {@code seedInventory} 种子库存。
 * <b>唯一种子库存</b>即本字段，禁止在别处（如 GameState）再维护第二份库存状态
 * （B 模块 §6.2）。种子数量的变化应经 B 的 EconomyService，本类的
 * {@link #setSeedInventory(Map)} 仅供状态恢复/序列化使用。
 *
 * <p><b>新档金币请勿直接 {@code new Player()}</b>：无参构造为占位默认值（100），
 * 新游戏请统一走 {@code GameManager.getInstance().start()/startNewGame()} 或
 * {@code GameManager.newGame()}（初始金币 500，见 B 模块 §9 / E 模块 §C3）。
 */
public class Player {

    /** 玩家姓名 */
    private String name;

    /** 金币数量 */
    private int gold;

    /**
     * 种子库存：作物类型 → 持有数量（唯一种子库存，B 模块 §6.2/§10.1）。
     * 使用 EnumMap 保证枚举键紧凑有序，且 {@link #getSeedInventory()} 恒不返回 null。
     */
    private Map<CropType, Integer> seedInventory = new EnumMap<>(CropType.class);

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

    /** 种子库存（作物类型 → 数量）；可写集合，缺失作物视为 0，恒不为 null。 */
    public Map<CropType, Integer> getSeedInventory() {
        return seedInventory;
    }

    /**
     * 仅用于状态恢复/序列化。
     * 正常游戏业务禁止直接调用，种子变化应经过 EconomyService。
     * 传入 null 时重置为空库存，保证 {@link #getSeedInventory()} 恒不为 null。
     */
    public void setSeedInventory(Map<CropType, Integer> seedInventory) {
        this.seedInventory = (seedInventory == null)
                ? new EnumMap<>(CropType.class)
                : seedInventory;
    }
}
