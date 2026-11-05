package com.smartspend.client.controller;

import com.smartspend.client.model.StockHoldingItem;
import com.smartspend.client.service.PortfolioServiceClient;
import com.smartspend.client.util.CurrencyHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class StockDialogController implements Initializable {

    @FXML private Label dialogTitle;
    @FXML private Label errorLabel;
    @FXML private TextField symbolField;
    @FXML private Button fetchQuoteBtn;

    @FXML private HBox quoteBanner;
    @FXML private Label bannerCompanyName;
    @FXML private Label bannerExchange;
    @FXML private Label bannerLivePrice;
    @FXML private Label bannerDayChange;

    @FXML private TextField companyNameField;
    @FXML private TextField quantityField;
    @FXML private TextField buyPriceField;
    @FXML private DatePicker buyDatePicker;
    @FXML private Label totalInvestedPreview;
    @FXML private Button saveButton;

    private final PortfolioServiceClient portfolioClient = new PortfolioServiceClient();
    private Long editingHoldingId = null;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buyDatePicker.setValue(LocalDate.now());

        // Listeners for live investment calculation preview
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateInvestedPreview());
        buyPriceField.textProperty().addListener((obs, oldVal, newVal) -> updateInvestedPreview());
    }

    public void setHolding(StockHoldingItem item) {
        if (item == null) return;
        editingHoldingId = item.getId();
        dialogTitle.setText("Edit Stock Holding #" + item.getId());

        symbolField.setText(item.getSymbol());
        companyNameField.setText(item.getCompanyName());
        if (item.getQuantity() != null) {
            quantityField.setText(String.valueOf(item.getQuantity()));
        }
        if (item.getBuyPrice() != null) {
            buyPriceField.setText(item.getBuyPrice().toPlainString());
        }
        if (item.getBuyDate() != null) {
            buyDatePicker.setValue(item.getBuyDate());
        }
        updateInvestedPreview();
    }

    @FXML
    public void handleFetchQuote() {
        String symbol = symbolField.getText() != null ? symbolField.getText().trim() : "";
        if (symbol.isEmpty()) {
            showError("Please enter a stock symbol first.");
            return;
        }

        hideError();
        fetchQuoteBtn.setDisable(true);
        fetchQuoteBtn.setText("Fetching...");

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return portfolioClient.getLiveQuote(symbol);
            }
        };

        task.setOnSucceeded(e -> {
            fetchQuoteBtn.setDisable(false);
            fetchQuoteBtn.setText("🔍 Live Price");

            Map<String, Object> quote = task.getValue();
            if (quote != null) {
                Boolean valid = (Boolean) quote.get("valid");
                Object priceObj = quote.get("currentPrice");
                BigDecimal price = priceObj != null ? new BigDecimal(priceObj.toString()) : BigDecimal.ZERO;

                if ((valid != null && !valid) || price.compareTo(BigDecimal.ZERO) <= 0) {
                    quoteBanner.setVisible(false);
                    quoteBanner.setManaged(false);
                    showError("⚠️ Stock symbol '" + symbol + "' not found on NSE/BSE. Please check spelling (e.g. RELIANCE, TCS, INFY, TATAMOTORS, HDFCBANK).");
                    return;
                }

                String compName = (String) quote.get("companyName");
                if (compName != null && !compName.isBlank() && companyNameField.getText().isBlank()) {
                    companyNameField.setText(compName);
                }
                bannerCompanyName.setText(compName != null ? compName : symbol);

                bannerLivePrice.setText(CurrencyHelper.format(price));

                // If buy price is currently empty, prefill with current live price
                if (buyPriceField.getText().isBlank()) {
                    buyPriceField.setText(price.toPlainString());
                }

                Object changePctObj = quote.get("dayChangePercent");
                if (changePctObj != null) {
                    BigDecimal pct = new BigDecimal(changePctObj.toString());
                    String sign = pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                    bannerDayChange.setText(sign + pct.toPlainString() + "%");
                    bannerDayChange.setStyle(pct.compareTo(BigDecimal.ZERO) >= 0 ?
                            "-fx-text-fill: #34d399;" : "-fx-text-fill: #f87171;");
                }

                quoteBanner.setVisible(true);
                quoteBanner.setManaged(true);
            } else {
                quoteBanner.setVisible(false);
                quoteBanner.setManaged(false);
                showError("⚠️ Stock symbol '" + symbol + "' not found. Please verify the NSE/BSE ticker.");
            }
        });

        task.setOnFailed(e -> {
            fetchQuoteBtn.setDisable(false);
            fetchQuoteBtn.setText("🔍 Live Price");
            Throwable ex = task.getException();
            showError("Could not fetch live price: " + (ex != null ? ex.getMessage() : "Network error"));
        });

        new Thread(task).start();
    }

    private void updateInvestedPreview() {
        try {
            String qStr = quantityField.getText() != null ? quantityField.getText().trim() : "";
            String pStr = buyPriceField.getText() != null ? buyPriceField.getText().trim() : "";

            if (!qStr.isEmpty() && !pStr.isEmpty()) {
                int qty = Integer.parseInt(qStr);
                BigDecimal price = new BigDecimal(pStr);
                if (qty > 0 && price.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
                    totalInvestedPreview.setText(CurrencyHelper.format(total));
                    return;
                }
            }
        } catch (Exception ignored) {}
        totalInvestedPreview.setText("₹0.00");
    }

    @FXML
    public void handleSave() {
        hideError();

        String symbol = symbolField.getText() != null ? symbolField.getText().trim() : "";
        String compName = companyNameField.getText() != null ? companyNameField.getText().trim() : "";
        String qtyStr = quantityField.getText() != null ? quantityField.getText().trim() : "";
        String priceStr = buyPriceField.getText() != null ? buyPriceField.getText().trim() : "";
        LocalDate buyDate = buyDatePicker.getValue();

        if (symbol.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || buyDate == null) {
            showError("Please fill in symbol, quantity, buy price and date.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                showError("Quantity must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid whole number for quantity.");
            return;
        }

        BigDecimal buyPrice;
        try {
            buyPrice = new BigDecimal(priceStr);
            if (buyPrice.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Buy price must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid monetary buy price.");
            return;
        }

        if (compName.isEmpty()) {
            compName = symbol.toUpperCase();
        }

        final String finalCompName = compName;
        saveButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (editingHoldingId == null) {
                    portfolioClient.addHolding(symbol, finalCompName, quantity, buyPrice, buyDate);
                } else {
                    portfolioClient.updateHolding(editingHoldingId, symbol, finalCompName, quantity, buyPrice, buyDate);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            saved = true;
            closeStage();
        });

        task.setOnFailed(e -> {
            saveButton.setDisable(false);
            Throwable ex = task.getException();
            showError(ex != null ? ex.getMessage() : "Failed to save stock holding.");
        });

        new Thread(task).start();
    }

    @FXML
    public void handleCancel() {
        closeStage();
    }

    public boolean isSaved() {
        return saved;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void closeStage() {
        Stage stage = (Stage) symbolField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
