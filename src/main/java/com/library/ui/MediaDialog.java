package com.library.ui;

import com.library.model.Author;
import com.library.model.Category;
import com.library.model.Media;
import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class MediaDialog extends Dialog<Media> {
    private TextField titleField;
    private TextField isbnField;
    private Spinner<Integer> publishYearSpinner;
    private TextField publisherField;
    private ComboBox<String> typeCombo;
    private Spinner<Integer> totalCopiesSpinner;
    private TextField locationField;
    private ComboBox<Author> authorCombo;
    private ComboBox<Category> categoryCombo;
    private TextArea descriptionField;
    private ComboBox<String> languageCombo;
    
    private Media media;
    private DatabaseManager dbManager;
    
    public MediaDialog(DatabaseManager dbManager) {
        this(dbManager, null);
    }
    
    public MediaDialog(DatabaseManager dbManager, Media media) {
        this.dbManager = dbManager;
        this.media = media;
        
        setTitle(media == null ? "Add New Media" : "Edit Media");
        setHeaderText(media == null ? "Enter media information" : "Edit media information");
        
        setupUI();
        setupButtons();
        
        if (media != null) {
            populateFields();
        }
        
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createMediaFromFields();
            }
            return null;
        });
    }
    
    private void setupUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Title
        grid.add(new Label("Title:"), 0, 0);
        titleField = new TextField();
        titleField.setPromptText("Enter title");
        titleField.setPrefWidth(300);
        grid.add(titleField, 1, 0);
        
        // ISBN
        grid.add(new Label("ISBN:"), 0, 1);
        isbnField = new TextField();
        isbnField.setPromptText("Enter ISBN");
        grid.add(isbnField, 1, 1);
        
        // Publish Year
        grid.add(new Label("Publish Year:"), 0, 2);
        publishYearSpinner = new Spinner<>(1800, java.time.LocalDate.now().getYear(), 2000);
        publishYearSpinner.setEditable(true);
        publishYearSpinner.setPrefWidth(150);
        grid.add(publishYearSpinner, 1, 2);
        
        // Publisher
        grid.add(new Label("Publisher:"), 0, 3);
        publisherField = new TextField();
        publisherField.setPromptText("Enter publisher");
        grid.add(publisherField, 1, 3);
        
        // Type
        grid.add(new Label("Type:"), 0, 4);
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Book", "DVD", "CD", "Magazine", "Journal");
        typeCombo.setValue("Book");
        grid.add(typeCombo, 1, 4);
        
        // Total Copies
        grid.add(new Label("Total Copies:"), 0, 5);
        totalCopiesSpinner = new Spinner<>(1, 50, 1);
        totalCopiesSpinner.setEditable(true);
        totalCopiesSpinner.setPrefWidth(150);
        grid.add(totalCopiesSpinner, 1, 5);
        
        // Location
        grid.add(new Label("Location:"), 0, 6);
        locationField = new TextField();
        locationField.setPromptText("Enter shelf location");
        grid.add(locationField, 1, 6);
        
        // Author
        grid.add(new Label("Author:"), 0, 7);
        authorCombo = new ComboBox<>();
        authorCombo.getItems().addAll(dbManager.getAllAuthors());
        authorCombo.setPromptText("Select author");
        grid.add(authorCombo, 1, 7);
        
        // Category
        grid.add(new Label("Category:"), 0, 8);
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(dbManager.getAllCategories());
        categoryCombo.setPromptText("Select category");
        grid.add(categoryCombo, 1, 8);
        
        // Language
        grid.add(new Label("Language:"), 0, 9);
        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("English", "German", "French", "Spanish", "Italian");
        languageCombo.setValue("English");
        grid.add(languageCombo, 1, 9);
        
        // Description
        grid.add(new Label("Description:"), 0, 10);
        descriptionField = new TextArea();
        descriptionField.setPromptText("Enter description");
        descriptionField.setPrefRowCount(3);
        descriptionField.setPrefWidth(300);
        grid.add(descriptionField, 1, 10);
        
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
        
        if (titleField.getText().trim().isEmpty()) {
            errors.append("- Title is required\n");
        }
        
        if (isbnField.getText().trim().isEmpty()) {
            errors.append("- ISBN is required\n");
        }
        
        if (publisherField.getText().trim().isEmpty()) {
            errors.append("- Publisher is required\n");
        }
        
        if (locationField.getText().trim().isEmpty()) {
            errors.append("- Location is required\n");
        }
        
        if (authorCombo.getValue() == null) {
            errors.append("- Author must be selected\n");
        }
        
        if (categoryCombo.getValue() == null) {
            errors.append("- Category must be selected\n");
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
        titleField.setText(media.getTitle());
        isbnField.setText(media.getIsbn());
        publishYearSpinner.getValueFactory().setValue(media.getPublishYear());
        publisherField.setText(media.getPublisher());
        typeCombo.setValue(media.getType());
        totalCopiesSpinner.getValueFactory().setValue(media.getTotalCopies());
        locationField.setText(media.getLocation());
        authorCombo.setValue(media.getAuthor());
        categoryCombo.setValue(media.getCategory());
        descriptionField.setText(media.getDescription());
        languageCombo.setValue(media.getLanguage());
    }
    
    private Media createMediaFromFields() {
        if (media == null) {
            media = new Media();
        }
        
        int oldTotalCopies = media.getTotalCopies();
        int oldAvailableCopies = media.getAvailableCopies();
        
        media.setTitle(titleField.getText().trim());
        media.setIsbn(isbnField.getText().trim());
        media.setPublishYear(publishYearSpinner.getValue());
        media.setPublisher(publisherField.getText().trim());
        media.setType(typeCombo.getValue());
        media.setTotalCopies(totalCopiesSpinner.getValue());
        media.setLocation(locationField.getText().trim());
        media.setAuthor(authorCombo.getValue());
        media.setCategory(categoryCombo.getValue());
        media.setDescription(descriptionField.getText().trim());
        media.setLanguage(languageCombo.getValue());
        
        // Adjust available copies if total copies changed
        if (oldTotalCopies != media.getTotalCopies()) {
            int difference = media.getTotalCopies() - oldTotalCopies;
            media.setAvailableCopies(oldAvailableCopies + difference);
        }
        
        if (media.getId() == 0) {
            dbManager.addMedia(media);
        } else {
            dbManager.updateMedia(media);
        }
        
        return media;
    }
}