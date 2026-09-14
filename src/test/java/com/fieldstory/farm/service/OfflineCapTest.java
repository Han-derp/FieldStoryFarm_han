package com.fieldstory.farm.service;

import com.fieldstory.farm.util.OfflineTimePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * B 模块 P2 离线上限规则测试。
 */
class OfflineCapTest {

    @Test
    void thirtyRealMinutesAdvanceThirtyGameHours() {
        long effective = OfflineTimePolicy.effectiveMinutes(30L);

        assertEquals(30L, effective);
        assertEquals(30L, OfflineTimePolicy.toGameHours(effective));
    }

    @Test
    void eightRealHoursAreCappedAtSeventyTwoGameHours() {
        long rawMinutes = 8L * 60L;
        long effective = OfflineTimePolicy.effectiveMinutes(rawMinutes);

        assertEquals(72L, effective);
        assertEquals(72L, OfflineTimePolicy.toGameHours(effective));
    }

    @Test
    void exactlySeventyTwoMinutesAreNotReduced() {
        assertEquals(
                72L,
                OfflineTimePolicy.effectiveMinutes(72L)
        );
    }

    @Test
    void zeroOfflineDurationStaysZero() {
        assertEquals(
                0L,
                OfflineTimePolicy.effectiveMinutes(0L)
        );
        assertEquals(
                0L,
                OfflineTimePolicy.toGameHours(0L)
        );
    }

    @Test
    void negativeOfflineDurationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> OfflineTimePolicy.effectiveMinutes(-1L)
        );
    }
}
