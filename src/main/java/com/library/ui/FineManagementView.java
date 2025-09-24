package com.library.ui;

import com.library.model.Fine;
import com.library.service.DatabaseManager;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Optional;

public class FineManagementView extends VBox {
    private DatabaseManager dbManager;
    private TableView<Fine> fineTable;
    private FilteredList<Fine> filteredFines;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label totalOutstandingLabel;
    private Label totalPaidLabel;
    
    public FineManagementView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
        loadFines();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("management-view");
        
        // Title
        Label title = new Label("Fine Management");
        title.getStyleClass().add("page-title");
        
        // Statistics Bar
        HBox statsBar = createStatsBar();
        
        // Search and Filter Bar
        HBox searchBar = createSearchBar();
        
        // Fine Table
        fineTable = createFineTable();
        VBox.setVgrow(fineTable, Priority.ALWAYS);
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        getChildren().addAll(title, statsBar, searchBar, actionButtons, fineTable);
    }
    
    private HBox createStatsBar() {
        HBox statsBar = new HBox(30);
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setPadding(new Insets(15));
        
        totalOutstandingLabel = new Label();
        totalOutstandingLabel.getStyleClass().add("stats-label");
        
        totalPaidLabel = new Label();
        totalPaidLabel.getStyleClass().add("stats-label");
        
        Label totalFinesLabel = new Label("Total Fines: " + dbManager.getAllFines().size());
        totalFinesLabel.getStyleClass().add("stats-label");
        
        statsBar.getChildren().addAll(totalOutstandingLabel, totalPaidLabel, totalFinesLabel);
        return statsBar;
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.getStyleClass().add("search-bar");
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search by member name, fine ID, or reason...");
        searchField.setPrefWidth(300);
        
        Label statusLabel = new Label("Status:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Outstanding", "Paid", "Waived");
        statusFilter.setValue("All");
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("All");
        });
        
        searchBar.getChildren().addAll(searchLabel, searchField, statusLabel, statusFilter, clearBtn);
        return searchBar;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button payBtn = new Button("💰 Mark as Paid");
        payBtn.getStyleClass().add("action-button");
        payBtn.setOnAction(e -> markAsPaid());
        payBtn.disableProperty().bind(fineTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button waiveBtn = new Button("❌ Waive Fine");
        waiveBtn.getStyleClass().add("action-button");
        waiveBtn.setOnAction(e -> waiveFine());
        waiveBtn.disableProperty().bind(fineTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button detailsBtn = new Button("👁️ View Details");
        detailsBtn.getStyleClass().add("action-button");
        detailsBtn.setOnAction(e -> showFineDetails());
        detailsBtn.disableProperty().bind(fineTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button printBtn = new Button("🖨️ Print Receipt");
        printBtn.getStyleClass().add("action-button");
        printBtn.setOnAction(e -> printReceipt());
        printBtn.disableProperty().bind(fineTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadFines());
        
        buttonBox.getChildren().addAll(payBtn, waiveBtn, detailsBtn, printBtn, refreshBtn);
        return buttonBox;
    }
    
    private TableView<Fine> createFineTable() {
        TableView<Fine> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // ID Column
        TableColumn<Fine, Integer> idCol = new TableColumn<>("Fine ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        // Member Column
        TableColumn<Fine, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getMemberName();
                }
            });
        memberCol.setPrefWidth(150);
        
        // Amount Column
        TableColumn<Fine, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getFormattedAmount();
                }
            });
        amountCol.setPrefWidth(100);
        
        // Reason Column
        TableColumn<Fine, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setPrefWidth(150);
        
        // Issue Date Column
        TableColumn<Fine, LocalDate> issueDateCol = new TableColumn<>("Issue Date");
        issueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        issueDateCol.setPrefWidth(100);
        
        // Status Column
        TableColumn<Fine, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, memberCol, amountCol, reasonCol, issueDateCol, statusCol);
        
        table.setRowFactory(tv -> {
            TableRow<Fine> row = new TableRow<Fine>() {
                @Override
                protected void updateItem(Fine fine, boolean empty) {
                    super.updateItem(fine, empty);
                    if (empty || fine == null) {
                        setStyle("");
                    } else if (fine.isOutstanding()) {
                        setStyle("-fx-background-color: #fff3cd;");
                    } else if (fine.isPaid()) {
                        setStyle("-fx-background-color: #d4edda;");
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showFineDetails();
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    private void loadFines() {
        ObservableList<Fine> fines = dbManager.getAllFines();
        filteredFines = new FilteredList<>(fines);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        
        SortedList<Fine> sortedFines = new SortedList<>(filteredFines);
        sortedFines.comparatorProperty().bind(fineTable.comparatorProperty());
        
        fineTable.setItems(sortedFines);
        updateStatistics();
    }
    
    private void updateFilters() {
        filteredFines.setPredicate(fine -> {
            String searchText = searchField.getText();
            boolean matchesSearch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = fine.getMemberName().toLowerCase().contains(lowerCaseFilter) ||
                              fine.getReason().toLowerCase().contains(lowerCaseFilter) ||
                              String.valueOf(fine.getId()).contains(lowerCaseFilter);
            }
            
            String statusFilterValue = statusFilter.getValue();
            boolean matchesStatus = statusFilterValue == null || "All".equals(statusFilterValue) || 
                                  statusFilterValue.equals(fine.getStatus());
            
            return matchesSearch && matchesStatus;
        });
    }
    
    private void updateStatistics() {
        double totalOutstanding = dbManager.getTotalOutstandingFines();
        double totalPaid = dbManager.getAllFines().stream()
            .filter(Fine::isPaid)
            .mapToDouble(Fine::getAmount)
            .sum();
        
        totalOutstandingLabel.setText(String.format("Outstanding: €%.2f", totalOutstanding));
        totalPaidLabel.setText(String.format("Total Paid: €%.2f", totalPaid));
    }
    
    private void markAsPaid() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine != null && selectedFine.isOutstanding()) {
            selectedFine.markAsPaid();
            dbManager.updateFine(selectedFine);
            loadFines();
            showAlert("Success", "Fine marked as paid!");
        }
    }
    
    private void waiveFine() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine != null && selectedFine.isOutstanding()) {
            selectedFine.waive();
            dbManager.updateFine(selectedFine);
            loadFines();
            showAlert("Success", "Fine waived!");
        }
    }
    
    private void showFineDetails() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine != null) {
            FineDetailsDialog dialog = new FineDetailsDialog(dbManager, selectedFine);
            dialog.showAndWait();
            loadFines();
        }
    }
    
    private void printReceipt() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine != null) {
            showAlert("Receipt", "Receipt printed for fine #" + selectedFine.getId());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}