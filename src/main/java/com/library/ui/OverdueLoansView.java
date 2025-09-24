package com.library.ui;

import com.library.model.Loan;
import com.library.service.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class OverdueLoansView extends VBox {
    private DatabaseManager dbManager;
    private TableView<Loan> overdueTable;
    private Label totalOverdueLabel;
    private Label totalFinesLabel;
    
    public OverdueLoansView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
        loadOverdueLoans();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("management-view");
        
        // Title
        Label title = new Label("Overdue Loans Management");
        title.getStyleClass().add("page-title");
        
        // Warning message
        VBox warningBox = createWarningBox();
        
        // Statistics Bar
        HBox statsBar = createStatsBar();
        
        // Overdue Table
        overdueTable = createOverdueTable();
        VBox.setVgrow(overdueTable, Priority.ALWAYS);
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        getChildren().addAll(title, warningBox, statsBar, actionButtons, overdueTable);
    }
    
    private VBox createWarningBox() {
        VBox warningBox = new VBox(5);
        warningBox.getStyleClass().add("warning-box");
        warningBox.setPadding(new Insets(15));
        
        Label warningIcon = new Label("⚠️");
        warningIcon.getStyleClass().add("warning-icon");
        
        Label warningText = new Label("These items are overdue and require immediate attention. " +
            "Contact members and collect fines as necessary.");
        warningText.getStyleClass().add("warning-text");
        warningText.setWrapText(true);
        
        warningBox.getChildren().addAll(warningIcon, warningText);
        return warningBox;
    }
    
    private HBox createStatsBar() {
        HBox statsBar = new HBox(30);
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setPadding(new Insets(15));
        
        totalOverdueLabel = new Label();
        totalOverdueLabel.getStyleClass().add("stats-label");
        
        totalFinesLabel = new Label();
        totalFinesLabel.getStyleClass().add("stats-label");
        
        Label avgDaysOverdueLabel = new Label();
        avgDaysOverdueLabel.getStyleClass().add("stats-label");
        
        statsBar.getChildren().addAll(totalOverdueLabel, totalFinesLabel, avgDaysOverdueLabel);
        return statsBar;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button contactBtn = new Button("📞 Contact Member");
        contactBtn.getStyleClass().add("action-button");
        contactBtn.setOnAction(e -> contactMember());
        contactBtn.disableProperty().bind(overdueTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button returnBtn = new Button("📥 Return Item");
        returnBtn.getStyleClass().add("action-button");
        returnBtn.setOnAction(e -> returnOverdueItem());
        returnBtn.disableProperty().bind(overdueTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button generateFinesBtn = new Button("💰 Generate Fines");
        generateFinesBtn.getStyleClass().add("action-button");
        generateFinesBtn.setOnAction(e -> generateAllFines());
        
        Button suspendMemberBtn = new Button("🚫 Suspend Member");
        suspendMemberBtn.getStyleClass().add("action-button");
        suspendMemberBtn.setOnAction(e -> suspendMember());
        suspendMemberBtn.disableProperty().bind(overdueTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button detailsBtn = new Button("👁️ View Details");
        detailsBtn.getStyleClass().add("action-button");
        detailsBtn.setOnAction(e -> showLoanDetails());
        detailsBtn.disableProperty().bind(overdueTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadOverdueLoans());
        
        buttonBox.getChildren().addAll(contactBtn, returnBtn, generateFinesBtn, 
                                     suspendMemberBtn, detailsBtn, refreshBtn);
        return buttonBox;
    }
    
    private TableView<Loan> createOverdueTable() {
        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // Loan ID Column
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
        
        // Contact Info Column
        TableColumn<Loan, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    var member = cellData.getValue().getMember();
                    return member.getPhone() + " / " + member.getEmail();
                }
            });
        contactCol.setPrefWidth(200);
        
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
        
        // Due Date Column
        TableColumn<Loan, LocalDate> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setPrefWidth(100);
        
        // Days Overdue Column
        TableColumn<Loan, String> overdueCol = new TableColumn<>("Days Overdue");
        overdueCol.setCellValueFactory(cellData -> {
            long days = cellData.getValue().getDaysOverdue();
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(days));
        });
        overdueCol.setPrefWidth(100);
        
        // Fine Amount Column
        TableColumn<Loan, String> fineCol = new TableColumn<>("Fine Amount");
        fineCol.setCellValueFactory(cellData -> {
            double fine = cellData.getValue().calculateFine();
            return new javafx.beans.property.SimpleStringProperty(String.format("€%.2f", fine));
        });
        fineCol.setPrefWidth(100);
        
        // Member Status Column
        TableColumn<Loan, String> statusCol = new TableColumn<>("Member Status");
        statusCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getMember().getStatus();
                }
            });
        statusCol.setPrefWidth(120);
        
        // Severity Column (based on days overdue)
        TableColumn<Loan, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(cellData -> {
            long days = cellData.getValue().getDaysOverdue();
            String severity;
            if (days <= 7) {
                severity = "Low";
            } else if (days <= 30) {
                severity = "Medium";
            } else {
                severity = "High";
            }
            return new javafx.beans.property.SimpleStringProperty(severity);
        });
        severityCol.setPrefWidth(80);
        
        table.getColumns().addAll(idCol, memberCol, contactCol, mediaCol, dueDateCol, 
                                overdueCol, fineCol, statusCol, severityCol);
        
        // Row styling based on severity
        table.setRowFactory(tv -> {
            TableRow<Loan> row = new TableRow<Loan>() {
                @Override
                protected void updateItem(Loan loan, boolean empty) {
                    super.updateItem(loan, empty);
                    if (empty || loan == null) {
                        setStyle("");
                    } else {
                        long days = loan.getDaysOverdue();
                        if (days <= 7) {
                            setStyle("-fx-background-color: #fff3cd;"); // Light yellow
                        } else if (days <= 30) {
                            setStyle("-fx-background-color: #f8d7da;"); // Light red
                        } else {
                            setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #721c24;"); // Dark red
                        }
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
    
    private void loadOverdueLoans() {
        List<Loan> overdueLoans = dbManager.getOverdueLoans();
        overdueTable.setItems(FXCollections.observableList(overdueLoans));
        
        updateStatistics(overdueLoans);
    }
    
    private void updateStatistics(List<Loan> overdueLoans) {
        int totalOverdue = overdueLoans.size();
        double totalFines = overdueLoans.stream()
            .mapToDouble(Loan::calculateFine)
            .sum();
        
        double avgDaysOverdue = overdueLoans.stream()
            .mapToLong(Loan::getDaysOverdue)
            .average()
            .orElse(0.0);
        
        totalOverdueLabel.setText("Total Overdue: " + totalOverdue);
        totalFinesLabel.setText(String.format("Total Fines: €%.2f", totalFines));
        
        // Update the third label with average days
        Label avgLabel = (Label) ((HBox) totalOverdueLabel.getParent()).getChildren().get(2);
        avgLabel.setText(String.format("Avg Days Overdue: %.1f", avgDaysOverdue));
    }
    
    private void contactMember() {
        Loan selectedLoan = overdueTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            ContactMemberDialog dialog = new ContactMemberDialog(dbManager, selectedLoan);
            dialog.showAndWait();
        }
    }
    
    private void returnOverdueItem() {
        Loan selectedLoan = overdueTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Return Overdue Item");
            confirmAlert.setHeaderText("Return " + selectedLoan.getMediaTitle() + "?");
            
            double fine = selectedLoan.calculateFine();
            confirmAlert.setContentText(String.format(
                "This item is %d days overdue.\n\nA fine of €%.2f will be applied.\n\nProceed with return?", 
                selectedLoan.getDaysOverdue(), fine));
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Generate fine
                com.library.model.Fine fineRecord = new com.library.model.Fine(
                    0, selectedLoan.getMember(), selectedLoan, fine, "Overdue return");
                dbManager.addFine(fineRecord);
                
                // Return loan
                dbManager.returnLoan(selectedLoan);
                loadOverdueLoans();
                showAlert("Success", String.format("Item returned successfully!\nFine of €%.2f has been applied.", fine));
            }
        }
    }
    
    private void generateAllFines() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Generate All Fines");
        confirmAlert.setHeaderText("Generate fines for all overdue loans?");
        confirmAlert.setContentText("This will create fine records for all currently overdue items. Continue?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dbManager.generateOverdueFines();
            loadOverdueLoans();
            showAlert("Success", "Overdue fines have been generated successfully!");
        }
    }
    
    private void suspendMember() {
        Loan selectedLoan = overdueTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            var member = selectedLoan.getMember();
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Suspend Member");
            confirmAlert.setHeaderText("Suspend " + member.getFullName() + "?");
            confirmAlert.setContentText("This will prevent the member from borrowing new items. " +
                "The member can be reactivated later. Continue?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                member.setStatus("Suspended");
                dbManager.updateMember(member);
                loadOverdueLoans();
                showAlert("Success", member.getFullName() + " has been suspended.");
            }
        }
    }
    
    private void showLoanDetails() {
        Loan selectedLoan = overdueTable.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            LoanDetailsDialog detailsDialog = new LoanDetailsDialog(dbManager, selectedLoan);
            detailsDialog.showAndWait();
            loadOverdueLoans();
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