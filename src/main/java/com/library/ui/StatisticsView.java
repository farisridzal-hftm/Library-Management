package com.library.ui;

import com.library.service.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsView extends VBox {
    private DatabaseManager dbManager;
    
    public StatisticsView(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setupUI();
    }
    
    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("statistics-view");
        
        // Title
        Label title = new Label("Library Statistics");
        title.getStyleClass().add("page-title");
        
        // Overview Cards
        HBox overviewCards = createOverviewCards();
        
        // Charts in tabs
        TabPane chartsTabPane = createChartsTabPane();
        
        getChildren().addAll(title, overviewCards, chartsTabPane);
    }
    
    private HBox createOverviewCards() {
        HBox cardsContainer = new HBox(20);
        cardsContainer.getStyleClass().add("overview-cards");
        
        // Member Statistics Card
        VBox memberCard = createStatsCard(
            "Member Statistics",
            new String[]{"Total Members", "Active Members", "Suspended Members"},
            new String[]{
                String.valueOf(dbManager.getTotalMembers()),
                String.valueOf(dbManager.getActiveMembers()),
                String.valueOf(dbManager.getTotalMembers() - dbManager.getActiveMembers())
            }
        );
        
        // Media Statistics Card
        VBox mediaCard = createStatsCard(
            "Media Statistics",
            new String[]{"Total Media", "Available Items", "On Loan"},
            new String[]{
                String.valueOf(dbManager.getTotalMedia()),
                String.valueOf(dbManager.getAvailableMediaCount()),
                String.valueOf(dbManager.getTotalMedia() - dbManager.getAvailableMediaCount())
            }
        );
        
        // Loan Statistics Card
        VBox loanCard = createStatsCard(
            "Loan Statistics",
            new String[]{"Active Loans", "Overdue Loans", "Total Loans"},
            new String[]{
                String.valueOf(dbManager.getTotalActiveLoans()),
                String.valueOf(dbManager.getTotalOverdueLoans()),
                String.valueOf(dbManager.getAllLoans().size())
            }
        );
        
        // Financial Statistics Card
        VBox financialCard = createStatsCard(
            "Financial Statistics",
            new String[]{"Outstanding Fines", "Total Fines", "Avg Fine Amount"},
            new String[]{
                String.format("€%.2f", dbManager.getTotalOutstandingFines()),
                String.valueOf(dbManager.getAllFines().size()),
                String.format("€%.2f", dbManager.getAllFines().isEmpty() ? 0 : 
                    dbManager.getAllFines().stream().mapToDouble(f -> f.getAmount()).average().orElse(0))
            }
        );
        
        cardsContainer.getChildren().addAll(memberCard, mediaCard, loanCard, financialCard);
        return cardsContainer;
    }
    
    private VBox createStatsCard(String title, String[] labels, String[] values) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stats-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(15);
        
        for (int i = 0; i < labels.length && i < values.length; i++) {
            Label label = new Label(labels[i] + ":");
            label.getStyleClass().add("card-label");
            
            Label value = new Label(values[i]);
            value.getStyleClass().add("card-value");
            
            grid.add(label, 0, i);
            grid.add(value, 1, i);
        }
        
        card.getChildren().addAll(titleLabel, grid);
        return card;
    }
    
    private TabPane createChartsTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(400);
        
        // Media Distribution Tab
        Tab mediaTab = new Tab("Media Distribution");
        mediaTab.setContent(createMediaDistributionChart());
        mediaTab.setClosable(false);
        
        // Loan Status Tab
        Tab loanTab = new Tab("Loan Status");
        loanTab.setContent(createLoanStatusChart());
        loanTab.setClosable(false);
        
        // Member Activity Tab
        Tab memberTab = new Tab("Member Activity");
        memberTab.setContent(createMemberActivityChart());
        memberTab.setClosable(false);
        
        // Financial Overview Tab
        Tab financialTab = new Tab("Financial Overview");
        financialTab.setContent(createFinancialChart());
        financialTab.setClosable(false);
        
        tabPane.getTabs().addAll(mediaTab, loanTab, memberTab, financialTab);
        return tabPane;
    }
    
    private VBox createMediaDistributionChart() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Media by Type Pie Chart
        PieChart mediaTypeChart = new PieChart();
        mediaTypeChart.setTitle("Media Distribution by Type");
        
        Map<String, Long> mediaByType = dbManager.getAllMedia().stream()
            .collect(Collectors.groupingBy(m -> m.getType(), Collectors.counting()));
        
        mediaByType.forEach((type, count) -> {
            mediaTypeChart.getData().add(new PieChart.Data(type + " (" + count + ")", count));
        });
        
        // Media Availability Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> availabilityChart = new BarChart<>(xAxis, yAxis);
        availabilityChart.setTitle("Media Availability");
        
        XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
        availableSeries.setName("Available");
        
        XYChart.Series<String, Number> loanedSeries = new XYChart.Series<>();
        loanedSeries.setName("On Loan");
        
        mediaByType.forEach((type, count) -> {
            long available = dbManager.getAllMedia().stream()
                .filter(m -> type.equals(m.getType()))
                .mapToLong(m -> m.getAvailableCopies())
                .sum();
            long onLoan = dbManager.getAllMedia().stream()
                .filter(m -> type.equals(m.getType()))
                .mapToLong(m -> m.getTotalCopies() - m.getAvailableCopies())
                .sum();
            
            availableSeries.getData().add(new XYChart.Data<>(type, available));
            loanedSeries.getData().add(new XYChart.Data<>(type, onLoan));
        });
        
        availabilityChart.getData().addAll(availableSeries, loanedSeries);
        
        container.getChildren().addAll(mediaTypeChart, availabilityChart);
        return container;
    }
    
    private VBox createLoanStatusChart() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Loan Status Pie Chart
        PieChart loanStatusChart = new PieChart();
        loanStatusChart.setTitle("Loan Status Distribution");
        
        long activeLoans = dbManager.getActiveLoans().size();
        long overdueLoans = dbManager.getOverdueLoans().size();
        long returnedLoans = dbManager.getAllLoans().stream()
            .filter(l -> "Returned".equals(l.getStatus()))
            .count();
        
        loanStatusChart.getData().add(new PieChart.Data("Active (" + activeLoans + ")", activeLoans));
        loanStatusChart.getData().add(new PieChart.Data("Overdue (" + overdueLoans + ")", overdueLoans));
        loanStatusChart.getData().add(new PieChart.Data("Returned (" + returnedLoans + ")", returnedLoans));
        
        // Monthly Loan Trends (simplified)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> trendChart = new LineChart<>(xAxis, yAxis);
        trendChart.setTitle("Monthly Loan Activity (Last 12 Months)");
        
        XYChart.Series<String, Number> loanSeries = new XYChart.Series<>();
        loanSeries.setName("Loans Created");
        
        // Generate sample trend data
        for (int i = 11; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String monthLabel = date.getMonth().toString().substring(0, 3) + " " + date.getYear();
            int loanCount = (int) (Math.random() * 20) + 5; // Sample data
            loanSeries.getData().add(new XYChart.Data<>(monthLabel, loanCount));
        }
        
        trendChart.getData().add(loanSeries);
        
        container.getChildren().addAll(loanStatusChart, trendChart);
        return container;
    }
    
    private VBox createMemberActivityChart() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Member Status Pie Chart
        PieChart memberStatusChart = new PieChart();
        memberStatusChart.setTitle("Member Status Distribution");
        
        Map<String, Long> memberByStatus = dbManager.getAllMembers().stream()
            .collect(Collectors.groupingBy(m -> m.getStatus(), Collectors.counting()));
        
        memberByStatus.forEach((status, count) -> {
            memberStatusChart.getData().add(new PieChart.Data(status + " (" + count + ")", count));
        });
        
        // Current Loans Distribution
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> loansChart = new BarChart<>(xAxis, yAxis);
        loansChart.setTitle("Current Loans per Member");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Members");
        
        Map<Integer, Long> loanDistribution = dbManager.getAllMembers().stream()
            .collect(Collectors.groupingBy(m -> m.getCurrentLoans(), Collectors.counting()));
        
        for (int i = 0; i <= 5; i++) {
            long count = loanDistribution.getOrDefault(i, 0L);
            series.getData().add(new XYChart.Data<>(i + " loans", count));
        }
        
        loansChart.getData().add(series);
        
        container.getChildren().addAll(memberStatusChart, loansChart);
        return container;
    }
    
    private VBox createFinancialChart() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Fine Status Pie Chart
        PieChart fineStatusChart = new PieChart();
        fineStatusChart.setTitle("Fine Status Distribution");
        
        Map<String, Long> finesByStatus = dbManager.getAllFines().stream()
            .collect(Collectors.groupingBy(f -> f.getStatus(), Collectors.counting()));
        
        finesByStatus.forEach((status, count) -> {
            fineStatusChart.getData().add(new PieChart.Data(status + " (" + count + ")", count));
        });
        
        // Fine Amounts Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> amountChart = new BarChart<>(xAxis, yAxis);
        amountChart.setTitle("Fine Amounts by Status");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Amount (€)");
        
        Map<String, Double> amountsByStatus = dbManager.getAllFines().stream()
            .collect(Collectors.groupingBy(f -> f.getStatus(), 
                Collectors.summingDouble(f -> f.getAmount())));
        
        amountsByStatus.forEach((status, amount) -> {
            series.getData().add(new XYChart.Data<>(status, amount));
        });
        
        amountChart.getData().add(series);
        
        container.getChildren().addAll(fineStatusChart, amountChart);
        return container;
    }
}