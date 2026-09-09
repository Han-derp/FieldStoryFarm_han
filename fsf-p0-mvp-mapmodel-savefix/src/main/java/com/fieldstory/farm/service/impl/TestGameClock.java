package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.service.GameClock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** 自动测试用可控时钟。 */
public class TestGameClock implements GameClock {
    private LocalDateTime currentWorldTime;

    public TestGameClock(LocalDateTime startWorldTime) {
        this.currentWorldTime = Objects.requireNonNull(startWorldTime);
    }

    @Override
    public LocalDateTime now() {
        return currentWorldTime;
    }

    /** TestClock默认1:1解释realElapsed，便于确定性测试。 */
    @Override
    public void advance(Duration realElapsed) {
        advanceGameTime(realElapsed);
    }

    public void advanceGameTime(Duration gameElapsed) {
        Objects.requireNonNull(gameElapsed);
        if (gameElapsed.isNegative()) {
            throw new IllegalArgumentException("gameElapsed must not be negative");
        }
        currentWorldTime = currentWorldTime.plus(gameElapsed);
    }
}
