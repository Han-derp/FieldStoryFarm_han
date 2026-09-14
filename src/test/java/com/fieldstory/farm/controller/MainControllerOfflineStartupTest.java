package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.manager.SceneManager;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.OfflineSimulationResult;
import com.fieldstory.farm.service.OfflineSimulationService;
import com.fieldstory.farm.service.SaveService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E P2 启动集成接线单测：装配游戏时按《验收规范》§八十四固定顺序调用 B 的离线模拟。
 *
 * <p>只验证 E 的接线：注入的 {@link OfflineSimulationService} 会在装配期被调用恰好一次，
 * 且入参来自统一 {@link com.fieldstory.farm.model.GameClock#calculateOfflineDuration()}。
 * 离线模拟规则、离线日志与弹窗均属 B，不在本测试范围。
 */
class MainControllerOfflineStartupTest {

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX 工具包初始化超时");
    }

    private static <T> T onFxThread(Supplier<T> supplier) throws InterruptedException {
        AtomicReference<T> ref = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ref.set(supplier.get());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(20, TimeUnit.SECONDS), "JavaFX 线程执行超时");
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
        return ref.get();
    }

    private static final class InMemorySaveService implements SaveService {
        @Override
        public boolean hasSave() {
            return false;
        }

        @Override
        public void save(GameState state) {
            // 测试不落盘
        }

        @Override
        public GameState load() {
            return null;
        }
    }

    /** 记录调用参数的假离线模拟服务（B 侧接口的替身）；返回"无离线进度"结果以免触发落盘。 */
    private static final class RecordingOfflineService implements OfflineSimulationService {
        private final List<Long> calls = new ArrayList<>();

        @Override
        public OfflineSimulationResult simulate(long rawOfflineMinutes) {
            calls.add(rawOfflineMinutes);
            return new OfflineSimulationResult(rawOfflineMinutes, 0, 0, List.of());
        }
    }

    private static void injectWelcomeText(MainController controller) {
        try {
            Field field = MainController.class.getDeclaredField("welcomeText");
            field.setAccessible(true);
            field.set(controller, new Label());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("注入 welcomeText 失败", e);
        }
    }

    @Test
    void assemblyInvokesInjectedOfflineSimulationExactlyOnce() throws InterruptedException {
        RecordingOfflineService offlineService = new RecordingOfflineService();

        onFxThread(() -> {
            SceneManager.getInstance().assemble(new Pane(), 960, 640);
            MainController controller =
                    new MainController(new GameManager(new InMemorySaveService()));
            injectWelcomeText(controller);
            controller.setOfflineSimulationService(offlineService);
            controller.onNewGameButtonClick();
            return null;
        });

        assertEquals(1, offlineService.calls.size(),
                "装配游戏时应按固定启动顺序调用一次 B 的离线模拟");
        assertEquals(0L, offlineService.calls.get(0),
                "离线时长必须取自统一时钟 calculateOfflineDuration()，E 不得读取系统时间");
    }

    @Test
    void assemblyWithoutInjectedOfflineServiceStillSucceeds() throws InterruptedException {
        onFxThread(() -> {
            SceneManager.getInstance().assemble(new Pane(), 960, 640);
            MainController controller =
                    new MainController(new GameManager(new InMemorySaveService()));
            injectWelcomeText(controller);
            // 不注入离线服务：等价于本局无离线，装配必须照常完成
            controller.onNewGameButtonClick();
            return null;
        });
    }
}
