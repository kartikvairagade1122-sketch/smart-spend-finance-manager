package com.smartspend.client.controller;

import com.smartspend.client.model.CategoryItem;
import com.smartspend.client.service.BudgetServiceClient;
import com.smartspend.client.service.CategoryServiceClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class BudgetDialogController implements Initializable {

    @FXML private Label errorLabel;
    @FXML private ComboBox<CategoryItem> categoryBox;
    @FXML private TextField limitField;
    @FXML private ComboBox<String> monthBox;
    @FXML private ComboBox<Integer> yearBox;

    private final CategoryServiceClient catClient = new CategoryServiceClient();
    private final BudgetServiceClient budgetClient = new BudgetServiceClient();

    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateDropdowns();
        loadExpenseCategories();
    }

    public void setPresetDate(int month, int year) {
        monthBox.setValue(month + " - " + Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearBox.setValue(year);
    }

    private void setupDateDropdowns() {
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthBox.getItems().add(m + " - " + monthName);
        }

        LocalDate now = LocalDate.now();
        monthBox.setValue(now.getMonthValue() + " - " + now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        int currentYear = now.getYear();
        for (int y = currentYear - 1; y <= currentYear + 3; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);
    }

    private void loadExpenseCategories() {
        Task<List<CategoryItem>> task = new Task<>() {
            @Override
            protected List<CategoryItem> call() throws Exception {
                return catClient.getCategories("EXPENSE");
            }
        };

        task.setOnSucceeded(e -> {
            categoryBox.getItems().setAll(task.getValue());
            if (!categoryBox.getItems().isEmpty()) {
                categoryBox.setValue(categoryBox.getItems().get(0));
            }
        });

        new Thread(task).start();
    }

    @FXML
    public void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        CategoryItem category = categoryBox.getValue();
        String limitStr = limitField.getText() != null ? limitField.getText().trim() : "";
        int month = getSelectedMonth();
        Integer year = yearBox.getValue();

        if (category == null || limitStr.isEmpty() || year == null) {
            showError("Please fill in all required fields.");
            return;
        }

        BigDecimal limit;
        try {
            limit = new BigDecimal(limitStr);
            if (limit.compareTo(BigDecimal.ONE) < 0) {
                showError("Monthly limit must be at least ₹1.00.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric limit amount.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                budgetClient.createBudget(category.getId(), limit, month, year);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            saved = true;
            closeStage();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError(ex != null ? ex.getMessage() : "Failed to set budget target.");
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

    private int getSelectedMonth() {
        String val = monthBox.getValue();
        if (val != null && val.contains(" - ")) {
            return Integer.parseInt(val.split(" - ")[0]);
        }
        return LocalDate.now().getMonthValue();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeStage() {
        Stage stage = (Stage) limitField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
