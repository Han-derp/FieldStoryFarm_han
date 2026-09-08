package com.fieldstory.farm;

import com.fieldstory.farm.view.MainApplication;
import javafx.application.Application;

/**
 * 启动器：与 Application 分离，保证模块化与非模块化环境下均可直接运行。
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
