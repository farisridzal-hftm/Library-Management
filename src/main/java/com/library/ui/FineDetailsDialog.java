package com.library.ui;

import com.library.model.Fine;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FineDetailsDialog extends Dialog<Void> {
    private DatabaseManager dbManager;
    private Fine fine;
    
    public FineDetailsDialog(DatabaseManager dbManager, Fine fine) {
        this.dbManager = dbManager;
        this.fine = fine;
        
        setTitle("Fine Details - #" + fine.getId());
        setHeaderText("Detailed information for fine #" + fine.getId());
        setResizable(true);
        
        setupUI();
        
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Add action buttons if fine is outstanding
        if (fine.isOutstanding()) {
            ButtonType payButtonType = new ButtonType("Mark as Paid", ButtonBar.ButtonData.OTHER);
            ButtonType waiveButtonType = new ButtonType("Waive Fine", ButtonBar.ButtonData.OTHER);
            getDialogPane().getButtonTypes().addAll(payButtonType, waiveButtonType);
            
            Button payButton = (Button) getDialogPane().lookupButton(payButtonType);
            Button waiveButton = (Button) getDialogPane().lookupButton(waiveButtonType);
            
            payButton.setOnAction(e -> {
                if (markAsPaid()) {
                    setupUI(); // Refresh dialog
                }
            });
            
            waiveButton.setOnAction(e -> {
                if (waiveFine()) {
                    setupUI(); // Refresh dialog
                }
            });
        }
    }
    
    private void setupUI() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Fine Information Section
        VBox fineInfoSection = createFineInfoSection();
        
        // Member Information Section
        VBox memberInfoSection = createMemberInfoSection();
        
        // Loan Information Section (if applicable)
        if (fine.getLoan() != null) {
            VBox loanInfoSection = createLoanInfoSection();
            content.getChildren().add(loanInfoSection);
        }
        
        content.getChildren().addAll(fineInfoSection, memberInfoSection);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        getDialogPane().setContent(scrollPane);
    }
    
    private VBox createFineInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Fine Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        addDetailRow(grid, 0, "Fine ID:", String.valueOf(fine.getId()));
        addDetailRow(grid, 1, "Amount:", fine.getFormattedAmount());
        addDetailRow(grid, 2, "Reason:", fine.getReason());
        addDetailRow(grid, 3, "Status:", fine.getStatus());
        addDetailRow(grid, 4, "Issue Date:", fine.getIssueDate().format(formatter));
        
        if (fine.getPaidDate() != null) {
            addDetailRow(grid, 5, "Paid Date:", fine.getPaidDate().format(formatter));
        } else {
            addDetailRow(grid, 5, "Paid Date:", "Not paid");
        }
        
        if (fine.isOutstanding()) {
            long daysOutstanding = java.time.temporal.ChronoUnit.DAYS.between(
                fine.getIssueDate(), java.time.LocalDate.now());
            addDetailRow(grid, 6, "Days Outstanding:", String.valueOf(daysOutstanding));
        }
        
        if (fine.getDescription() != null && !fine.getDescription().trim().isEmpty()) {
            addDetailRow(grid, 7, "Description:", fine.getDescription());
        }
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private VBox createMemberInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Member Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        var member = fine.getMember();
        addDetailRow(grid, 0, "Name:", member.getFullName());
        addDetailRow(grid, 1, "ID:", String.valueOf(member.getId()));
        addDetailRow(grid, 2, "Email:", member.getEmail());
        addDetailRow(grid, 3, "Phone:", member.getPhone());
        addDetailRow(grid, 4, "Status:", member.getStatus());
        
        // Show total outstanding fines for this member
        double totalFines = dbManager.getMemberFines(member).stream()
            .filter(com.library.model.Fine::isOutstanding)
            .mapToDouble(com.library.model.Fine::getAmount)
            .sum();
        addDetailRow(grid, 5, "Total Outstanding Fines:", String.format("€%.2f", totalFines));
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private VBox createLoanInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Related Loan Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        var loan = fine.getLoan();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        addDetailRow(grid, 0, "Loan ID:", String.valueOf(loan.getId()));
        addDetailRow(grid, 1, "Media:", loan.getMediaTitle());
        addDetailRow(grid, 2, "Loan Date:", loan.getLoanDate().format(formatter));
        addDetailRow(grid, 3, "Due Date:", loan.getDueDate().format(formatter));
        
        if (loan.getReturnDate() != null) {
            addDetailRow(grid, 4, "Return Date:", loan.getReturnDate().format(formatter));
        } else {
            addDetailRow(grid, 4, "Return Date:", "Not returned");
        }
        
        addDetailRow(grid, 5, "Status:", loan.getStatus());
        
        if (loan.isOverdue()) {
            addDetailRow(grid, 6, "Days Overdue:", String.valueOf(loan.getDaysOverdue()));
        }
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelControl = new Label(label);
        labelControl.getStyleClass().add("detail-label");
        
        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("detail-value");
        
        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }
    
    private boolean markAsPaid() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark Fine as Paid");
        confirmAlert.setHeaderText("Mark this fine as paid?");
        confirmAlert.setContentText(String.format(
            "Fine: %s\nAmount: %s\n\nMark as paid?",
            fine.getReason(), fine.getFormattedAmount()));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fine.markAsPaid();
            dbManager.updateFine(fine);
            showAlert("Success", "Fine marked as paid successfully!");
            return true;
        }
        
        return false;
    }
    
    private boolean waiveFine() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Waive Fine");
        confirmAlert.setHeaderText("Waive this fine?");
        confirmAlert.setContentText(String.format(
            "Fine: %s\nAmount: %s\n\nThis action cannot be undone. Waive this fine?",
            fine.getReason(), fine.getFormattedAmount()));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fine.waive();
            dbManager.updateFine(fine);
            showAlert("Success", "Fine waived successfully!");
            return true;
        }
        
        return false;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}