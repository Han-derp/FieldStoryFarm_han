package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * 主界面控制器（场景组装接线层）。
 *
 * <p>只负责：接收界面事件 → 调用 Service/Manager → 刷新界面。
 * 不包含任何业务规则；存档位置对 Controller 不可见（验收规范 §39），
 * 本控制器只依赖 {@link GameManager} 与 {@link GameState}。
 */
public class MainController {

    private final GameManager gameManager;

    @FXML
    private Label welcomeText;

    @FXML
    private Label statusText;

    @FXML
    private Button startButton;

    @FXML
    private Button saveExitButton;

    public MainController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @FXML
    private void initialize() {
        GameState state = gameManager.currentState();
        Player player = state.getPlayer();
        String mode = gameManager.hasSavedGame() ? "已从存档恢复" : "新游戏";
        welcomeText.setText("欢迎来到田野故事农场，%s！（%s）".formatted(player.getName(), mode));
        refreshStatus();
    }

    @FXML
    protected void onStartButtonClick() {
        GameState state = gameManager.currentState();
        Player player = state.getPlayer();
        welcomeText.setText("开始耕种吧！当前金币：%d".formatted(player.getGold()));
        refreshStatus();
    }

    @FXML
    protected void onSaveExitButtonClick() {
        // 完整集成后此处应传入 D 模块 GameClock.getWorldTime()；
        // 骨架阶段使用当前状态已记录的世界时间，避免越层调用系统时钟。
        GameState state = gameManager.currentState();
        gameManager.saveNow(state.getCurrentWorldTime());
        welcomeText.setText("已保存，正在退出…");
        Platform.exit();
    }

    private void refreshStatus() {
        GameState state = gameManager.currentState();
        Player player = state.getPlayer();
        String worldTime = state.getCurrentWorldTime() == null
                ? "未开始" : state.getCurrentWorldTime().toString();
        statusText.setText("金币：%d ｜ 世界时间：%s ｜ 每次关键操作与退出均自动保存".formatted(
                player.getGold(), worldTime));
    }
}
