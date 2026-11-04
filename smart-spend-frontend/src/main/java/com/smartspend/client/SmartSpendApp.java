package com.smartspend.client;

import com.smartspend.client.util.SceneNavigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class SmartSpendApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.setStage(primaryStage);

        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/smartspend-logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        SceneNavigator.loadScene("/fxml/login.fxml", "Smart Spend - Personal Finance Management System");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
