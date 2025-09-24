package com.library.ui;

import com.library.model.Loan;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LoanDetailsDialog extends Dialog<Void> {
    private DatabaseManager dbManager;
    private Loan loan;
    
    public LoanDetailsDialog(DatabaseManager dbManager, Loan loan) {
        this.dbManager = dbManager;
        this.loan = loan;
        
        setTitle("Loan Details - #" + loan.getId());
        setHeaderText("Detailed information for loan #" + loan.getId());
        setResizable(true);
        
        setupUI();
        
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        
        // Add action buttons if loan is active
        if ("Active".equals(loan.getStatus()) || loan.isOverdue()) {
            ButtonType renewButtonType = new ButtonType("Renew", ButtonBar.ButtonData.OTHER);
            ButtonType returnButtonType = new ButtonType("Return", ButtonBar.ButtonData.OTHER);
            getDialogPane().getButtonTypes().addAll(renewButtonType, returnButtonType);
            
            // Handle button actions
            Button renewButton = (Button) getDialogPane().lookupButton(renewButtonType);
            Button returnButton = (Button) getDialogPane().lookupButton(returnButtonType);
            
            renewButton.setDisable(!loan.canRenew());
            
            renewButton.setOnAction(e -> {
                if (renewLoan()) {
                    // Refresh the dialog content
                    setupUI();
                }
            });
            
            returnButton.setOnAction(e -> {
                if (returnLoan()) {
                    // Close dialog after successful return
                    setResult(null);
                    close();
                }
            });
        }
    }
    
    private void setupUI() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Loan Information Section
        VBox loanInfoSection = createLoanInfoSection();
        
        // Member Information Section
        VBox memberInfoSection = createMemberInfoSection();
        
        // Media Information Section
        VBox mediaInfoSection = createMediaInfoSection();
        
        // Status and Fine Information
        VBox statusSection = createStatusSection();
        
        content.getChildren().addAll(loanInfoSection, memberInfoSection, mediaInfoSection, statusSection);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        getDialogPane().setContent(scrollPane);
    }
    
    private VBox createLoanInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Loan Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        addDetailRow(grid, 0, "Loan ID:", String.valueOf(loan.getId()));
        addDetailRow(grid, 1, "Loan Date:", loan.getLoanDate().format(formatter));
        addDetailRow(grid, 2, "Due Date:", loan.getDueDate().format(formatter));
        
        if (loan.getReturnDate() != null) {
            addDetailRow(grid, 3, "Return Date:", loan.getReturnDate().format(formatter));
        } else {
            addDetailRow(grid, 3, "Return Date:", "Not returned");
        }
        
        addDetailRow(grid, 4, "Status:", loan.getStatus());
        addDetailRow(grid, 5, "Renewals:", loan.getRenewalCount() + "/" + loan.getMaxRenewals());
        
        if (loan.isOverdue()) {
            addDetailRow(grid, 6, "Days Overdue:", String.valueOf(loan.getDaysOverdue()));
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
        
        var member = loan.getMember();
        addDetailRow(grid, 0, "Name:", member.getFullName());
        addDetailRow(grid, 1, "ID:", String.valueOf(member.getId()));
        addDetailRow(grid, 2, "Email:", member.getEmail());
        addDetailRow(grid, 3, "Phone:", member.getPhone());
        addDetailRow(grid, 4, "Status:", member.getStatus());
        addDetailRow(grid, 5, "Current Loans:", member.getCurrentLoans() + "/" + member.getMaxLoans());
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private VBox createMediaInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Media Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        var media = loan.getMedia();
        addDetailRow(grid, 0, "Title:", media.getTitle());
        addDetailRow(grid, 1, "ID:", String.valueOf(media.getId()));
        addDetailRow(grid, 2, "Author:", media.getAuthorName());
        addDetailRow(grid, 3, "Type:", media.getType());
        addDetailRow(grid, 4, "ISBN:", media.getIsbn());
        addDetailRow(grid, 5, "Publisher:", media.getPublisher());
        addDetailRow(grid, 6, "Year:", String.valueOf(media.getPublishYear()));
        addDetailRow(grid, 7, "Location:", media.getLocation());
        addDetailRow(grid, 8, "Available Copies:", media.getAvailableCopies() + "/" + media.getTotalCopies());
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private VBox createStatusSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        
        Label title = new Label("Status & Fees");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        if (loan.isOverdue()) {
            double fine = loan.calculateFine();
            addDetailRow(grid, 0, "Overdue Status:", "OVERDUE (" + loan.getDaysOverdue() + " days)");
            addDetailRow(grid, 1, "Fine Amount:", String.format("€%.2f", fine));
            addDetailRow(grid, 2, "Fine Rate:", "€0.50 per day (max €10.00)");
        } else if ("Returned".equals(loan.getStatus())) {
            addDetailRow(grid, 0, "Status:", "Returned on time");
        } else {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), loan.getDueDate());
            addDetailRow(grid, 0, "Days Remaining:", String.valueOf(daysRemaining));
        }
        
        if (loan.canRenew()) {
            addDetailRow(grid, 3, "Renewal Status:", "Can be renewed");
        } else if ("Active".equals(loan.getStatus())) {
            String reason = loan.isOverdue() ? "Item is overdue" : "Maximum renewals reached";
            addDetailRow(grid, 3, "Renewal Status:", "Cannot renew - " + reason);
        }
        
        // Notes
        if (loan.getNotes() != null && !loan.getNotes().trim().isEmpty()) {
            Label notesTitle = new Label("Notes:");
            notesTitle.getStyleClass().add("detail-label");
            
            TextArea notesArea = new TextArea(loan.getNotes());
            notesArea.setEditable(false);
            notesArea.setPrefRowCount(2);
            notesArea.setWrapText(true);
            
            section.getChildren().addAll(new Separator(), notesTitle, notesArea);
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
    
    private boolean renewLoan() {
        if (!loan.canRenew()) {
            showAlert("Cannot Renew", "This loan cannot be renewed.");
            return false;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Renew Loan");
        confirmAlert.setHeaderText("Renew this loan?");
        confirmAlert.setContentText("This will extend the due date by " + 
            loan.getMedia().getLoanDurationDays() + " days.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loan.renew();
            dbManager.updateLoan(loan);
            showAlert("Success", "Loan renewed successfully! New due date: " + loan.getDueDate());
            return true;
        }
        
        return false;
    }
    
    private boolean returnLoan() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Return Item");
        confirmAlert.setHeaderText("Return " + loan.getMediaTitle() + "?");
        
        String contentText = "Mark this item as returned?";
        if (loan.isOverdue()) {
            double fine = loan.calculateFine();
            contentText += String.format("\n\nNote: This item is %d days overdue. A fine of €%.2f will be applied.", 
                loan.getDaysOverdue(), fine);
        }
        confirmAlert.setContentText(contentText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Generate fine if overdue
            if (loan.isOverdue()) {
                double fineAmount = loan.calculateFine();
                com.library.model.Fine fine = new com.library.model.Fine(
                    0, loan.getMember(), loan, fineAmount, "Overdue return");
                dbManager.addFine(fine);
            }
            
            dbManager.returnLoan(loan);
            showAlert("Success", "Item returned successfully!");
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