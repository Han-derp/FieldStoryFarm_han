package com.fieldstory.farm.model;

import com.fieldstory.farm.model.impl.BasicGameClock;

/**
 * 游戏模型聚合（D 模块 P0：世界环境）。
 *
 * <p>依据《D模块 P0 接口与类设计文档》§三、《FSF_P0-P4 分阶段实现与验收规范》§5/§41/§42。
 *
 * <p>职责：聚合 {@link GameClock} 与 {@link Farm}，对外暴露统一时间读取与存档恢复入口。
 * 本类不直接处理生长、天气、事件等业务逻辑（由 Controller 协调 A 模块 GrowthService，验收规范 §3.1）。
 *
 * <p><b>团队裁决落实（原"矛盾报告"）</b>：
 * <ul>
 *   <li>裁决 ①：土地字段统一为 A 模块的 {@link Farm} 接口（原 D 文档写作 {@code LandGrid}，
 *       该类型在全项目不存在，已按 A 模块 §7.1 的 {@code Farm} 接口统一）。</li>
 *   <li>裁决 ②：本类<b>不</b>持有 {@code Player}（{@code Player} 归 B 模块，D 不越层持有），
 *       玩家数据由 E 模块装配层（GameManager）注入到需要的组件。</li>
 * </ul>
 */
public class FarmGameModel {

    /** 游戏时钟（聚合，1 对 1）。 */
    private final GameClock gameClock;

    /** 农田地图（聚合，1 对 1）；由 E 模块装配层注入，可为 null（P0 早期未装配时）。 */
    private Farm farm;

    /**
     * 默认构造：初始化 {@code gameClock = new BasicGameClock()}（第 1 天 06:00，验收规范 §5）。
     */
    public FarmGameModel() {
        this.gameClock = new BasicGameClock();
    }

    /**
     * 注入时钟构造（测试可注入 TestGameClock，规则 §八）。
     *
     * @param gameClock 游戏时钟
     */
    public FarmGameModel(GameClock gameClock) {
        this.gameClock = gameClock;
    }

    /**
     * 推进时间：只调用 {@code gameClock.tick()}，不含天气/事件/日结逻辑（验收规范 §10）。
     */
    public void tick() {
        gameClock.tick();
    }

    /**
     * 获取时钟引用（供 A 模块读取时间，跨模块接口约定 §11.1）。
     *
     * @return 游戏时钟
     */
    public GameClock getGameClock() {
        return gameClock;
    }

    /**
     * 获取农田地图（裁决 ①：类型为 A 模块的 {@link Farm} 接口）。
     *
     * @return 农田地图，未装配时返回 null
     */
    public Farm getFarm() {
        return farm;
    }

    /**
     * 设置农田地图（由 E 模块装配层注入）。
     *
     * @param farm 农田地图
     */
    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    /**
     * 获取当前总分钟数（存档用，验收规范 §41）。
     *
     * @return 总分钟数
     */
    public int getWorldTimeTotalMinutes() {
        return gameClock.getTotalMinutes();
    }

    /**
     * 恢复时间（存档用，验收规范 §41）。
     *
     * @param totalMinutes 总分钟数
     */
    public void restoreWorldTime(int totalMinutes) {
        gameClock.setTotalMinutes(totalMinutes);
    }
}
