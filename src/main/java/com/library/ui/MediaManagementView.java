package com.library.ui;

import com.library.model.Media;
import com.library.service.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class MediaManagementView extends VBox {
    private DatabaseManager dbManager;
    private TableView<Media> mediaTable;
    private FilteredList<Media> filteredMedia;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> availabilityFilter;
    
    public MediaManagementView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
        loadMedia();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("management-view");
        
        // Title
        Label title = new Label("Media Management");
        title.getStyleClass().add("page-title");
        
        // Search and Filter Bar
        HBox searchBar = createSearchBar();
        
        // Media Table
        mediaTable = createMediaTable();
        VBox.setVgrow(mediaTable, Priority.ALWAYS);
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        getChildren().addAll(title, searchBar, actionButtons, mediaTable);
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.getStyleClass().add("search-bar");
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search media by title, author, ISBN...");
        searchField.setPrefWidth(300);
        
        Label typeLabel = new Label("Type:");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All", "Book", "DVD", "CD", "Magazine", "Journal");
        typeFilter.setValue("All");
        
        Label availabilityLabel = new Label("Availability:");
        availabilityFilter = new ComboBox<>();
        availabilityFilter.getItems().addAll("All", "Available", "Unavailable");
        availabilityFilter.setValue("All");
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            typeFilter.setValue("All");
            availabilityFilter.setValue("All");
        });
        
        searchBar.getChildren().addAll(searchLabel, searchField, typeLabel, typeFilter, 
                                     availabilityLabel, availabilityFilter, clearBtn);
        return searchBar;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button addBtn = new Button("➕ Add Media");
        addBtn.getStyleClass().add("action-button");
        addBtn.setOnAction(e -> showAddMediaDialog());
        
        Button editBtn = new Button("✏️ Edit Media");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> showEditMediaDialog());
        editBtn.disableProperty().bind(mediaTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button deleteBtn = new Button("🗑️ Delete Media");
        deleteBtn.getStyleClass().add("action-button");
        deleteBtn.setOnAction(e -> deleteMedia());
        deleteBtn.disableProperty().bind(mediaTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button copyBtn = new Button("📋 Copy Media");
        copyBtn.getStyleClass().add("action-button");
        copyBtn.setOnAction(e -> copyMedia());
        copyBtn.disableProperty().bind(mediaTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadMedia());
        
        buttonBox.getChildren().addAll(addBtn, editBtn, deleteBtn, copyBtn, refreshBtn);
        return buttonBox;
    }
    
    private TableView<Media> createMediaTable() {
        TableView<Media> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // ID Column
        TableColumn<Media, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        // Title Column
        TableColumn<Media, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        // Author Column
        TableColumn<Media, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getAuthorName();
                }
            });
        authorCol.setPrefWidth(150);
        
        // Type Column
        TableColumn<Media, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(80);
        
        // Year Column
        TableColumn<Media, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("publishYear"));
        yearCol.setPrefWidth(70);
        
        // ISBN Column
        TableColumn<Media, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(120);
        
        // Location Column
        TableColumn<Media, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(120);
        
        // Total Copies Column
        TableColumn<Media, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        totalCol.setPrefWidth(70);
        
        // Available Copies Column
        TableColumn<Media, Integer> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        availableCol.setPrefWidth(80);
        
        // Category Column
        TableColumn<Media, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getCategoryName();
                }
            });
        categoryCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, titleCol, authorCol, typeCol, yearCol, isbnCol, 
                                locationCol, totalCol, availableCol, categoryCol);
        
        // Row styling for availability
        table.setRowFactory(tv -> {
            TableRow<Media> row = new TableRow<Media>() {
                @Override
                protected void updateItem(Media media, boolean empty) {
                    super.updateItem(media, empty);
                    if (empty || media == null) {
                        setStyle("");
                    } else if (!media.isAvailable()) {
                        setStyle("-fx-background-color: #ffebee;");
                    } else {
                        setStyle("");
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditMediaDialog();
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    private void loadMedia() {
        ObservableList<Media> mediaList = dbManager.getAllMedia();
        filteredMedia = new FilteredList<>(mediaList);
        
        // Setup search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        // Setup type filter
        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        // Setup availability filter
        availabilityFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        SortedList<Media> sortedMedia = new SortedList<>(filteredMedia);
        sortedMedia.comparatorProperty().bind(mediaTable.comparatorProperty());
        
        mediaTable.setItems(sortedMedia);
    }
    
    private void updateFilters() {
        filteredMedia.setPredicate(media -> {
            // Search filter
            String searchText = searchField.getText();
            boolean matchesSearch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = media.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                              media.getAuthorName().toLowerCase().contains(lowerCaseFilter) ||
                              media.getIsbn().toLowerCase().contains(lowerCaseFilter) ||
                              String.valueOf(media.getId()).contains(lowerCaseFilter);
            }
            
            // Type filter
            String typeFilterValue = typeFilter.getValue();
            boolean matchesType = typeFilterValue == null || "All".equals(typeFilterValue) || 
                                typeFilterValue.equals(media.getType());
            
            // Availability filter
            String availabilityFilterValue = availabilityFilter.getValue();
            boolean matchesAvailability = true;
            if (availabilityFilterValue != null && !"All".equals(availabilityFilterValue)) {
                if ("Available".equals(availabilityFilterValue)) {
                    matchesAvailability = media.isAvailable();
                } else if ("Unavailable".equals(availabilityFilterValue)) {
                    matchesAvailability = !media.isAvailable();
                }
            }
            
            return matchesSearch && matchesType && matchesAvailability;
        });
    }
    
    private void showAddMediaDialog() {
        MediaDialog dialog = new MediaDialog(dbManager);
        Optional<Media> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadMedia();
            showAlert("Success", "Media added successfully!");
        }
    }
    
    private void showEditMediaDialog() {
        Media selectedMedia = mediaTable.getSelectionModel().getSelectedItem();
        if (selectedMedia != null) {
            MediaDialog dialog = new MediaDialog(dbManager, selectedMedia);
            Optional<Media> result = dialog.showAndWait();
            if (result.isPresent()) {
                loadMedia();
                showAlert("Success", "Media updated successfully!");
            }
        }
    }
    
    private void deleteMedia() {
        Media selectedMedia = mediaTable.getSelectionModel().getSelectedItem();
        if (selectedMedia != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Media");
            confirmAlert.setHeaderText("Delete " + selectedMedia.getTitle() + "?");
            confirmAlert.setContentText("Are you sure you want to delete this media item? This action cannot be undone.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int copiesOnLoan = selectedMedia.getTotalCopies() - selectedMedia.getAvailableCopies();
                if (copiesOnLoan > 0) {
                    showAlert("Cannot Delete", "Cannot delete media with copies currently on loan. " +
                            "Wait for all copies to be returned first.");
                } else {
                    dbManager.deleteMedia(selectedMedia);
                    loadMedia();
                    showAlert("Success", "Media deleted successfully!");
                }
            }
        }
    }
    
    private void copyMedia() {
        Media selectedMedia = mediaTable.getSelectionModel().getSelectedItem();
        if (selectedMedia != null) {
            Media copy = new Media();
            copy.setTitle(selectedMedia.getTitle() + " (Copy)");
            copy.setIsbn(selectedMedia.getIsbn() + "-COPY");
            copy.setPublishYear(selectedMedia.getPublishYear());
            copy.setPublisher(selectedMedia.getPublisher());
            copy.setType(selectedMedia.getType());
            copy.setTotalCopies(1);
            copy.setAvailableCopies(1);
            copy.setLocation(selectedMedia.getLocation());
            copy.setAuthor(selectedMedia.getAuthor());
            copy.setCategory(selectedMedia.getCategory());
            copy.setDescription(selectedMedia.getDescription());
            copy.setLanguage(selectedMedia.getLanguage());
            
            MediaDialog dialog = new MediaDialog(dbManager, copy);
            Optional<Media> result = dialog.showAndWait();
            if (result.isPresent()) {
                loadMedia();
                showAlert("Success", "Media copied successfully!");
            }
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