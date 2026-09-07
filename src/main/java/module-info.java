module com.example.fieldstoryfarm {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.fieldstoryfarm to javafx.fxml;
    exports com.example.fieldstoryfarm;

    // FXML 反射需要访问 controller 与 view 包
    opens com.example.fieldstoryfarm.controller to javafx.fxml;
    opens com.example.fieldstoryfarm.view to javafx.fxml;

    // javafx.graphics 需要反射实例化 Application 子类
    exports com.example.fieldstoryfarm.view to javafx.graphics;
}