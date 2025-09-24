package com.library.ui;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MemberLoansDialog extends Dialog<Void> {
    private DatabaseManager dbManager;
    private Member member;
    private TableView<Loan> loansTable;
    
    public MemberLoansDialog(DatabaseManager dbManager, Member member) {
        this.dbManager = dbManager;
        this.member = member;
        
        setTitle("Member Loans - " + member.getFullName());
        setHeaderText("Loan History for " + member.getFullName());
        setResizable(true);
        
        setupUI();
        loadLoans();
        
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }
    
    private void setupUI() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(800);
        content.setPrefHeight(500);
        
        // Member Info
        VBox memberInfo = createMemberInfoSection();
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        // Loans Table
        loansTable = createLoansTable();
        VBox.setVgrow(loansTable, Priority.ALWAYS);
        
        content.getChildren().addAll(memberInfo, actionButtons, loansTable);
        getDialogPane().setContent(content);
    }
    
    private VBox createMemberInfoSection() {
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("info-section");
        infoBox.setPadding(new Insets(10));
        
        Label title = new Label("Member Information");
        title.getStyleClass().add("section-title");
        
        Label nameLabel = new Label("Name: " + member.getFullName());
        Label emailLabel = new Label("Email: " + member.getEmail());
        Label statusLabel = new Label("Status: " + member.getStatus());
        Label loansLabel = new Label("Current Loans: " + member.getCurrentLoans() + "/" + member.getMaxLoans());
        
        infoBox.getChildren().addAll(title, nameLabel, emailLabel, statusLabel, loansLabel);
        return infoBox;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button renewBtn = new Button("🔄 Renew Loan");
        renewBtn.getStyleClass().add("action-button");
        renewBtn.setOnAction(e -> renewLoan());
        renewBtn.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button returnBtn = new Button("📥 Return Item");
        returnBtn.getStyleClass().add("action-button");
        returnBtn.setOnAction(e -> returnLoan());
        returnBtn.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button viewDetailsBtn = new Button("👁️ View Details");
        viewDetailsBtn.getStyleClass().add("action-button");
        viewDetailsBtn.setOnAction(e -> viewLoanDetails());
        viewDetailsBtn.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadLoans());
        
        buttonBox.getChildren().addAll(renewBtn, returnBtn, viewDetailsBtn, refreshBtn);
        return buttonBox;
    }
    
    private TableView<Loan> createLoansTable() {
        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // ID Column
        TableColumn<Loan, Integer> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        // Media Title Column
        TableColumn<Loan, String> titleCol = new TableColumn<>("Media Title");
        titleCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getMediaTitle();
                }
            });
        titleCol.setPrefWidth(200);
        
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
        statusCol.setPrefWidth(100);
        
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
        
        table.getColumns().addAll(idCol, titleCol, loanDateCol, dueDateCol, returnDateCol, statusCol, renewalsCol, overdueCol);
        
        // Row styling for overdue loans
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
                    viewLoanDetails();
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    private void loadLoans() {
        List<Loan> memberLoans = dbManager.getMemberLoans(member);
        loansTable.getItems().clear();
        loansTable.getItems().addAll(memberLoans);
    }
    
    private void renewLoan() {
        Loan selectedLoan = loansTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            if (!"Active".equals(selectedLoan.getStatus())) {
                showAlert("Cannot Renew", "Only active loans can be renewed.");
                return;
            }
            
            if (selectedLoan.canRenew()) {
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
            } else {
                String reason = selectedLoan.isOverdue() ? "overdue" : "maximum renewals reached";
                showAlert("Cannot Renew", "This loan cannot be renewed because it is " + reason + ".");
            }
        }
    }
    
    private void returnLoan() {
        Loan selectedLoan = loansTable.getSelectionModel().getSelectedItem();
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
    
    private void viewLoanDetails() {
        Loan selectedLoan = loansTable.getSelectionModel().getSelectedItem();
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