package com.smartspend.client.controller;

import com.smartspend.client.model.BudgetItem;
import com.smartspend.client.service.BudgetServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.CurrencyHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class BudgetsController implements Initializable {

    @FXML private ComboBox<String> monthBox;
    @FXML private ComboBox<Integer> yearBox;

    @FXML private Label totalBudgetedLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label totalRemainingLabel;

    @FXML private VBox budgetCardsList;

    private final BudgetServiceClient budgetClient = new BudgetServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateFilters();
        loadBudgets();
    }

    private void setupDateFilters() {
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthBox.getItems().add(m + " - " + monthName);
        }

        LocalDate now = LocalDate.now();
        monthBox.setValue(now.getMonthValue() + " - " + now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        int currentYear = now.getYear();
        for (int y = currentYear - 2; y <= currentYear + 2; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);
    }

    @FXML
    public void handleFilter() {
        loadBudgets();
    }

    public void loadBudgets() {
        int month = getSelectedMonth();
        int year = yearBox.getValue() != null ? yearBox.getValue() : LocalDate.now().getYear();

        Task<List<BudgetItem>> task = new Task<>() {
            @Override
            protected List<BudgetItem> call() throws Exception {
                return budgetClient.getBudgets(month, year);
            }
        };

        task.setOnSucceeded(e -> {
            List<BudgetItem> budgets = task.getValue();
            renderBudgets(budgets);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Budget Error", "Could not load budgets: " + (ex != null ? ex.getMessage() : ""));
        });

        new Thread(task).start();
    }

    private void renderBudgets(List<BudgetItem> budgets) {
        budgetCardsList.getChildren().clear();

        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        if (budgets == null || budgets.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 40;");
            Label emptyLabel = new Label("No budget targets defined for this month.");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: 600;");
            Button addBtn = new Button("➕ Set Your First Budget Target");
            addBtn.getStyleClass().add("btn-primary");
            addBtn.setOnAction(ev -> handleNewBudget());
            emptyBox.getChildren().addAll(emptyLabel, addBtn);
            budgetCardsList.getChildren().add(emptyBox);

            totalBudgetedLabel.setText("₹0.00");
            totalSpentLabel.setText("₹0.00");
            totalRemainingLabel.setText("₹0.00");
            return;
        }

        for (BudgetItem b : budgets) {
            totalBudgeted = totalBudgeted.add(b.getMonthlyLimit() != null ? b.getMonthlyLimit() : BigDecimal.ZERO);
            totalSpent = totalSpent.add(b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO);

            VBox card = createBudgetCard(b);
            budgetCardsList.getChildren().add(card);
        }

        BigDecimal remaining = totalBudgeted.subtract(totalSpent);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        totalBudgetedLabel.setText(CurrencyHelper.format(totalBudgeted));
        totalSpentLabel.setText(CurrencyHelper.format(totalSpent));
        totalRemainingLabel.setText(CurrencyHelper.format(remaining));
    }

    private VBox createBudgetCard(BudgetItem b) {
        VBox card = new VBox(12);
        card.getStyleClass().add("kpi-card");
        card.setPadding(new Insets(18, 24, 18, 24));

        // Row 1: Category Name, Status Badge, and Delete Button
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label catLabel = new Label(b.getCategoryName() != null ? b.getCategoryName() : "Category");
        catLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");

        Label statusBadge = new Label();
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        double progress = b.getPercentageUsed() / 100.0;
        progressBar.setProgress(Math.min(progress, 1.0));

        if ("EXCEEDED".equalsIgnoreCase(b.getStatus())) {
            statusBadge.setText("⚠️ EXCEEDED (" + b.getPercentageUsed() + "%)");
            statusBadge.getStyleClass().add("badge-expense");
            progressBar.getStyleClass().add("progress-bar-danger");
        } else if ("WARNING".equalsIgnoreCase(b.getStatus())) {
            statusBadge.setText("⚡ WARNING (" + b.getPercentageUsed() + "%)");
            statusBadge.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
            progressBar.getStyleClass().add("progress-bar-warning");
        } else {
            statusBadge.setText("✓ NORMAL (" + b.getPercentageUsed() + "%)");
            statusBadge.getStyleClass().add("badge-income");
            progressBar.getStyleClass().add("progress-bar-normal");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> handleDeleteBudget(b));

        topRow.getChildren().addAll(catLabel, statusBadge, spacer, deleteBtn);

        // Row 2: Metrics Info
        HBox metricsRow = new HBox(24);
        metricsRow.setAlignment(Pos.CENTER_LEFT);

        Label spentLabel = new Label("Spent: " + b.getFormattedSpent() + " of " + b.getFormattedLimit());
        spentLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-font-weight: 600;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label remLabel = new Label("Remaining: " + b.getFormattedRemaining());
        remLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 13px; -fx-font-weight: bold;");

        metricsRow.getChildren().addAll(spentLabel, spacer2, remLabel);

        card.getChildren().addAll(topRow, progressBar, metricsRow);
        return card;
    }

    private void handleDeleteBudget(BudgetItem b) {
        boolean confirm = AlertHelper.showConfirmation(
                "Delete Budget Target",
                "Are you sure you want to delete the monthly budget for '" + b.getCategoryName() + "'?"
        );

        if (confirm) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    budgetClient.deleteBudget(b.getId());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                AlertHelper.showInfo("Success", "Budget target deleted.");
                loadBudgets();
            });

            task.setOnFailed(e -> {
                AlertHelper.showError("Delete Failed", "Could not delete budget: " + task.getException().getMessage());
            });

            new Thread(task).start();
        }
    }

    @FXML
    public void handleNewBudget() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/budget_dialog.fxml"));
            Parent root = loader.load();

            BudgetDialogController controller = loader.getController();
            controller.setPresetDate(getSelectedMonth(), yearBox.getValue());

            Stage stage = new Stage();
            stage.setTitle("Set Monthly Budget Target");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadBudgets();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Dialog Error", "Could not open budget dialog: " + e.getMessage());
        }
    }

    private int getSelectedMonth() {
        String val = monthBox.getValue();
        if (val != null && val.contains(" - ")) {
            return Integer.parseInt(val.split(" - ")[0]);
        }
        return LocalDate.now().getMonthValue();
    }
}
