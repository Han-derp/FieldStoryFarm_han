package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.service.GameClock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * P0答辩用DemoClock。
 *
 * 正式规则：1现实分钟 = 1游戏小时 => 60x。
 * DemoClock再加速12倍 => 720x。
 *
 * 与旧实现不同：now() 不再基于 System.nanoTime() 实时计算。
 * 只有 advance() 才能真正改变 currentWorldTime，彻底保证按钮、刷新、保存不会推进时间。
 */
public class DemoGameClock implements GameClock {
    public static final double WORLD_SPEED = 720.0;

    private LocalDateTime currentWorldTime;

    public DemoGameClock(LocalDateTime startWorldTime) {
        this.currentWorldTime = Objects.requireNonNull(startWorldTime);
    }

    @Override
    public LocalDateTime now() {
        return currentWorldTime;
    }

    @Override
    public void advance(Duration realElapsed) {
        Objects.requireNonNull(realElapsed);
        if (realElapsed.isNegative()) {
            throw new IllegalArgumentException("realElapsed must not be negative");
        }

        long scaledNanos = Math.round(realElapsed.toNanos() * WORLD_SPEED);
        currentWorldTime = currentWorldTime.plusNanos(scaledNanos);
    }
}
