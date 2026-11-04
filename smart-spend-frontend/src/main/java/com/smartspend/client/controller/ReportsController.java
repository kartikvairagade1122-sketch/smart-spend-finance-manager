package com.smartspend.client.controller;

import com.smartspend.client.model.DashboardData;
import com.smartspend.client.model.ReportData;
import com.smartspend.client.service.ReportServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.CurrencyHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private ToggleButton btnDaily;
    @FXML private ToggleButton btnWeekly;
    @FXML private ToggleButton btnMonthly;
    @FXML private ToggleButton btnYearly;
    @FXML private DatePicker customStartDate;
    @FXML private DatePicker customEndDate;
    @FXML private Label periodLabel;

    @FXML private Label repTotalIncome;
    @FXML private Label repTotalExpenses;
    @FXML private Label repNetSavings;
    @FXML private Label repSavingsRate;
    @FXML private Label repTxCount;

    @FXML private TableView<DashboardData.CategoryBreakdownItem> categoryTable;
    @FXML private TableColumn<DashboardData.CategoryBreakdownItem, String> colCatName;
    @FXML private TableColumn<DashboardData.CategoryBreakdownItem, String> colCatAmount;
    @FXML private TableColumn<DashboardData.CategoryBreakdownItem, String> colCatPercent;
    @FXML private TableColumn<DashboardData.CategoryBreakdownItem, Long> colCatTxCount;

    @FXML private PieChart reportPieChart;

    private final ToggleGroup periodGroup = new ToggleGroup();
    private final ReportServiceClient reportClient = new ReportServiceClient();
    private String selectedPeriod = "MONTHLY";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnDaily.setToggleGroup(periodGroup);
        btnWeekly.setToggleGroup(periodGroup);
        btnMonthly.setToggleGroup(periodGroup);
        btnYearly.setToggleGroup(periodGroup);

        btnMonthly.setSelected(true);

        setupTableColumns();
        loadReport();
    }

    private void setupTableColumns() {
        colCatName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        colCatAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                CurrencyHelper.format(cellData.getValue().getTotalAmount())));

        colCatPercent.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getPercentage() + "%"));

        colCatTxCount.setCellValueFactory(new PropertyValueFactory<>("transactionCount"));
    }

    @FXML
    public void handlePeriodSelect() {
        ToggleButton selected = (ToggleButton) periodGroup.getSelectedToggle();
        if (selected == null) {
            btnMonthly.setSelected(true);
            selected = btnMonthly;
        }

        if (selected == btnDaily) {
            selectedPeriod = "DAILY";
            periodLabel.setText("Report for Today (" + LocalDate.now() + ")");
        } else if (selected == btnWeekly) {
            selectedPeriod = "WEEKLY";
            periodLabel.setText("Report for Past 7 Days");
        } else if (selected == btnYearly) {
            selectedPeriod = "YEARLY";
            periodLabel.setText("Report for Year " + LocalDate.now().getYear());
        } else {
            selectedPeriod = "MONTHLY";
            periodLabel.setText("Report for Current Month (" + LocalDate.now().getMonth() + " " + LocalDate.now().getYear() + ")");
        }

        customStartDate.setValue(null);
        customEndDate.setValue(null);
        loadReport();
    }

    @FXML
    public void handleApplyCustomRange() {
        LocalDate start = customStartDate.getValue();
        LocalDate end = customEndDate.getValue();

        if (start == null || end == null) {
            AlertHelper.showWarning("Dates Required", "Please select both a start date and an end date.");
            return;
        }

        if (start.isAfter(end)) {
            AlertHelper.showWarning("Invalid Range", "Start date cannot be after end date.");
            return;
        }

        selectedPeriod = "CUSTOM";
        periodGroup.selectToggle(null);
        periodLabel.setText("Custom: " + start + " to " + end);
        loadReport();
    }

    public void loadReport() {
        LocalDate start = customStartDate.getValue();
        LocalDate end = customEndDate.getValue();

        Task<ReportData> task = new Task<>() {
            @Override
            protected ReportData call() throws Exception {
                return reportClient.getReport(selectedPeriod, start, end);
            }
        };

        task.setOnSucceeded(e -> {
            ReportData data = task.getValue();
            updateUI(data);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Report Error", "Failed to load report: " + (ex != null ? ex.getMessage() : ""));
        });

        new Thread(task).start();
    }

    private void updateUI(ReportData data) {
        if (data == null) return;

        repTotalIncome.setText(CurrencyHelper.format(data.getTotalIncome()));
        repTotalExpenses.setText(CurrencyHelper.format(data.getTotalExpenses()));
        repNetSavings.setText(CurrencyHelper.format(data.getNetSavings()));
        repSavingsRate.setText(data.getSavingsRate() + "%");
        repTxCount.setText(data.getTotalTransactions() + " transactions recorded");

        // Table
        categoryTable.setItems(FXCollections.observableArrayList(data.getCategoryBreakdown()));

        // Pie Chart
        reportPieChart.getData().clear();
        for (DashboardData.CategoryBreakdownItem item : data.getCategoryBreakdown()) {
            reportPieChart.getData().add(new PieChart.Data(
                    item.getCategoryName() + " (" + item.getPercentage() + "%)",
                    item.getTotalAmount().doubleValue()
            ));
        }
    }

    @FXML
    public void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Financial Report CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Spreadsheet (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("smartspend-report-" + selectedPeriod.toLowerCase() + "-" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(periodLabel.getScene().getWindow());
        if (file == null) return;

        LocalDate start = customStartDate.getValue();
        LocalDate end = customEndDate.getValue();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                byte[] bytes = reportClient.downloadCsv(selectedPeriod, start, end);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            AlertHelper.showInfo("Export Successful", "Report exported successfully to:\n" + file.getAbsolutePath());
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Export Failed", "Could not export CSV: " + (ex != null ? ex.getMessage() : ""));
        });

        new Thread(task).start();
    }
}
