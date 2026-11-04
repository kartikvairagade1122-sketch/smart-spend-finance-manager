package com.smartspend.client.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class AlertHelper {

    public static void showInfo(String title, String message) {
        runOnFx(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            return null;
        });
    }

    public static void showWarning(String title, String message) {
        runOnFx(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            return null;
        });
    }

    public static void showError(String title, String message) {
        runOnFx(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            return null;
        });
    }

    public static boolean showConfirmation(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        } else {
            FutureTask<Boolean> task = new FutureTask<>(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                Optional<ButtonType> result = alert.showAndWait();
                return result.isPresent() && result.get() == ButtonType.OK;
            });
            Platform.runLater(task);
            try {
                return task.get();
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static void runOnFx(Callable<Void> action) {
        if (Platform.isFxApplicationThread()) {
            try {
                action.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    action.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
