package com.smartspend.client.controller;

import com.smartspend.client.service.AuthServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label avatarBigLetter;
    @FXML private Label profileFullName;
    @FXML private Label profileUsername;
    @FXML private Label profileEmail;

    @FXML private Label pwdMessageLabel;
    @FXML private PasswordField currentPwdField;
    @FXML private PasswordField newPwdField;
    @FXML private PasswordField confirmNewPwdField;

    private final AuthServiceClient authClient = new AuthServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String fullName = SessionManager.getInstance().getFullName();
        String username = SessionManager.getInstance().getUsername();
        String email = SessionManager.getInstance().getEmail();

        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : username;
        profileFullName.setText(displayName != null ? displayName : "User");
        profileUsername.setText("@" + (username != null ? username : "user"));
        profileEmail.setText(email != null ? email : "Not available");

        if (displayName != null && !displayName.isEmpty()) {
            avatarBigLetter.setText(displayName.substring(0, 1).toUpperCase());
        }
    }

    @FXML
    public void handleUpdatePassword() {
        pwdMessageLabel.setVisible(false);
        pwdMessageLabel.setManaged(false);

        String currentPwd = currentPwdField.getText();
        String newPwd = newPwdField.getText();
        String confirmPwd = confirmNewPwdField.getText();

        if (currentPwd == null || currentPwd.isEmpty() ||
            newPwd == null || newPwd.isEmpty() ||
            confirmPwd == null || confirmPwd.isEmpty()) {
            showPwdMessage("Please fill in all password fields.", true);
            return;
        }

        if (newPwd.length() < 6) {
            showPwdMessage("New password must be at least 6 characters.", true);
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            showPwdMessage("New password and confirm password do not match.", true);
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                authClient.changePassword(currentPwd, newPwd, confirmPwd);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showPwdMessage("Password updated successfully!", false);
            currentPwdField.clear();
            newPwdField.clear();
            confirmNewPwdField.clear();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showPwdMessage(ex != null ? ex.getMessage() : "Failed to update password.", true);
        });

        new Thread(task).start();
    }

    private void showPwdMessage(String msg, boolean isError) {
        pwdMessageLabel.setText(msg);
        pwdMessageLabel.setStyle(isError ?
                "-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171;" :
                "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399;");
        pwdMessageLabel.setVisible(true);
        pwdMessageLabel.setManaged(true);
    }
}
