package com.fieldstory.farm.manager;

import com.fieldstory.farm.model.OfflineSimulationResult;
import com.fieldstory.farm.model.impl.BasicGameClock;
import com.fieldstory.farm.service.OfflineSimulationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * E P2 启动集成「离线一段」接线单测（验收规范 §八十四固定启动顺序）。
 *
 * <p>只校验 E 的接线契约：未注入 B 的离线模拟时跳过；注入时把<b>统一时钟</b>给出的
 * 离线真实分钟原样交给 {@link OfflineSimulationService#simulate(long)}。
 * 离线成长/枯萎/事件算法与离线日志均属 B，不在本测试范围。
 */
class OfflineStartupStepTest {

    /** 记录调用参数的假离线模拟服务（B 侧接口的替身）。 */
    private static final class RecordingService implements OfflineSimulationService {
        private final List<Long> calls = new ArrayList<>();
        private final OfflineSimulationResult result;

        RecordingService(OfflineSimulationResult result) {
            this.result = result;
        }

        @Override
        public OfflineSimulationResult simulate(long rawOfflineMinutes) {
            calls.add(rawOfflineMinutes);
            return result;
        }
    }

    @Test
    void nullServiceSkipsOfflineSimulation() {
        assertNull(OfflineStartupStep.run(new BasicGameClock(), null),
                "B 未注入离线模拟时应直接跳过，返回 null");
    }

    @Test
    void passesUnifiedClockOfflineDurationToBService() {
        OfflineSimulationResult expected = new OfflineSimulationResult(0, 0, 0, List.of());
        RecordingService service = new RecordingService(expected);

        OfflineSimulationResult actual = OfflineStartupStep.run(new BasicGameClock(), service);

        assertSame(expected, actual, "应原样返回 B 的模拟结果");
        assertEquals(List.of(0L), service.calls,
                "离线时长必须取自统一时钟 calculateOfflineDuration()，E 不得自行读取系统时间");
    }
}
