package com.library.ui;

import com.library.model.Loan;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class ContactMemberDialog extends Dialog<Void> {
    private DatabaseManager dbManager;
    private Loan loan;
    private TextArea messageArea;
    private ComboBox<String> templateCombo;
    
    public ContactMemberDialog(DatabaseManager dbManager, Loan loan) {
        this.dbManager = dbManager;
        this.loan = loan;
        
        setTitle("Contact Member - " + loan.getMemberName());
        setHeaderText("Contact " + loan.getMemberName() + " about overdue item");
        setResizable(true);
        
        setupUI();
        
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Send Notification");
        
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                sendNotification();
            }
            return null;
        });
    }
    
    private void setupUI() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setPrefHeight(500);
        
        // Member and Loan Information
        VBox infoSection = createInfoSection();
        
        // Message Templates
        VBox templateSection = createTemplateSection();
        
        // Message Editor
        VBox messageSection = createMessageSection();
        
        content.getChildren().addAll(infoSection, templateSection, messageSection);
        getDialogPane().setContent(content);
    }
    
    private VBox createInfoSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("info-section");
        section.setPadding(new Insets(15));
        
        Label title = new Label("Member & Loan Information");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        var member = loan.getMember();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        addInfoRow(grid, 0, "Member:", member.getFullName());
        addInfoRow(grid, 1, "Email:", member.getEmail());
        addInfoRow(grid, 2, "Phone:", member.getPhone());
        addInfoRow(grid, 3, "Item:", loan.getMediaTitle());
        addInfoRow(grid, 4, "Due Date:", loan.getDueDate().format(formatter));
        addInfoRow(grid, 5, "Days Overdue:", String.valueOf(loan.getDaysOverdue()));
        addInfoRow(grid, 6, "Fine Amount:", String.format("€%.2f", loan.calculateFine()));
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    private VBox createTemplateSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Message Templates");
        title.getStyleClass().add("section-title");
        
        templateCombo = new ComboBox<>();
        templateCombo.getItems().addAll(
            "Friendly Reminder",
            "Urgent Notice", 
            "Final Warning",
            "Custom Message"
        );
        templateCombo.setPromptText("Select a template");
        templateCombo.setPrefWidth(200);
        
        templateCombo.setOnAction(e -> loadTemplate());
        
        section.getChildren().addAll(title, templateCombo);
        return section;
    }
    
    private VBox createMessageSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Message Content");
        title.getStyleClass().add("section-title");
        
        messageArea = new TextArea();
        messageArea.setPromptText("Enter your message here or select a template above...");
        messageArea.setPrefRowCount(12);
        messageArea.setWrapText(true);
        
        // Character count label
        Label charCountLabel = new Label("0 characters");
        charCountLabel.getStyleClass().add("char-count");
        
        messageArea.textProperty().addListener((obs, oldText, newText) -> {
            charCountLabel.setText(newText.length() + " characters");
        });
        
        section.getChildren().addAll(title, messageArea, charCountLabel);
        return section;
    }
    
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelControl = new Label(label);
        labelControl.getStyleClass().add("info-label");
        
        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("info-value");
        
        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }
    
    private void loadTemplate() {
        String template = templateCombo.getValue();
        if (template == null || "Custom Message".equals(template)) {
            return;
        }
        
        var member = loan.getMember();
        String memberName = member.getFirstName();
        String mediaTitle = loan.getMediaTitle();
        long daysOverdue = loan.getDaysOverdue();
        String dueDate = loan.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        double fineAmount = loan.calculateFine();
        
        String message = "";
        
        switch (template) {
            case "Friendly Reminder":
                message = String.format(
                    "Dear %s,\n\n" +
                    "This is a friendly reminder that the following item is overdue:\n\n" +
                    "Title: %s\n" +
                    "Due Date: %s\n" +
                    "Days Overdue: %d\n\n" +
                    "Please return this item at your earliest convenience. " +
                    "A fine of €%.2f has been applied to your account.\n\n" +
                    "If you have already returned this item, please disregard this message. " +
                    "If you need to renew the loan, please contact us immediately.\n\n" +
                    "Thank you for your cooperation.\n\n" +
                    "Best regards,\n" +
                    "Library Staff",
                    memberName, mediaTitle, dueDate, daysOverdue, fineAmount
                );
                break;
                
            case "Urgent Notice":
                message = String.format(
                    "URGENT: Overdue Item Notice\n\n" +
                    "Dear %s,\n\n" +
                    "Our records show that you have an overdue item that requires immediate attention:\n\n" +
                    "Title: %s\n" +
                    "Due Date: %s\n" +
                    "Days Overdue: %d\n" +
                    "Current Fine: €%.2f\n\n" +
                    "Please return this item IMMEDIATELY to avoid additional penalties. " +
                    "Failure to return overdue items may result in suspension of your library privileges.\n\n" +
                    "Please contact us at your earliest convenience if you are experiencing difficulties " +
                    "returning this item.\n\n" +
                    "Urgent attention required.\n\n" +
                    "Library Administration",
                    memberName, mediaTitle, dueDate, daysOverdue, fineAmount
                );
                break;
                
            case "Final Warning":
                message = String.format(
                    "FINAL WARNING: Immediate Action Required\n\n" +
                    "Dear %s,\n\n" +
                    "This is your FINAL WARNING regarding the following severely overdue item:\n\n" +
                    "Title: %s\n" +
                    "Due Date: %s\n" +
                    "Days Overdue: %d\n" +
                    "Current Fine: €%.2f\n\n" +
                    "IMMEDIATE ACTION IS REQUIRED. You must:\n" +
                    "1. Return the overdue item within 48 hours\n" +
                    "2. Pay the outstanding fine\n" +
                    "3. Contact the library to discuss this matter\n\n" +
                    "Failure to comply within 48 hours will result in:\n" +
                    "• Suspension of your library account\n" +
                    "• Additional administrative fees\n" +
                    "• Potential legal action for item replacement\n\n" +
                    "This is your final opportunity to resolve this matter before " +
                    "further action is taken.\n\n" +
                    "Contact us immediately: [Library Contact Information]\n\n" +
                    "Library Administration\n" +
                    "Final Notice",
                    memberName, mediaTitle, dueDate, daysOverdue, fineAmount
                );
                break;
        }
        
        messageArea.setText(message);
    }
    
    private void sendNotification() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Message");
            alert.setHeaderText("Message is empty");
            alert.setContentText("Please enter a message or select a template.");
            alert.showAndWait();
            return;
        }
        
        // In a real application, this would send an email/SMS
        // For now, we'll show a confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.INFORMATION);
        confirmAlert.setTitle("Notification Sent");
        confirmAlert.setHeaderText("Notification sent successfully");
        confirmAlert.setContentText(String.format(
            "Message sent to %s\n\nEmail: %s\nPhone: %s\n\nMessage length: %d characters",
            loan.getMemberName(),
            loan.getMember().getEmail(),
            loan.getMember().getPhone(),
            message.length()
        ));
        confirmAlert.showAndWait();
        
        // Add a note to the loan
        String existingNotes = loan.getNotes() != null ? loan.getNotes() : "";
        String newNote = "\nContacted member on " + java.time.LocalDate.now() + 
                        " - Template: " + (templateCombo.getValue() != null ? templateCombo.getValue() : "Custom");
        loan.setNotes(existingNotes + newNote);
        dbManager.updateLoan(loan);
    }
}