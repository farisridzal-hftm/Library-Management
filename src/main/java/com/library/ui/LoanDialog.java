package com.library.ui;

import com.library.model.Loan;
import com.library.model.Media;
import com.library.model.Member;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoanDialog extends Dialog<Loan> {
    private ComboBox<Member> memberCombo;
    private ComboBox<Media> mediaCombo;
    private DatePicker loanDatePicker;
    private DatePicker dueDatePicker;
    private TextArea notesField;
    private Label memberInfoLabel;
    private Label mediaInfoLabel;
    
    private Loan loan;
    private DatabaseManager dbManager;
    
    public LoanDialog(DatabaseManager dbManager) {
        this(dbManager, null);
    }
    
    public LoanDialog(DatabaseManager dbManager, Loan loan) {
        this.dbManager = dbManager;
        this.loan = loan;
        
        setTitle(loan == null ? "Create New Loan" : "Edit Loan");
        setHeaderText(loan == null ? "Create a new loan" : "Edit loan information");
        
        setupUI();
        setupButtons();
        setupEventHandlers();
        
        if (loan != null) {
            populateFields();
        } else {
            loanDatePicker.setValue(LocalDate.now());
        }
        
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createLoanFromFields();
            }
            return null;
        });
    }
    
    private void setupUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Member selection
        grid.add(new Label("Member:"), 0, 0);
        memberCombo = new ComboBox<>();
        memberCombo.setPromptText("Select member");
        memberCombo.setPrefWidth(300);
        
        // Filter to show only active members who can borrow
        List<Member> eligibleMembers = dbManager.getAllMembers().stream()
            .filter(Member::canBorrow)
            .collect(Collectors.toList());
        memberCombo.getItems().addAll(eligibleMembers);
        
        grid.add(memberCombo, 1, 0);
        
        // Member info label
        memberInfoLabel = new Label();
        memberInfoLabel.getStyleClass().add("info-label");
        grid.add(memberInfoLabel, 1, 1);
        
        // Media selection
        grid.add(new Label("Media:"), 0, 2);
        mediaCombo = new ComboBox<>();
        mediaCombo.setPromptText("Select media");
        mediaCombo.setPrefWidth(300);
        
        // Filter to show only available media
        List<Media> availableMedia = dbManager.getAllMedia().stream()
            .filter(Media::isAvailable)
            .collect(Collectors.toList());
        mediaCombo.getItems().addAll(availableMedia);
        
        grid.add(mediaCombo, 1, 2);
        
        // Media info label
        mediaInfoLabel = new Label();
        mediaInfoLabel.getStyleClass().add("info-label");
        grid.add(mediaInfoLabel, 1, 3);
        
        // Loan date
        grid.add(new Label("Loan Date:"), 0, 4);
        loanDatePicker = new DatePicker();
        loanDatePicker.setPromptText("Select loan date");
        grid.add(loanDatePicker, 1, 4);
        
        // Due date (calculated automatically)
        grid.add(new Label("Due Date:"), 0, 5);
        dueDatePicker = new DatePicker();
        dueDatePicker.setDisable(true);
        dueDatePicker.setPromptText("Calculated automatically");
        grid.add(dueDatePicker, 1, 5);
        
        // Notes
        grid.add(new Label("Notes:"), 0, 6);
        notesField = new TextArea();
        notesField.setPromptText("Enter any notes (optional)");
        notesField.setPrefRowCount(3);
        notesField.setPrefWidth(300);
        grid.add(notesField, 1, 6);
        
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
    
    private void setupEventHandlers() {
        // Update member info when selection changes
        memberCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMemberInfo(newValue);
        });
        
        // Update media info when selection changes
        mediaCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMediaInfo(newValue);
            calculateDueDate();
        });
        
        // Recalculate due date when loan date changes
        loanDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateDueDate();
        });
    }
    
    private void updateMemberInfo(Member member) {
        if (member != null) {
            memberInfoLabel.setText(String.format(
                "Current loans: %d/%d | Status: %s | Email: %s", 
                member.getCurrentLoans(), member.getMaxLoans(), 
                member.getStatus(), member.getEmail()));
        } else {
            memberInfoLabel.setText("");
        }
    }
    
    private void updateMediaInfo(Media media) {
        if (media != null) {
            mediaInfoLabel.setText(String.format(
                "Available: %d/%d | Type: %s | Location: %s", 
                media.getAvailableCopies(), media.getTotalCopies(),
                media.getType(), media.getLocation()));
        } else {
            mediaInfoLabel.setText("");
        }
    }
    
    private void calculateDueDate() {
        if (loanDatePicker.getValue() != null && mediaCombo.getValue() != null) {
            LocalDate loanDate = loanDatePicker.getValue();
            Media media = mediaCombo.getValue();
            LocalDate dueDate = loanDate.plusDays(media.getLoanDurationDays());
            dueDatePicker.setValue(dueDate);
        }
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (memberCombo.getValue() == null) {
            errors.append("- Member must be selected\n");
        } else {
            Member member = memberCombo.getValue();
            if (!member.canBorrow()) {
                errors.append("- Selected member cannot borrow (inactive or at loan limit)\n");
            }
        }
        
        if (mediaCombo.getValue() == null) {
            errors.append("- Media must be selected\n");
        } else {
            Media media = mediaCombo.getValue();
            if (!media.isAvailable()) {
                errors.append("- Selected media is not available\n");
            }
        }
        
        if (loanDatePicker.getValue() == null) {
            errors.append("- Loan date is required\n");
        } else if (loanDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("- Loan date cannot be in the future\n");
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
        memberCombo.setValue(loan.getMember());
        mediaCombo.setValue(loan.getMedia());
        loanDatePicker.setValue(loan.getLoanDate());
        dueDatePicker.setValue(loan.getDueDate());
        notesField.setText(loan.getNotes());
        
        updateMemberInfo(loan.getMember());
        updateMediaInfo(loan.getMedia());
        
        // For existing loans, disable member and media selection
        memberCombo.setDisable(true);
        mediaCombo.setDisable(true);
    }
    
    private Loan createLoanFromFields() {
        if (loan == null) {
            loan = new Loan();
            loan.setMember(memberCombo.getValue());
            loan.setMedia(mediaCombo.getValue());
            loan.setLoanDate(loanDatePicker.getValue());
        }
        
        loan.setNotes(notesField.getText().trim());
        
        if (loan.getId() == 0) {
            dbManager.addLoan(loan);
        } else {
            dbManager.updateLoan(loan);
        }
        
        return loan;
    }
}