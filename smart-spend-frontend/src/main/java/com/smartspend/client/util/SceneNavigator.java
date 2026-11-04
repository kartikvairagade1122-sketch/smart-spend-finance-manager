package com.smartspend.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void loadScene(String fxmlPath, String title) {
        try {
            URL url = SceneNavigator.class.getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML view not found: " + fxmlPath);
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/css/theme.css").toExternalForm());

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Could not load screen: " + e.getMessage());
        }
    }

    public static Parent loadView(String fxmlPath) throws IOException {
        URL url = SceneNavigator.class.getResource(fxmlPath);
        if (url == null) {
            throw new IOException("FXML view not found: " + fxmlPath);
        }
        return FXMLLoader.load(url);
    }
}
