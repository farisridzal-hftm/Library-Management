package com.library.ui;

import com.library.model.Loan;
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

public class LoanManagementView extends VBox {
    private DatabaseManager dbManager;
    private TableView<Loan> loanTable;
    private FilteredList<Loan> filteredLoans;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> overdueFilter;
    
    public LoanManagementView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
        loadLoans();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("management-view");
        
        // Title
        Label title = new Label("Loan Management");
        title.getStyleClass().add("page-title");
        
        // Search and Filter Bar
        HBox searchBar = createSearchBar();
        
        // Loan Table
        loanTable = createLoanTable();
        VBox.setVgrow(loanTable, Priority.ALWAYS);
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        // Statistics Bar
        HBox statsBar = createStatsBar();
        
        getChildren().addAll(title, searchBar, actionButtons, statsBar, loanTable);
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.getStyleClass().add("search-bar");
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search by member name, media title, or loan ID...");
        searchField.setPrefWidth(300);
        
        Label statusLabel = new Label("Status:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Active", "Returned", "Overdue");
        statusFilter.setValue("All");
        
        Label overdueLabel = new Label("Overdue:");
        overdueFilter = new ComboBox<>();
        overdueFilter.getItems().addAll("All", "Overdue Only", "Not Overdue");
        overdueFilter.setValue("All");
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("All");
            overdueFilter.setValue("All");
        });
        
        searchBar.getChildren().addAll(searchLabel, searchField, statusLabel, statusFilter, 
                                     overdueLabel, overdueFilter, clearBtn);
        return searchBar;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button addBtn = new Button("➕ Create Loan");
        addBtn.getStyleClass().add("action-button");
        addBtn.setOnAction(e -> showCreateLoanDialog());
        
        Button editBtn = new Button("✏️ Edit Loan");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> showEditLoanDialog());
        editBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button returnBtn = new Button("📥 Return Item");
        returnBtn.getStyleClass().add("action-button");
        returnBtn.setOnAction(e -> returnLoan());
        returnBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button renewBtn = new Button("🔄 Renew Loan");
        renewBtn.getStyleClass().add("action-button");
        renewBtn.setOnAction(e -> renewLoan());
        renewBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button detailsBtn = new Button("👁️ View Details");
        detailsBtn.getStyleClass().add("action-button");
        detailsBtn.setOnAction(e -> showLoanDetails());
        detailsBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadLoans());
        
        buttonBox.getChildren().addAll(addBtn, editBtn, returnBtn, renewBtn, detailsBtn, refreshBtn);
        return buttonBox;
    }
    
    private HBox createStatsBar() {
        HBox statsBar = new HBox(20);
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setPadding(new Insets(10));
        
        Label activeLoansLabel = new Label("Active Loans: " + dbManager.getTotalActiveLoans());
        activeLoansLabel.getStyleClass().add("stats-label");
        
        Label overdueLoansLabel = new Label("Overdue Loans: " + dbManager.getTotalOverdueLoans());
        overdueLoansLabel.getStyleClass().add("stats-label");
        
        Label totalLoansLabel = new Label("Total Loans: " + dbManager.getAllLoans().size());
        totalLoansLabel.getStyleClass().add("stats-label");
        
        statsBar.getChildren().addAll(activeLoansLabel, overdueLoansLabel, totalLoansLabel);
        return statsBar;
    }
    
    private TableView<Loan> createLoanTable() {
        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // ID Column
        TableColumn<Loan, Integer> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        // Member Column
        TableColumn<Loan, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getMemberName();
                }
            });
        memberCol.setPrefWidth(150);
        
        // Media Column
        TableColumn<Loan, String> mediaCol = new TableColumn<>("Media");
        mediaCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getMediaTitle();
                }
            });
        mediaCol.setPrefWidth(200);
        
        // Loan Date Column
        TableColumn<Loan, LocalDate> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        loanDateCol.setPrefWidth(100);
        
        // Due Date Column
        TableColumn<Loan, LocalDate> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setPrefWidth(100);
        
        // Return Date Column
        TableColumn<Loan, LocalDate> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnDateCol.setPrefWidth(100);
        
        // Status Column
        TableColumn<Loan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        
        // Renewals Column
        TableColumn<Loan, Integer> renewalsCol = new TableColumn<>("Renewals");
        renewalsCol.setCellValueFactory(new PropertyValueFactory<>("renewalCount"));
        renewalsCol.setPrefWidth(80);
        
        // Days Overdue Column
        TableColumn<Loan, String> overdueCol = new TableColumn<>("Days Overdue");
        overdueCol.setCellValueFactory(cellData -> {
            Loan loan = cellData.getValue();
            if (loan.isOverdue()) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(loan.getDaysOverdue()));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        overdueCol.setPrefWidth(100);
        
        // Fine Amount Column
        TableColumn<Loan, String> fineCol = new TableColumn<>("Fine Amount");
        fineCol.setCellValueFactory(cellData -> {
            Loan loan = cellData.getValue();
            double fine = loan.calculateFine();
            if (fine > 0) {
                return new javafx.beans.property.SimpleStringProperty(String.format("€%.2f", fine));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        fineCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, memberCol, mediaCol, loanDateCol, dueDateCol, 
                                returnDateCol, statusCol, renewalsCol, overdueCol, fineCol);
        
        // Row styling
        table.setRowFactory(tv -> {
            TableRow<Loan> row = new TableRow<Loan>() {
                @Override
                protected void updateItem(Loan loan, boolean empty) {
                    super.updateItem(loan, empty);
                    if (empty || loan == null) {
                        setStyle("");
                    } else if (loan.isOverdue()) {
                        setStyle("-fx-background-color: #ffebee;");
                    } else if ("Returned".equals(loan.getStatus())) {
                        setStyle("-fx-background-color: #e8f5e8;");
                    } else {
                        setStyle("");
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showLoanDetails();
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    private void loadLoans() {
        ObservableList<Loan> loans = dbManager.getAllLoans();
        filteredLoans = new FilteredList<>(loans);
        
        // Setup filters
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        overdueFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        SortedList<Loan> sortedLoans = new SortedList<>(filteredLoans);
        sortedLoans.comparatorProperty().bind(loanTable.comparatorProperty());
        
        loanTable.setItems(sortedLoans);
    }
    
    private void updateFilters() {
        filteredLoans.setPredicate(loan -> {
            // Search filter
            String searchText = searchField.getText();
            boolean matchesSearch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = loan.getMemberName().toLowerCase().contains(lowerCaseFilter) ||
                              loan.getMediaTitle().toLowerCase().contains(lowerCaseFilter) ||
                              String.valueOf(loan.getId()).contains(lowerCaseFilter);
            }
            
            // Status filter
            String statusFilterValue = statusFilter.getValue();
            boolean matchesStatus = true;
            if (statusFilterValue != null && !"All".equals(statusFilterValue)) {
                if ("Overdue".equals(statusFilterValue)) {
                    matchesStatus = loan.isOverdue();
                } else {
                    matchesStatus = statusFilterValue.equals(loan.getStatus());
                }
            }
            
            // Overdue filter
            String overdueFilterValue = overdueFilter.getValue();
            boolean matchesOverdue = true;
            if (overdueFilterValue != null && !"All".equals(overdueFilterValue)) {
                if ("Overdue Only".equals(overdueFilterValue)) {
                    matchesOverdue = loan.isOverdue();
                } else if ("Not Overdue".equals(overdueFilterValue)) {
                    matchesOverdue = !loan.isOverdue();
                }
            }
            
            return matchesSearch && matchesStatus && matchesOverdue;
        });
    }
    
    private void showCreateLoanDialog() {
        LoanDialog dialog = new LoanDialog(dbManager);
        Optional<Loan> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadLoans();
            showAlert("Success", "Loan created successfully!");
        }
    }
    
    private void showEditLoanDialog() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            LoanDialog dialog = new LoanDialog(dbManager, selectedLoan);
            Optional<Loan> result = dialog.showAndWait();
            if (result.isPresent()) {
                loadLoans();
                showAlert("Success", "Loan updated successfully!");
            }
        }
    }
    
    private void returnLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            if (!"Active".equals(selectedLoan.getStatus()) && !selectedLoan.isOverdue()) {
                showAlert("Cannot Return", "Only active loans can be returned.");
                return;
            }
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Return Item");
            confirmAlert.setHeaderText("Return " + selectedLoan.getMediaTitle() + "?");
            
            String contentText = "Mark this item as returned?";
            if (selectedLoan.isOverdue()) {
                double fine = selectedLoan.calculateFine();
                contentText += String.format("\n\nNote: This item is %d days overdue. A fine of €%.2f will be applied.", 
                    selectedLoan.getDaysOverdue(), fine);
            }
            confirmAlert.setContentText(contentText);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Generate fine if overdue
                if (selectedLoan.isOverdue()) {
                    double fineAmount = selectedLoan.calculateFine();
                    com.library.model.Fine fine = new com.library.model.Fine(
                        0, selectedLoan.getMember(), selectedLoan, fineAmount, "Overdue return");
                    dbManager.addFine(fine);
                }
                
                dbManager.returnLoan(selectedLoan);
                loadLoans();
                showAlert("Success", "Item returned successfully!");
            }
        }
    }
    
    private void renewLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            if (!selectedLoan.canRenew()) {
                String reason = selectedLoan.isOverdue() ? "overdue" : "maximum renewals reached";
                showAlert("Cannot Renew", "This loan cannot be renewed because it is " + reason + ".");
                return;
            }
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Renew Loan");
            confirmAlert.setHeaderText("Renew loan for " + selectedLoan.getMediaTitle() + "?");
            confirmAlert.setContentText("This will extend the due date by " + 
                selectedLoan.getMedia().getLoanDurationDays() + " days.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedLoan.renew();
                dbManager.updateLoan(selectedLoan);
                loadLoans();
                showAlert("Success", "Loan renewed successfully! New due date: " + selectedLoan.getDueDate());
            }
        }
    }
    
    private void showLoanDetails() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            LoanDetailsDialog detailsDialog = new LoanDetailsDialog(dbManager, selectedLoan);
            detailsDialog.showAndWait();
            loadLoans();
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