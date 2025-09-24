package com.library.ui;

import com.library.model.Member;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.Optional;

public class MemberDialog extends Dialog<Member> {
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private TextArea addressField;
    private DatePicker birthDatePicker;
    private ComboBox<String> statusCombo;
    private Spinner<Integer> maxLoansSpinner;
    
    private Member member;
    private DatabaseManager dbManager;
    
    public MemberDialog(DatabaseManager dbManager) {
        this(dbManager, null);
    }
    
    public MemberDialog(DatabaseManager dbManager, Member member) {
        this.dbManager = dbManager;
        this.member = member;
        
        setTitle(member == null ? "Add New Member" : "Edit Member");
        setHeaderText(member == null ? "Enter member information" : "Edit member information");
        
        setupUI();
        setupButtons();
        
        if (member != null) {
            populateFields();
        }
        
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createMemberFromFields();
            }
            return null;
        });
    }
    
    private void setupUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // First Name
        grid.add(new Label("First Name:"), 0, 0);
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        grid.add(firstNameField, 1, 0);
        
        // Last Name
        grid.add(new Label("Last Name:"), 0, 1);
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        grid.add(lastNameField, 1, 1);
        
        // Email
        grid.add(new Label("Email:"), 0, 2);
        emailField = new TextField();
        emailField.setPromptText("Enter email address");
        grid.add(emailField, 1, 2);
        
        // Phone
        grid.add(new Label("Phone:"), 0, 3);
        phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        grid.add(phoneField, 1, 3);
        
        // Address
        grid.add(new Label("Address:"), 0, 4);
        addressField = new TextArea();
        addressField.setPromptText("Enter address");
        addressField.setPrefRowCount(3);
        grid.add(addressField, 1, 4);
        
        // Birth Date
        grid.add(new Label("Birth Date:"), 0, 5);
        birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Select birth date");
        grid.add(birthDatePicker, 1, 5);
        
        // Status
        grid.add(new Label("Status:"), 0, 6);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Suspended", "Inactive");
        statusCombo.setValue("Active");
        grid.add(statusCombo, 1, 6);
        
        // Max Loans
        grid.add(new Label("Max Loans:"), 0, 7);
        maxLoansSpinner = new Spinner<>(1, 10, 5);
        maxLoansSpinner.setEditable(true);
        grid.add(maxLoansSpinner, 1, 7);
        
        getDialogPane().setContent(grid);
    }
    
    private void setupButtons() {
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateFields()) {
                event.consume();
            }
        });
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("- First name is required\n");
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("- Last name is required\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("- Email format is invalid\n");
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("- Phone number is required\n");
        }
        
        if (addressField.getText().trim().isEmpty()) {
            errors.append("- Address is required\n");
        }
        
        if (birthDatePicker.getValue() == null) {
            errors.append("- Birth date is required\n");
        } else if (birthDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("- Birth date cannot be in the future\n");
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void populateFields() {
        firstNameField.setText(member.getFirstName());
        lastNameField.setText(member.getLastName());
        emailField.setText(member.getEmail());
        phoneField.setText(member.getPhone());
        addressField.setText(member.getAddress());
        birthDatePicker.setValue(member.getBirthDate());
        statusCombo.setValue(member.getStatus());
        maxLoansSpinner.getValueFactory().setValue(member.getMaxLoans());
    }
    
    private Member createMemberFromFields() {
        if (member == null) {
            member = new Member();
        }
        
        member.setFirstName(firstNameField.getText().trim());
        member.setLastName(lastNameField.getText().trim());
        member.setEmail(emailField.getText().trim());
        member.setPhone(phoneField.getText().trim());
        member.setAddress(addressField.getText().trim());
        member.setBirthDate(birthDatePicker.getValue());
        member.setStatus(statusCombo.getValue());
        member.setMaxLoans(maxLoansSpinner.getValue());
        
        if (member.getId() == 0) {
            dbManager.addMember(member);
        } else {
            dbManager.updateMember(member);
        }
        
        return member;
    }
}