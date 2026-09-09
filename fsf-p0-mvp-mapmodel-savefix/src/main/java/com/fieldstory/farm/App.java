package com.fieldstory.farm;

import com.fieldstory.farm.controller.FarmController;
import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.view.FarmView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** P0 MVP入口。 */
public class App extends Application {
    private static final java.time.Duration WORLD_PULSE_REAL_TIME = java.time.Duration.ofSeconds(1);

    private GameManager game;

    @Override
    public void start(Stage stage) {
        game = new GameManager();
        game.start();

        FarmController controller = new FarmController(game);
        FarmView view = new FarmView(game, controller);

        /*
         * P0唯一世界推进源。
         * 每次Timeline事件代表“1个标准现实秒”的推进脉冲。
         * 即使一次按钮/同步存档阻塞了JavaFX线程，也不会在下一帧按阻塞耗时补算，
         * 因而按钮无法造成世界时间跳变。
         */
        Timeline timer = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    game.advanceWorld(WORLD_PULSE_REAL_TIME);
                    view.refresh();
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        stage.setScene(new Scene(view.root(), 1280, 780));
        stage.setTitle("Field Story Farm - P0 MVP (12x12 MapTile model)");
        stage.setOnCloseRequest(e -> game.save());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
