package com.fieldstory.farm.view;

import com.fieldstory.farm.config.AppConfig;
import com.fieldstory.farm.controller.MainController;
import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.persistence.JsonSaveService;
import com.fieldstory.farm.service.SaveService;
import com.fieldstory.farm.util.FxmlUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 应用入口：负责场景组装（模块分工.md E 行 P0：场景组装）。
 *
 * <p>启动流程（对应脚手架 §十二 入口程序 + 验收规范 §42 退出行为）：
 * <pre>
 * 启动
 *  ↓
 * 创建 JsonSaveService（P0 临时 JSON 存档）
 *  ↓
 * 创建 GameManager 并 start()：有存档→恢复退出瞬间；无存档→新建
 *  ↓
 * 加载主界面（FXML），注入 GameManager
 *  ↓
 * 显示窗口；关闭窗口 → 保存当前状态并记录世界时间 → 退出
 * </pre>
 * 启动加载阶段不执行任何离线作物成长计算（离线推进属 P2）。
 */
public class MainApplication extends Application {

    private GameManager gameManager;

    @Override
    public void start(Stage stage) throws IOException {
        // P0：JSON 临时存档；P1 起替换为 SqliteSaveService，业务层调用方式不变（§40）。
        SaveService saveService = new JsonSaveService();
        gameManager = new GameManager(saveService);

        // 启动即恢复：有存档→恢复退出瞬间状态；无存档→新建（金币 500）。
        gameManager.start();

        FXMLLoader loader = FxmlUtil.load(MainApplication.class, AppConfig.MAIN_VIEW_FXML);
        loader.setController(new MainController(gameManager));
        Parent root = loader.load();

        Scene scene = new Scene(root, AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        stage.setTitle(AppConfig.APP_TITLE);
        stage.setScene(scene);

        // 退出行为：保存当前状态 + 记录当前世界时间（验收规范 §42）。
        // 完整集成后应读取 D 模块 GameClock.getWorldTime() 作为世界时间快照。
        stage.setOnCloseRequest(event -> gameManager.saveAndExit(currentWorldTime()));

        stage.show();
    }

    /** 世界时间来源：骨架阶段暂取状态内时间；完整集成后由 D 模块 GameClock 提供。 */
    private LocalDateTime currentWorldTime() {
        GameState state = gameManager.currentState();
        return state == null ? null : state.getCurrentWorldTime();
    }

    @Override
    public void stop() {
        // 兜底：任何路径关闭应用都完整保存一次。
        if (gameManager != null) {
            try {
                gameManager.saveAndExit(currentWorldTime());
            } catch (RuntimeException e) {
                // 存档失败不阻塞进程退出；日志完善属 P2 LogService 范围。
                System.err.println("退出保存失败: " + e.getMessage());
            }
        }
    }
}
