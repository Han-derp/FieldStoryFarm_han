package com.fieldstory.farm.service.impl;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoGameClockTest {

    @Test
    void readingClockDoesNotAdvanceWorldTime() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 8, 0);
        DemoGameClock clock = new DemoGameClock(start);

        assertEquals(start, clock.now());
        assertEquals(start, clock.now());
        assertEquals(start, clock.now());
    }

    @Test
    void oneRealSecondPulseAdvancesTwelveGameMinutes() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 8, 0);
        DemoGameClock clock = new DemoGameClock(start);

        clock.advance(Duration.ofSeconds(1));

        assertEquals(start.plusMinutes(12), clock.now());
    }
}
