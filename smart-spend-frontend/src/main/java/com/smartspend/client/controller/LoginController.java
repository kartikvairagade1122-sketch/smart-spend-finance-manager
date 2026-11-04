package com.smartspend.client.controller;

import com.smartspend.client.service.AuthServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.SceneNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private ImageView brandLogo;
    @FXML private Button tabLoginBtn;
    @FXML private Button tabRegisterBtn;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private Label messageLabel;

    @FXML private VBox nameBox;
    @FXML private TextField nameField;

    @FXML private VBox emailBox;
    @FXML private TextField emailField;

    @FXML private Label usernameLabel;
    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private VBox confirmPasswordBox;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button submitBtn;
    @FXML private ProgressIndicator progressIndicator;

    private boolean isRegisterMode = false;
    private final AuthServiceClient authServiceClient = new AuthServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            InputStream is = getClass().getResourceAsStream("/images/smartspend-logo.png");
            if (is != null) {
                brandLogo.setImage(new Image(is));
            }
        } catch (Exception ignored) {}
    }

    @FXML
    public void handleShowLogin() {
        isRegisterMode = false;
        tabLoginBtn.getStyleClass().add("nav-button-active");
        tabRegisterBtn.getStyleClass().remove("nav-button-active");

        formTitle.setText("Welcome Back");
        formSubtitle.setText("Enter your credentials to access your financial dashboard");

        nameBox.setVisible(false);
        nameBox.setManaged(false);
        emailBox.setVisible(false);
        emailBox.setManaged(false);
        confirmPasswordBox.setVisible(false);
        confirmPasswordBox.setManaged(false);

        usernameLabel.setText("Username or Email");
        usernameField.setPromptText("Enter your username or email");
        submitBtn.setText("Sign In to Smart Spend");
        hideMessage();
    }

    @FXML
    public void handleShowRegister() {
        isRegisterMode = true;
        tabRegisterBtn.getStyleClass().add("nav-button-active");
        tabLoginBtn.getStyleClass().remove("nav-button-active");

        formTitle.setText("Create Account");
        formSubtitle.setText("Register your private, independent finance management account");

        nameBox.setVisible(true);
        nameBox.setManaged(true);
        emailBox.setVisible(true);
        emailBox.setManaged(true);
        confirmPasswordBox.setVisible(true);
        confirmPasswordBox.setManaged(true);

        usernameLabel.setText("Username");
        usernameField.setPromptText("Choose a unique username");
        submitBtn.setText("Register Account");
        hideMessage();
    }

    @FXML
    public void handleSubmit() {
        hideMessage();

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (isRegisterMode) {
            String fullName = nameField.getText() != null ? nameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showMessage("Please fill in all required fields.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                showMessage("Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                showMessage("Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showMessage("Password and confirm password do not match.");
                return;
            }

            setLoading(true);
            Task<Boolean> registerTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return authServiceClient.register(fullName, email, username, password, confirmPassword);
                }
            };

            registerTask.setOnSucceeded(e -> {
                setLoading(false);
                if (Boolean.TRUE.equals(registerTask.getValue())) {
                    SceneNavigator.loadScene("/fxml/main_layout.fxml", "Smart Spend - Personal Finance Manager");
                }
            });

            registerTask.setOnFailed(e -> {
                setLoading(false);
                Throwable ex = registerTask.getException();
                showMessage(ex != null ? ex.getMessage() : "Registration failed.");
            });

            new Thread(registerTask).start();

        } else {
            // Login Mode
            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please enter your username/email and password.");
                return;
            }

            setLoading(true);
            Task<Boolean> loginTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return authServiceClient.login(username, password);
                }
            };

            loginTask.setOnSucceeded(e -> {
                setLoading(false);
                if (Boolean.TRUE.equals(loginTask.getValue())) {
                    SceneNavigator.loadScene("/fxml/main_layout.fxml", "Smart Spend - Personal Finance Manager");
                }
            });

            loginTask.setOnFailed(e -> {
                setLoading(false);
                Throwable ex = loginTask.getException();
                showMessage(ex != null ? ex.getMessage() : "Invalid credentials.");
            });

            new Thread(loginTask).start();
        }
    }

    private void setLoading(boolean loading) {
        submitBtn.setDisable(loading);
        progressIndicator.setVisible(loading);
        progressIndicator.setManaged(loading);
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
