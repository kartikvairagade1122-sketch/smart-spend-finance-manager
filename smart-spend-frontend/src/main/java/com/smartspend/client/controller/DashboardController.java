package com.smartspend.client.controller;

import com.smartspend.client.model.DashboardData;
import com.smartspend.client.model.TransactionItem;
import com.smartspend.client.service.DashboardServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.CurrencyHelper;
import com.smartspend.client.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeSubtitle;
    @FXML private Label bannerBalanceLabel;
    @FXML private Label bannerSubtext;
    @FXML private Label bannerSavingsRateLabel;

    @FXML private Label kpiTotalIncome;
    @FXML private Label kpiTotalExpenses;
    @FXML private Label kpiMonthlyIncome;
    @FXML private Label kpiMonthlyExpenses;

    @FXML private LineChart<String, Number> trendChart;
    @FXML private PieChart categoryPieChart;

    @FXML private TableView<TransactionItem> recentTableView;
    @FXML private TableColumn<TransactionItem, String> colDate;
    @FXML private TableColumn<TransactionItem, String> colDesc;
    @FXML private TableColumn<TransactionItem, String> colCat;
    @FXML private TableColumn<TransactionItem, String> colType;
    @FXML private TableColumn<TransactionItem, String> colAmount;

    private final DashboardServiceClient dashboardClient = new DashboardServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String name = SessionManager.getInstance().getFullName();
        welcomeSubtitle.setText("Welcome, " + (name != null ? name : "User") + "! Real-time financial metrics in Indian Rupees (₹).");

        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTransactionDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTransactionDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("INCOME".equalsIgnoreCase(item) ? "badge-income" : "badge-expense");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedAmount()));
        colAmount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+")) {
                        setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    @FXML
    public void loadDashboardData() {
        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                return dashboardClient.getDashboardSummary();
            }
        };

        task.setOnSucceeded(e -> {
            DashboardData data = task.getValue();
            updateUI(data);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Connection Error", "Could not refresh dashboard: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        new Thread(task).start();
    }

    private void updateUI(DashboardData data) {
        if (data == null) return;

        bannerBalanceLabel.setText(CurrencyHelper.format(data.getCurrentBalance()));
        bannerSavingsRateLabel.setText("Monthly Savings: " + CurrencyHelper.format(data.getMonthlySavings()) +
                " (" + data.getSavingsRate() + "%)");

        kpiTotalIncome.setText(CurrencyHelper.format(data.getTotalIncome()));
        kpiTotalExpenses.setText(CurrencyHelper.format(data.getTotalExpenses()));
        kpiMonthlyIncome.setText(CurrencyHelper.format(data.getMonthlyIncome()));
        kpiMonthlyExpenses.setText(CurrencyHelper.format(data.getMonthlyExpenses()));

        // Populate Cash Flow Trends
        trendChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income (Credits)");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense (Debits)");

        for (DashboardData.MonthlyTrendItem item : data.getMonthlyTrends()) {
            incomeSeries.getData().add(new XYChart.Data<>(item.getMonthName(), item.getIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(item.getMonthName(), item.getExpense()));
        }
        trendChart.getData().addAll(incomeSeries, expenseSeries);

        // Populate Pie Chart
        categoryPieChart.getData().clear();
        for (DashboardData.CategoryBreakdownItem item : data.getExpenseCategoryBreakdown()) {
            categoryPieChart.getData().add(new PieChart.Data(
                    item.getCategoryName() + " (" + item.getPercentage() + "%)",
                    item.getTotalAmount().doubleValue()
            ));
        }

        // Populate Recent Transactions Table
        recentTableView.setItems(FXCollections.observableArrayList(data.getRecentTransactions()));
    }

    @FXML
    public void handleQuickAdd() {
        openTransactionDialog(null);
    }

    @FXML
    public void handleViewAll() {
        // Find MainLayoutController to navigate
        Scene scene = recentTableView.getScene();
        if (scene != null) {
            Button btn = (Button) scene.lookup("#navTransactions");
            if (btn != null) {
                btn.fire();
            }
        }
    }

    private void openTransactionDialog(TransactionItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction_dialog.fxml"));
            Parent root = loader.load();

            TransactionDialogController controller = loader.getController();
            if (item != null) {
                controller.setTransaction(item);
            }

            Stage stage = new Stage();
            stage.setTitle(item == null ? "Add Transaction" : "Edit Transaction");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadDashboardData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Dialog Error", "Could not open transaction form: " + e.getMessage());
        }
    }
}
