package com.smartspend.client.controller;

import com.smartspend.client.model.CategoryItem;
import com.smartspend.client.model.TransactionItem;
import com.smartspend.client.service.CategoryServiceClient;
import com.smartspend.client.service.TransactionServiceClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionDialogController implements Initializable {

    @FXML private Label dialogTitle;
    @FXML private Label errorLabel;
    @FXML private RadioButton rbExpense;
    @FXML private RadioButton rbIncome;
    @FXML private TextField amountField;
    @FXML private ComboBox<CategoryItem> categoryBox;
    @FXML private TextField descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;

    private final ToggleGroup typeGroup = new ToggleGroup();
    private final CategoryServiceClient catClient = new CategoryServiceClient();
    private final TransactionServiceClient txClient = new TransactionServiceClient();

    private Long editingTransactionId = null;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rbExpense.setToggleGroup(typeGroup);
        rbIncome.setToggleGroup(typeGroup);

        datePicker.setValue(LocalDate.now());

        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            loadCategoriesForSelectedType();
        });

        loadCategoriesForSelectedType();
    }

    public void setTransaction(TransactionItem item) {
        if (item == null) return;
        editingTransactionId = item.getId();
        dialogTitle.setText("Edit Transaction #" + item.getId());

        if ("INCOME".equalsIgnoreCase(item.getType())) {
            rbIncome.setSelected(true);
        } else {
            rbExpense.setSelected(true);
        }

        if (item.getAmount() != null) {
            amountField.setText(item.getAmount().toPlainString());
        }
        descriptionField.setText(item.getDescription());
        if (item.getTransactionDate() != null) {
            datePicker.setValue(item.getTransactionDate());
        }

        loadCategoriesForSelectedType(item.getCategoryId());
    }

    private void loadCategoriesForSelectedType() {
        loadCategoriesForSelectedType(null);
    }

    private void loadCategoriesForSelectedType(Long preselectedCategoryId) {
        String type = rbIncome.isSelected() ? "INCOME" : "EXPENSE";

        Task<List<CategoryItem>> task = new Task<>() {
            @Override
            protected List<CategoryItem> call() throws Exception {
                return catClient.getCategories(type);
            }
        };

        task.setOnSucceeded(e -> {
            categoryBox.getItems().setAll(task.getValue());
            if (preselectedCategoryId != null) {
                for (CategoryItem cat : categoryBox.getItems()) {
                    if (cat.getId().equals(preselectedCategoryId)) {
                        categoryBox.setValue(cat);
                        break;
                    }
                }
            } else if (!categoryBox.getItems().isEmpty()) {
                categoryBox.setValue(categoryBox.getItems().get(0));
            }
        });

        new Thread(task).start();
    }

    @FXML
    public void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String amountStr = amountField.getText() != null ? amountField.getText().trim() : "";
        String desc = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
        CategoryItem category = categoryBox.getValue();
        LocalDate date = datePicker.getValue();
        String type = rbIncome.isSelected() ? "INCOME" : "EXPENSE";

        if (amountStr.isEmpty() || desc.isEmpty() || date == null) {
            showError("Please fill in all required fields.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid monetary amount.");
            return;
        }

        Long catId = category != null ? category.getId() : null;
        saveButton.setDisable(true);

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (editingTransactionId == null) {
                    txClient.createTransaction(catId, type, amount, desc, date);
                } else {
                    txClient.updateTransaction(editingTransactionId, catId, type, amount, desc, date);
                }
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            saved = true;
            closeStage();
        });

        saveTask.setOnFailed(e -> {
            saveButton.setDisable(false);
            Throwable ex = saveTask.getException();
            showError(ex != null ? ex.getMessage() : "Failed to save transaction.");
        });

        new Thread(saveTask).start();
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

    private void closeStage() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
