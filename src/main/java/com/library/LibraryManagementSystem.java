package com.library;

import com.library.service.DatabaseManager;
import com.library.ui.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LibraryManagementSystem extends Application {
    private DatabaseManager dbManager; // MySQL database manager
    private BorderPane mainLayout;
    private VBox sideMenu;
    private StackPane contentArea;
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        initializeDatabase();
        
        dbManager = DatabaseManager.getInstance();
        
        primaryStage.setTitle("Library Management System");
        primaryStage.setMaximized(true);
        
        setupUI();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/library-style.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        showDashboard();
    }
    
    private void initializeDatabase() {
        System.out.println("Initializing database...");
        try {
            // Database initialization is now handled by DatabaseManager itself
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void setupUI() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");
        
        setupHeader();
        setupSideMenu();
        setupContentArea();
        
        mainLayout.setTop(createHeader());
        mainLayout.setLeft(sideMenu);
        mainLayout.setCenter(contentArea);
    }
    
    private Node createHeader() {
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        
        Label title = new Label("Library Management System");
        title.getStyleClass().add("header-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("Logged in as: Admin");
        userLabel.getStyleClass().add("header-user");
        
        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.setOnAction(e -> refreshCurrentView());
        
        header.getChildren().addAll(title, spacer, userLabel, refreshBtn);
        return header;
    }
    
    private void setupHeader() {
        // Header is created in createHeader method
    }
    
    private void setupSideMenu() {
        sideMenu = new VBox(10);
        sideMenu.getStyleClass().add("side-menu");
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(250);
        
        Label menuTitle = new Label("Navigation");
        menuTitle.getStyleClass().add("menu-title");
        
        Button dashboardBtn = createMenuButton("Dashboard", "📊");
        dashboardBtn.setOnAction(e -> showDashboard());
        
        Button membersBtn = createMenuButton("Members", "👥");
        membersBtn.setOnAction(e -> showMemberManagement());
        
        Button mediaBtn = createMenuButton("Media", "📚");
        mediaBtn.setOnAction(e -> showMediaManagement());
        
        Button loansBtn = createMenuButton("Loans", "📋");
        loansBtn.setOnAction(e -> showLoanManagement());
        
        Button overdueBtn = createMenuButton("Overdue Items", "⚠️");
        overdueBtn.setOnAction(e -> showOverdueLoans());
        
        Button finesBtn = createMenuButton("Fines", "💰");
        finesBtn.setOnAction(e -> showFineManagement());
        
        Button statisticsBtn = createMenuButton("Statistics", "📈");
        statisticsBtn.setOnAction(e -> showStatistics());
        
        Separator separator = new Separator();
        
        Button aboutBtn = createMenuButton("About", "ℹ️");
        aboutBtn.setOnAction(e -> showAbout());
        
        sideMenu.getChildren().addAll(
            menuTitle,
            new Separator(),
            dashboardBtn,
            membersBtn,
            mediaBtn,
            loansBtn,
            overdueBtn,
            finesBtn,
            statisticsBtn,
            separator,
            aboutBtn
        );
    }
    
    private Button createMenuButton(String text, String icon) {
        Button button = new Button(icon + "  " + text);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }
    
    private void setupContentArea() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(20));
    }
    
    private void showDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.getStyleClass().add("dashboard");
        
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        
        // Statistics cards
        HBox statsCards = createStatsCards();
        
        // Quick Actions
        HBox quickActions = createQuickActions();
        
        // Charts
        HBox charts = createCharts();
        
        dashboard.getChildren().addAll(title, statsCards, quickActions, charts);
        
        ScrollPane scrollPane = new ScrollPane(dashboard);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        VBox totalMembers = createStatsCard("Total Members", String.valueOf(dbManager.getTotalMembers()), "👥");
        VBox totalMedia = createStatsCard("Total Media", String.valueOf(dbManager.getTotalMedia()), "📚");
        VBox activeLoans = createStatsCard("Active Loans", String.valueOf(dbManager.getTotalActiveLoans()), "📋");
        VBox overdueLoans = createStatsCard("Overdue Loans", String.valueOf(dbManager.getTotalOverdueLoans()), "⚠️");
        VBox outstandingFines = createStatsCard("Outstanding Fines", 
            String.format("€%.2f", dbManager.getTotalOutstandingFines()), "💰");
        
        statsBox.getChildren().addAll(totalMembers, totalMedia, activeLoans, overdueLoans, outstandingFines);
        return statsBox;
    }
    
    private VBox createStatsCard(String title, String value, String icon) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stats-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stats-icon");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stats-value");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stats-title");
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }
    
    private HBox createQuickActions() {
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label quickActionsTitle = new Label("Quick Actions");
        quickActionsTitle.getStyleClass().add("section-title");
        
        VBox actionsContainer = new VBox(10);
        
        Button addMemberBtn = new Button("➕ Add New Member");
        addMemberBtn.getStyleClass().add("action-button");
        addMemberBtn.setOnAction(e -> showAddMemberDialog());
        
        Button addMediaBtn = new Button("➕ Add New Media");
        addMediaBtn.getStyleClass().add("action-button");
        addMediaBtn.setOnAction(e -> showAddMediaDialog());
        
        Button createLoanBtn = new Button("➕ Create New Loan");
        createLoanBtn.getStyleClass().add("action-button");
        createLoanBtn.setOnAction(e -> showAddLoanDialog());
        
        Button generateFinesBtn = new Button("💰 Generate Overdue Fines");
        generateFinesBtn.getStyleClass().add("action-button");
        generateFinesBtn.setOnAction(e -> {
            dbManager.generateOverdueFines();
            showAlert("Fines Generated", "Overdue fines have been generated successfully!");
            refreshCurrentView();
        });
        
        actionsBox.getChildren().addAll(addMemberBtn, addMediaBtn, createLoanBtn, generateFinesBtn);
        
        actionsContainer.getChildren().addAll(quickActionsTitle, actionsBox);
        return new HBox(actionsContainer);
    }
    
    private HBox createCharts() {
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        
        // Media type distribution pie chart
        PieChart mediaChart = new PieChart();
        mediaChart.setTitle("Media Distribution by Type");
        mediaChart.getData().add(new PieChart.Data("Books", 
            dbManager.getAllMedia().stream().filter(m -> "Book".equals(m.getType())).count()));
        mediaChart.getData().add(new PieChart.Data("DVDs", 
            dbManager.getAllMedia().stream().filter(m -> "DVD".equals(m.getType())).count()));
        mediaChart.getData().add(new PieChart.Data("CDs", 
            dbManager.getAllMedia().stream().filter(m -> "CD".equals(m.getType())).count()));
        
        // Loan status bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> loanChart = new BarChart<>(xAxis, yAxis);
        loanChart.setTitle("Loan Statistics");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Count");
        series.getData().add(new XYChart.Data<>("Active", dbManager.getTotalActiveLoans()));
        series.getData().add(new XYChart.Data<>("Overdue", dbManager.getTotalOverdueLoans()));
        series.getData().add(new XYChart.Data<>("Returned", 
            dbManager.getAllLoans().stream().filter(l -> "Returned".equals(l.getStatus())).count()));
        
        loanChart.getData().add(series);
        
        chartsBox.getChildren().addAll(mediaChart, loanChart);
        return chartsBox;
    }
    
    
    private void showMemberManagement() {
        MemberManagementView memberView = new MemberManagementView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(memberView);
    }
    
    private void showMediaManagement() {
        MediaManagementView mediaView = new MediaManagementView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(mediaView);
    }
    
    private void showLoanManagement() {
        LoanManagementView loanView = new LoanManagementView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(loanView);
    }
    
    private void showOverdueLoans() {
        OverdueLoansView overdueView = new OverdueLoansView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(overdueView);
    }
    
    private void showFineManagement() {
        FineManagementView fineView = new FineManagementView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(fineView);
    }
    
    private void showStatistics() {
        StatisticsView statsView = new StatisticsView(dbManager);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(statsView);
    }
    
    private void showAddMemberDialog() {
        MemberDialog dialog = new MemberDialog(dbManager);
        dialog.showAndWait().ifPresent(member -> {
            refreshCurrentView();
            showAlert("Success", "Member added successfully!");
        });
    }
    
    private void showAddMediaDialog() {
        MediaDialog dialog = new MediaDialog(dbManager);
        dialog.showAndWait().ifPresent(media -> {
            refreshCurrentView();
            showAlert("Success", "Media added successfully!");
        });
    }
    
    private void showAddLoanDialog() {
        LoanDialog dialog = new LoanDialog(dbManager);
        dialog.showAndWait().ifPresent(loan -> {
            refreshCurrentView();
            showAlert("Success", "Loan created successfully!");
        });
    }
    
    private void refreshCurrentView() {
        // This would refresh the current view - for now just refresh dashboard
        showDashboard();
    }
    
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Library Management System");
        alert.setContentText("Version 1.0.0\nBuilt with JavaFX\nDeveloped for IN257 Relational Databases");
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}