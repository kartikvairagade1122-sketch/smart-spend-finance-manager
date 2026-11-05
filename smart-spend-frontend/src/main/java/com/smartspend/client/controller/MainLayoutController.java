package com.smartspend.client.controller;

import com.smartspend.client.service.AuthServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.SceneNavigator;
import com.smartspend.client.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private ImageView topLogo;
    @FXML private Label userAvatarLetter;
    @FXML private Label userDisplayName;
    @FXML private HBox profilePill;

    @FXML private Button navDashboard;
    @FXML private Button navTransactions;
    @FXML private Button navIncomes;
    @FXML private Button navExpenses;
    @FXML private Button navBudgets;
    @FXML private Button navPortfolio;
    @FXML private Button navReports;
    @FXML private Button navProfile;

    @FXML private StackPane contentArea;

    private Button currentActiveNav;
    private final AuthServiceClient authServiceClient = new AuthServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load Logo Image
        try {
            InputStream is = getClass().getResourceAsStream("/images/smartspend-logo.png");
            if (is != null) {
                topLogo.setImage(new Image(is));
            }
        } catch (Exception ignored) {}

        // Set User Information
        String fullName = SessionManager.getInstance().getFullName();
        String username = SessionManager.getInstance().getUsername();
        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : username;

        userDisplayName.setText(displayName != null ? displayName : "User");
        if (displayName != null && !displayName.isEmpty()) {
            userAvatarLetter.setText(displayName.substring(0, 1).toUpperCase());
        }

        // Show Default View (Dashboard)
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        setActiveNav(navDashboard);
        loadViewIntoContentArea("/fxml/dashboard.fxml");
    }

    @FXML
    public void showTransactions() {
        setActiveNav(navTransactions);
        loadTransactionsView(null);
    }

    @FXML
    public void showIncomes() {
        setActiveNav(navIncomes);
        loadTransactionsView("INCOME");
    }

    @FXML
    public void showExpenses() {
        setActiveNav(navExpenses);
        loadTransactionsView("EXPENSE");
    }

    @FXML
    public void showBudgets() {
        setActiveNav(navBudgets);
        loadViewIntoContentArea("/fxml/budgets.fxml");
    }

    @FXML
    public void showPortfolio() {
        setActiveNav(navPortfolio);
        loadViewIntoContentArea("/fxml/portfolio.fxml");
    }

    @FXML
    public void showReports() {
        setActiveNav(navReports);
        loadViewIntoContentArea("/fxml/reports.fxml");
    }

    @FXML
    public void showProfile() {
        setActiveNav(navProfile);
        loadViewIntoContentArea("/fxml/profile.fxml");
    }

    @FXML
    public void handleProfileClick() {
        showProfile();
    }

    @FXML
    public void handleLogout() {
        boolean confirmed = AlertHelper.showConfirmation("Sign Out", "Are you sure you want to sign out of Smart Spend?");
        if (confirmed) {
            authServiceClient.logout();
            SceneNavigator.loadScene("/fxml/login.fxml", "Smart Spend - Login");
        }
    }

    private void setActiveNav(Button btn) {
        if (currentActiveNav != null) {
            currentActiveNav.getStyleClass().remove("nav-button-active");
        }
        currentActiveNav = btn;
        if (currentActiveNav != null && !currentActiveNav.getStyleClass().contains("nav-button-active")) {
            currentActiveNav.getStyleClass().add("nav-button-active");
        }
    }

    private void loadViewIntoContentArea(String fxmlPath) {
        try {
            Parent view = SceneNavigator.loadView(fxmlPath);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("View Error", "Could not load view: " + e.getMessage());
        }
    }

    private void loadTransactionsView(String presetType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transactions.fxml"));
            Parent view = loader.load();
            TransactionsController controller = loader.getController();
            if (presetType != null) {
                controller.setPresetTypeFilter(presetType);
            }
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("View Error", "Could not load transactions: " + e.getMessage());
        }
    }
}
