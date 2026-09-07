package com.example.fieldstoryfarm.controller;

import com.example.fieldstoryfarm.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 主界面控制器。
 */
public class MainController {

    @FXML
    private Label welcomeText;

    private final Player player = new Player();

    @FXML
    private void initialize() {
        welcomeText.setText("欢迎来到田野故事农场，%s！".formatted(player.getName()));
    }

    @FXML
    protected void onStartButtonClick() {
        welcomeText.setText("开始耕种吧！当前金币：%d".formatted(player.getGold()));
    }
}
