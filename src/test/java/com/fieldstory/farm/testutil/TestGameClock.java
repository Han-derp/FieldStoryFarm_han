package com.fieldstory.farm.testutil;

import com.fieldstory.farm.manager.GameClock;

/**
 * {@link GameClock} 测试桩（《D模块 P0 接口与类设计文档》§1.2 签名子集）。
 *
 * <p>A 模块设计文档 §12.3 约定 D 提供 TestGameClock 供我方单测；
 * D 交付前由本桩代行，随 {@code manager.GameClock} 临时桩一并删除/切换。
 *
 * <p>gameDay / gameHour 均可 set，供播种时刻计算单测使用
 * （plantWorldTime = gameDay × 24 + gameHour，决策 D14）。
 */
public class TestGameClock implements GameClock {

    /** 当前游戏日（可 set） */
    private int gameDay;

    /** 当前游戏小时（可 set） */
    private int gameHour;

    /** 设置当前游戏日（测试用）。 */
    public void setGameDay(int gameDay) {
        this.gameDay = gameDay;
    }

    /** 设置当前游戏小时（测试用）。 */
    public void setGameHour(int gameHour) {
        this.gameHour = gameHour;
    }

    @Override
    public int getGameDay() {
        return gameDay;
    }

    @Override
    public int getGameHour() {
        return gameHour;
    }
}
