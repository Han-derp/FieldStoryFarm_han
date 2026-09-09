package com.fieldstory.farm.view;

import com.fieldstory.farm.config.AppConfig;
import com.fieldstory.farm.util.FxmlUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 应用入口：负责加载主界面并显示窗口。
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(FxmlUtil.load(this.getClass(), AppConfig.MAIN_VIEW_FXML).load(),
                AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        stage.setTitle(AppConfig.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}
