package com.fieldstory.farm.model;

import com.fieldstory.farm.model.impl.BasicGameClock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * P0 FarmGameModel 测试（D 模块 P0 文档 §三、团队裁决 ①/②）。
 *
 * <p>覆盖：时钟聚合、tick 委托、存档恢复、土地字段类型为 A 的 {@link Farm} 接口、
 * 不持有 Player。
 */
class FarmGameModelTest {

    @Test
    void defaultConstructorAggregatesBasicGameClock() {
        FarmGameModel model = new FarmGameModel();
        assertNotNull(model.getGameClock());
        assertEquals(360, model.getWorldTimeTotalMinutes());
        assertEquals(1, model.getGameClock().getGameDay());
    }

    @Test
    void tickDelegatesToGameClock() {
        FarmGameModel model = new FarmGameModel();
        model.tick();
        assertEquals(370, model.getWorldTimeTotalMinutes());
    }

    @Test
    void restoreWorldTimeSetsClock() {
        FarmGameModel model = new FarmGameModel();
        model.restoreWorldTime(1800);
        assertEquals(1800, model.getWorldTimeTotalMinutes());
        assertEquals(2, model.getGameClock().getGameDay());
    }

    @Test
    void injectedClockIsUsed() {
        GameClock clock = new BasicGameClock(720);
        FarmGameModel model = new FarmGameModel(clock);
        assertSame(clock, model.getGameClock());
        assertEquals(720, model.getWorldTimeTotalMinutes());
    }

    @Test
    void farmFieldIsTypedAsFarmInterfaceAndDefaultsToNull() {
        FarmGameModel model = new FarmGameModel();
        assertNull(model.getFarm(), "未装配时土地字段应为 null");
        Farm farm = new StubFarm();
        model.setFarm(farm);
        assertSame(farm, model.getFarm());
    }

    /** 最小 Farm 桩，仅用于验证字段类型为 A 的 Farm 接口（裁决 ①）。 */
    private static final class StubFarm implements Farm {
        @Override
        public int getWidth() {
            return 12;
        }

        @Override
        public int getHeight() {
            return 12;
        }

        @Override
        public FarmPlot getPlot(int row, int col) {
            return FarmPlot.FARM_PLOT;
        }

        @Override
        public Soil getSoil(int row, int col) {
            return null;
        }

        @Override
        public boolean isFarmPlot(int row, int col) {
            return true;
        }
    }
}
