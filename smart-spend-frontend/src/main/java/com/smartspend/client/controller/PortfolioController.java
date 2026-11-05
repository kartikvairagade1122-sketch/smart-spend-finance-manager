package com.smartspend.client.controller;

import com.smartspend.client.model.PortfolioData;
import com.smartspend.client.model.StockHoldingItem;
import com.smartspend.client.service.PortfolioServiceClient;
import com.smartspend.client.util.AlertHelper;
import com.smartspend.client.util.CurrencyHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PortfolioController implements Initializable {

    @FXML private Button refreshBtn;
    @FXML private Label kpiInvested;
    @FXML private Label kpiCurrentValue;
    @FXML private Label kpiTotalPnl;
    @FXML private Label kpiPnlSubtext;
    @FXML private Label kpiHoldingsCount;

    @FXML private Label lastUpdatedLabel;
    @FXML private TableView<StockHoldingItem> stocksTable;
    @FXML private TableColumn<StockHoldingItem, String> colSymbol;
    @FXML private TableColumn<StockHoldingItem, String> colCompany;
    @FXML private TableColumn<StockHoldingItem, Integer> colQuantity;
    @FXML private TableColumn<StockHoldingItem, String> colBuyPrice;
    @FXML private TableColumn<StockHoldingItem, String> colLivePrice;
    @FXML private TableColumn<StockHoldingItem, String> colCurrentVal;
    @FXML private TableColumn<StockHoldingItem, String> colPnl;

    @FXML private PieChart portfolioPieChart;

    private final PortfolioServiceClient portfolioClient = new PortfolioServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPortfolio();
    }

    private void setupTableColumns() {
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        colSymbol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: 800; -fx-text-fill: white;");
                }
            }
        });

        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colBuyPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedBuyPrice()));

        colLivePrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedLivePrice()));
        colLivePrice.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
                }
            }
        });

        colCurrentVal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedCurrentValue()));

        colPnl.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedPnl()));
        colPnl.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    boolean isProfit = item.startsWith("+");
                    badge.setStyle(isProfit ?
                            "-fx-background-color: rgba(16, 185, 129, 0.18); -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;" :
                            "-fx-background-color: rgba(239, 68, 68, 0.18); -fx-text-fill: #f87171; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    public void loadPortfolio() {
        refreshBtn.setDisable(true);
        refreshBtn.setText("Fetching Rates...");

        Task<PortfolioData> task = new Task<>() {
            @Override
            protected PortfolioData call() throws Exception {
                return portfolioClient.getPortfolio();
            }
        };

        task.setOnSucceeded(e -> {
            refreshBtn.setDisable(false);
            refreshBtn.setText("🔄 Refresh Rates");
            PortfolioData data = task.getValue();
            updateUI(data);
        });

        task.setOnFailed(e -> {
            refreshBtn.setDisable(false);
            refreshBtn.setText("🔄 Refresh Rates");
            Throwable ex = task.getException();
            AlertHelper.showError("Market Error", "Could not refresh portfolio: " + (ex != null ? ex.getMessage() : "Network error"));
        });

        new Thread(task).start();
    }

    private void updateUI(PortfolioData data) {
        if (data == null) return;

        kpiInvested.setText(CurrencyHelper.format(data.getTotalInvested()));
        kpiCurrentValue.setText(CurrencyHelper.format(data.getCurrentValue()));

        BigDecimal pnl = data.getTotalPnl();
        BigDecimal pnlPct = data.getTotalPnlPercentage();
        String sign = pnl.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        kpiTotalPnl.setText(sign + CurrencyHelper.format(pnl) + " (" + sign + pnlPct.toPlainString() + "%)");
        kpiTotalPnl.setStyle(pnl.compareTo(BigDecimal.ZERO) >= 0 ?
                "-fx-text-fill: #34d399; -fx-font-weight: 800; -fx-font-size: 22px;" :
                "-fx-text-fill: #f87171; -fx-font-weight: 800; -fx-font-size: 22px;");

        kpiPnlSubtext.setText(data.getProfitableCount() + " in profit / " + data.getLossCount() + " in loss");
        kpiHoldingsCount.setText(data.getHoldingsCount() + " Stocks");

        lastUpdatedLabel.setText("Updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

        // Populate Table
        stocksTable.setItems(FXCollections.observableArrayList(data.getHoldings()));

        // Populate Pie Chart
        portfolioPieChart.getData().clear();
        for (StockHoldingItem item : data.getHoldings()) {
            if (item.getCurrentValue() != null && item.getCurrentValue().compareTo(BigDecimal.ZERO) > 0) {
                portfolioPieChart.getData().add(new PieChart.Data(
                        item.getSymbol() + " (" + CurrencyHelper.format(item.getCurrentValue()) + ")",
                        item.getCurrentValue().doubleValue()
                ));
            }
        }
    }

    @FXML
    public void handleRefresh() {
        loadPortfolio();
    }

    @FXML
    public void handleAddStock() {
        openStockDialog(null);
    }

    @FXML
    public void handleEditSelected() {
        StockHoldingItem selected = stocksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "Please select a stock holding from the table to edit.");
            return;
        }
        openStockDialog(selected);
    }

    @FXML
    public void handleDeleteSelected() {
        StockHoldingItem selected = stocksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "Please select a stock holding from the table to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Confirm Delete Holding",
                "Are you sure you want to remove " + selected.getSymbol() + " (" + selected.getCompanyName() + ") from your portfolio?"
        );

        if (confirm) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    portfolioClient.deleteHolding(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                AlertHelper.showInfo("Success", "Holding removed successfully.");
                loadPortfolio();
            });

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                AlertHelper.showError("Delete Failed", "Could not remove holding: " + (ex != null ? ex.getMessage() : ""));
            });

            new Thread(task).start();
        }
    }

    private void openStockDialog(StockHoldingItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stock_dialog.fxml"));
            Parent root = loader.load();

            StockDialogController controller = loader.getController();
            if (item != null) {
                controller.setHolding(item);
            }

            Stage stage = new Stage();
            stage.setTitle(item == null ? "Add Stock Investment" : "Edit Stock Investment");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 520, 620);
            stage.setScene(scene);
            stage.setMinWidth(480);
            stage.setMinHeight(560);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadPortfolio();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Dialog Error", "Could not open stock investment dialog: " + e.getMessage());
        }
    }
}
