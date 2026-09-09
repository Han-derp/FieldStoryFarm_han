package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.service.GameClock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** 正式游戏时钟：1现实分钟 = 1游戏小时，即60x。 */
public class RealGameClock implements GameClock {
    public static final double WORLD_SPEED = 60.0;

    private LocalDateTime currentWorldTime;

    public RealGameClock(LocalDateTime startWorldTime) {
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
