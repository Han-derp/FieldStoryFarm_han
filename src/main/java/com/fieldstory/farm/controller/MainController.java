package com.fieldstory.farm.controller;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 主界面控制器（E 场景组装：接入 GameManager 的开始/继续与手动存档）。
 */
public class MainController {

    @FXML
    private Label welcomeText;

    /** 全局唯一游戏管理器（单例） */
    private final GameManager gameManager = GameManager.getInstance();

    @FXML
    private void initialize() {
        welcomeText.setText("欢迎来到田野故事农场！");
    }

    /**
     * 开始 / 继续游戏：有存档恢复退出瞬间状态，无存档则新建游戏（金币 500）。
     * 随后挂载农场场景（FarmBootstrap：菜地挂 CENTER、临时商店挂 RIGHT）。
     */
    @FXML
    protected void onStartButtonClick() {
        boolean hasSave = gameManager.hasSavedGame();
        GameState state = gameManager.start();
        FarmBootstrap.mountFarmScene();
        Player player = state.getPlayer();
        String gold = (player == null) ? "?" : String.valueOf(player.getGold());
        welcomeText.setText("开始耕种吧！当前金币：%s  游戏天数：%d%s".formatted(
                gold, state.getGameDay(), hasSave ? "（已恢复存档）" : "（新游戏）"));
    }

    /** 手动存档入口：保存当前进度（退出/刷新时另有自动存档兜底）。 */
    @FXML
    protected void onSaveButtonClick() {
        try {
            gameManager.saveNow();
            welcomeText.setText("进度已保存！");
        } catch (IllegalStateException e) {
            welcomeText.setText("尚无进行中的游戏，请先点击“开始游戏”。");
        }
    }
}
