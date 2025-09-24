package com.library.ui;

import com.library.model.Member;
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

import java.time.LocalDate;
import java.util.Optional;

public class MemberManagementView extends VBox {
    private DatabaseManager dbManager;
    private TableView<Member> memberTable;
    private FilteredList<Member> filteredMembers;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    
    public MemberManagementView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
        loadMembers();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("management-view");
        
        // Title
        Label title = new Label("Member Management");
        title.getStyleClass().add("page-title");
        
        // Search and Filter Bar
        HBox searchBar = createSearchBar();
        
        // Member Table
        memberTable = createMemberTable();
        VBox.setVgrow(memberTable, Priority.ALWAYS);
        
        // Action Buttons
        HBox actionButtons = createActionButtons();
        
        getChildren().addAll(title, searchBar, actionButtons, memberTable);
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.getStyleClass().add("search-bar");
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search members by name, email, or ID...");
        searchField.setPrefWidth(300);
        
        Label filterLabel = new Label("Status:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Active", "Suspended", "Inactive");
        statusFilter.setValue("All");
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("All");
        });
        
        searchBar.getChildren().addAll(searchLabel, searchField, filterLabel, statusFilter, clearBtn);
        return searchBar;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-bar");
        
        Button addBtn = new Button("➕ Add Member");
        addBtn.getStyleClass().add("action-button");
        addBtn.setOnAction(e -> showAddMemberDialog());
        
        Button editBtn = new Button("✏️ Edit Member");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> showEditMemberDialog());
        editBtn.disableProperty().bind(memberTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button deleteBtn = new Button("🗑️ Delete Member");
        deleteBtn.getStyleClass().add("action-button");
        deleteBtn.setOnAction(e -> deleteMember());
        deleteBtn.disableProperty().bind(memberTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button viewLoansBtn = new Button("📋 View Loans");
        viewLoansBtn.getStyleClass().add("action-button");
        viewLoansBtn.setOnAction(e -> showMemberLoans());
        viewLoansBtn.disableProperty().bind(memberTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("action-button");
        refreshBtn.setOnAction(e -> loadMembers());
        
        buttonBox.getChildren().addAll(addBtn, editBtn, deleteBtn, viewLoansBtn, refreshBtn);
        return buttonBox;
    }
    
    private TableView<Member> createMemberTable() {
        TableView<Member> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        // ID Column
        TableColumn<Member, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        // Name Column
        TableColumn<Member, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return cellData.getValue().getFullName();
                }
            });
        nameCol.setPrefWidth(200);
        
        // Email Column
        TableColumn<Member, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);
        
        // Phone Column
        TableColumn<Member, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(150);
        
        // Status Column
        TableColumn<Member, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        // Current Loans Column
        TableColumn<Member, Integer> loansCol = new TableColumn<>("Current Loans");
        loansCol.setCellValueFactory(new PropertyValueFactory<>("currentLoans"));
        loansCol.setPrefWidth(120);
        
        // Max Loans Column
        TableColumn<Member, Integer> maxLoansCol = new TableColumn<>("Max Loans");
        maxLoansCol.setCellValueFactory(new PropertyValueFactory<>("maxLoans"));
        maxLoansCol.setPrefWidth(100);
        
        // Member Since Column
        TableColumn<Member, LocalDate> memberSinceCol = new TableColumn<>("Member Since");
        memberSinceCol.setCellValueFactory(new PropertyValueFactory<>("memberSince"));
        memberSinceCol.setPrefWidth(120);
        
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, statusCol, loansCol, maxLoansCol, memberSinceCol);
        
        // Row selection
        table.setRowFactory(tv -> {
            TableRow<Member> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditMemberDialog();
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void loadMembers() {
        ObservableList<Member> members = dbManager.getAllMembers();
        filteredMembers = new FilteredList<>(members);
        
        // Setup search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMembers.setPredicate(member -> {
                if (newValue == null || newValue.isEmpty()) {
                    return matchesStatusFilter(member);
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                boolean matchesSearch = member.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                                      member.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                                      String.valueOf(member.getId()).contains(lowerCaseFilter);
                
                return matchesSearch && matchesStatusFilter(member);
            });
        });
        
        // Setup status filter
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredMembers.setPredicate(member -> {
                String searchText = searchField.getText();
                boolean matchesSearch = true;
                
                if (searchText != null && !searchText.isEmpty()) {
                    String lowerCaseFilter = searchText.toLowerCase();
                    matchesSearch = member.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                                   member.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                                   String.valueOf(member.getId()).contains(lowerCaseFilter);
                }
                
                return matchesSearch && matchesStatusFilter(member);
            });
        });
        
        SortedList<Member> sortedMembers = new SortedList<>(filteredMembers);
        sortedMembers.comparatorProperty().bind(memberTable.comparatorProperty());
        
        memberTable.setItems(sortedMembers);
    }
    
    private boolean matchesStatusFilter(Member member) {
        String statusFilterValue = statusFilter.getValue();
        return statusFilterValue == null || "All".equals(statusFilterValue) || 
               statusFilterValue.equals(member.getStatus());
    }
    
    private void showAddMemberDialog() {
        MemberDialog dialog = new MemberDialog(dbManager);
        Optional<Member> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadMembers();
            showAlert("Success", "Member added successfully!");
        }
    }
    
    private void showEditMemberDialog() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            MemberDialog dialog = new MemberDialog(dbManager, selectedMember);
            Optional<Member> result = dialog.showAndWait();
            if (result.isPresent()) {
                loadMembers();
                showAlert("Success", "Member updated successfully!");
            }
        }
    }
    
    private void deleteMember() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Member");
            confirmAlert.setHeaderText("Delete " + selectedMember.getFullName() + "?");
            confirmAlert.setContentText("Are you sure you want to delete this member? This action cannot be undone.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (selectedMember.getCurrentLoans() > 0) {
                    showAlert("Cannot Delete", "Cannot delete member with active loans. Please return all items first.");
                } else {
                    dbManager.deleteMember(selectedMember);
                    loadMembers();
                    showAlert("Success", "Member deleted successfully!");
                }
            }
        }
    }
    
    private void showMemberLoans() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            MemberLoansDialog dialog = new MemberLoansDialog(dbManager, selectedMember);
            dialog.showAndWait();
            loadMembers(); // Refresh in case loans were updated
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