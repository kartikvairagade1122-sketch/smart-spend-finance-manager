package com.smartspend.client.controller;

import com.smartspend.client.model.CategoryItem;
import com.smartspend.client.model.TransactionItem;
import com.smartspend.client.service.CategoryServiceClient;
import com.smartspend.client.service.TransactionServiceClient;
import com.smartspend.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable {

    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterBox;
    @FXML private ComboBox<CategoryItem> categoryFilterBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label recordCountLabel;

    @FXML private TableView<TransactionItem> transactionsTable;
    @FXML private TableColumn<TransactionItem, Long> colId;
    @FXML private TableColumn<TransactionItem, String> colDate;
    @FXML private TableColumn<TransactionItem, String> colDesc;
    @FXML private TableColumn<TransactionItem, String> colCategory;
    @FXML private TableColumn<TransactionItem, String> colType;
    @FXML private TableColumn<TransactionItem, String> colAmount;

    private final TransactionServiceClient txClient = new TransactionServiceClient();
    private final CategoryServiceClient catClient = new CategoryServiceClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilterControls();
        loadCategories();
        loadTransactions();
    }

    public void setPresetTypeFilter(String type) {
        if ("INCOME".equalsIgnoreCase(type)) {
            pageTitle.setText("Income Log (Credits)");
            typeFilterBox.setValue("INCOME");
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            pageTitle.setText("Expense Log (Debits)");
            typeFilterBox.setValue("EXPENSE");
        }
        loadTransactions();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTransactionDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTransactionDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

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

    private void setupFilterControls() {
        typeFilterBox.getItems().setAll("All Types", "INCOME", "EXPENSE");
        typeFilterBox.setValue("All Types");
    }

    private void loadCategories() {
        Task<List<CategoryItem>> task = new Task<>() {
            @Override
            protected List<CategoryItem> call() throws Exception {
                return catClient.getCategories(null);
            }
        };

        task.setOnSucceeded(e -> {
            CategoryItem all = new CategoryItem(null, "All Categories", null, "tag", "#64748b", true);
            categoryFilterBox.getItems().clear();
            categoryFilterBox.getItems().add(all);
            categoryFilterBox.getItems().addAll(task.getValue());
            categoryFilterBox.setValue(all);
        });

        new Thread(task).start();
    }

    @FXML
    public void loadTransactions() {
        String typeVal = typeFilterBox.getValue();
        String typeParam = ("All Types".equalsIgnoreCase(typeVal) || typeVal == null) ? null : typeVal;

        CategoryItem catVal = categoryFilterBox.getValue();
        Long categoryId = (catVal != null && catVal.getId() != null) ? catVal.getId() : null;

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String query = searchField.getText() != null ? searchField.getText().trim() : "";

        Task<List<TransactionItem>> task = new Task<>() {
            @Override
            protected List<TransactionItem> call() throws Exception {
                return txClient.getTransactions(typeParam, categoryId, start, end, query);
            }
        };

        task.setOnSucceeded(e -> {
            List<TransactionItem> list = task.getValue();
            transactionsTable.setItems(FXCollections.observableArrayList(list));
            recordCountLabel.setText("Showing " + list.size() + " transactions");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Data Error", "Could not load transactions: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        new Thread(task).start();
    }

    @FXML
    public void handleApplyFilters() {
        loadTransactions();
    }

    @FXML
    public void handleResetFilters() {
        searchField.clear();
        typeFilterBox.setValue("All Types");
        if (!categoryFilterBox.getItems().isEmpty()) {
            categoryFilterBox.setValue(categoryFilterBox.getItems().get(0));
        }
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadTransactions();
    }

    @FXML
    public void handleAddTransaction() {
        openDialog(null);
    }

    @FXML
    public void handleEditSelected() {
        TransactionItem selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "Please select a transaction to edit.");
            return;
        }
        openDialog(selected);
    }

    @FXML
    public void handleDeleteSelected() {
        TransactionItem selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "Please select a transaction to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Confirm Deletion",
                "Are you sure you want to delete transaction #" + selected.getId() + " (" + selected.getDescription() + ")?"
        );

        if (confirm) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    txClient.deleteTransaction(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                AlertHelper.showInfo("Success", "Transaction deleted successfully.");
                loadTransactions();
            });

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                AlertHelper.showError("Delete Failed", "Could not delete transaction: " + (ex != null ? ex.getMessage() : ""));
            });

            new Thread(task).start();
        }
    }

    private void openDialog(TransactionItem item) {
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
                loadTransactions();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Dialog Error", "Could not open dialog: " + e.getMessage());
        }
    }
}
