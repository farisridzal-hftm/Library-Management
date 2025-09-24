package com.library.service;

import com.library.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Properties;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Properties config;
    private String sqlFilePath;
    
    private Connection connection;
    
    // Cache for ObservableList to maintain compatibility with existing UI code
    private final ObservableList<Member> membersCache = FXCollections.observableArrayList();
    private final ObservableList<Media> mediaCache = FXCollections.observableArrayList();
    private final ObservableList<Author> authorsCache = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesCache = FXCollections.observableArrayList();
    private final ObservableList<Loan> loansCache = FXCollections.observableArrayList();
    private final ObservableList<Fine> finesCache = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffCache = FXCollections.observableArrayList();
    
    private DatabaseManager() {
        System.out.println("Using H2 Database with SQL file storage");
        loadConfig();
        try {
            // Load H2 JDBC driver
            Class.forName(config.getProperty("database.driver", "org.h2.Driver"));
            
            // Get SQL file path
            sqlFilePath = config.getProperty("database.sql.file", "library_data.sql");
            
            // Connect to H2 in-memory database
            connection = DriverManager.getConnection(
                config.getProperty("database.url", "jdbc:h2:mem:library_management;DB_CLOSE_DELAY=-1;MODE=MySQL"), 
                config.getProperty("database.username", "sa"), 
                config.getProperty("database.password", "")
            );
            
            System.out.println("✓ H2 database connection successful!");
            
            // Create schema and load data
            initializeDatabase();
            
            // Load all data into cache
            loadAllData();
            
            System.out.println("✓ Database initialized with SQL file: " + sqlFilePath);
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadConfig() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (Exception e) {
            System.err.println("Failed to load database config: " + e.getMessage());
        }
    }
    
    private void initializeDatabase() {
        try {
            // Create schema first
            createSchema();
            
            // Load data from SQL file if it exists
            if (Files.exists(Paths.get(sqlFilePath))) {
                System.out.println("Loading data from SQL file: " + sqlFilePath);
                importFromSQLFile();
                loadAllData(); // Load data into cache after import
                System.out.println("DEBUG: Loaded " + membersCache.size() + " members into cache");
            } else {
                System.out.println("SQL file not found, creating with sample data: " + sqlFilePath);
                insertSampleData();
                exportToSQLFile();
            }
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void importFromSQLFile() {
        try {
            String sqlContent = Files.readString(Paths.get(sqlFilePath));
            
            // Split by semicolon and execute each statement
            String[] statements = sqlContent.split(";");
            
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--") && !sql.startsWith("/*")) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Ignore errors for CREATE TABLE statements (they might already exist)
                        if (!sql.toUpperCase().startsWith("CREATE")) {
                            System.err.println("Error executing SQL: " + sql);
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("✓ Data imported from SQL file");
        } catch (IOException e) {
            System.err.println("Error importing from SQL file: " + e.getMessage());
        }
    }
    
    public void exportToSQLFile() {
        if (!Boolean.parseBoolean(config.getProperty("database.sql.auto_export", "true"))) {
            return;
        }
        
        try {
            StringBuilder sqlContent = new StringBuilder();
            sqlContent.append("-- Library Management System Data Export\n");
            sqlContent.append("-- Generated: ").append(java.time.LocalDateTime.now()).append("\n\n");
            
            // Export schema first
            sqlContent.append(getSchemaSQL()).append("\n");
            
            // Export data
            sqlContent.append(exportTableData("authors"));
            sqlContent.append(exportTableData("categories"));
            sqlContent.append(exportTableData("media"));
            sqlContent.append(exportTableData("members"));
            sqlContent.append(exportTableData("staff"));
            sqlContent.append(exportTableData("loans"));
            sqlContent.append(exportTableData("fines"));
            
            Files.writeString(Paths.get(sqlFilePath), sqlContent.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("✓ Data exported to SQL file: " + sqlFilePath);
        } catch (IOException | SQLException e) {
            System.err.println("Error exporting to SQL file: " + e.getMessage());
        }
    }
    
    private String exportTableData(String tableName) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("\n-- Data for table ").append(tableName).append("\n");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Clear existing data
            sql.append("DELETE FROM ").append(tableName).append(";\n");
            
            while (rs.next()) {
                sql.append("INSERT INTO ").append(tableName).append(" VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) sql.append(", ");
                    Object value = rs.getObject(i);
                    if (value == null) {
                        sql.append("NULL");
                    } else if (value instanceof String || value instanceof Date || value instanceof LocalDate) {
                        sql.append("'").append(value.toString().replace("'", "''")).append("'");
                    } else if (value instanceof java.sql.Timestamp) {
                        // Format timestamp for H2 compatibility
                        sql.append("'").append(value.toString()).append("'");
                    } else if (value instanceof Boolean) {
                        sql.append(((Boolean) value) ? "true" : "false");
                    } else {
                        sql.append(value);
                    }
                }
                sql.append(");\n");
            }
        }
        return sql.toString();
    }
    
    private String getSchemaSQL() {
        return """
            CREATE TABLE IF NOT EXISTS authors (
                id INT PRIMARY KEY AUTO_INCREMENT,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                biography TEXT,
                nationality VARCHAR(50)
            );
            
            CREATE TABLE IF NOT EXISTS categories (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(50) NOT NULL UNIQUE,
                description TEXT,
                loan_duration_days INT DEFAULT 14
            );
            
            CREATE TABLE IF NOT EXISTS media (
                id INT PRIMARY KEY AUTO_INCREMENT,
                title VARCHAR(255) NOT NULL,
                isbn VARCHAR(20),
                publish_year INT,
                publisher VARCHAR(255),
                type VARCHAR(20) NOT NULL,
                total_copies INT DEFAULT 1,
                available_copies INT DEFAULT 1,
                location VARCHAR(100),
                author_id INT,
                category_id INT NOT NULL,
                description TEXT,
                language VARCHAR(50) DEFAULT 'English',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS members (
                id INT PRIMARY KEY AUTO_INCREMENT,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                phone VARCHAR(20),
                address TEXT,
                birth_date DATE,
                status VARCHAR(20) DEFAULT 'Active',
                max_loans INT DEFAULT 5,
                current_loans INT DEFAULT 0,
                member_since DATE DEFAULT CURRENT_DATE,
                active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS staff (
                id INT PRIMARY KEY AUTO_INCREMENT,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                phone VARCHAR(20),
                position VARCHAR(100),
                department VARCHAR(100),
                hire_date DATE DEFAULT CURRENT_DATE,
                salary DECIMAL(10,2) DEFAULT 0.00,
                status VARCHAR(20) DEFAULT 'Active',
                username VARCHAR(50) UNIQUE,
                role VARCHAR(20) DEFAULT 'Librarian',
                active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS loans (
                id INT PRIMARY KEY AUTO_INCREMENT,
                member_id INT NOT NULL,
                media_id INT NOT NULL,
                loan_date DATE NOT NULL,
                due_date DATE NOT NULL,
                return_date DATE NULL,
                status VARCHAR(20) DEFAULT 'Active',
                renewal_count INT DEFAULT 0,
                max_renewals INT DEFAULT 2,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS fines (
                id INT PRIMARY KEY AUTO_INCREMENT,
                member_id INT NOT NULL,
                loan_id INT NULL,
                amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                reason VARCHAR(255),
                issue_date DATE DEFAULT CURRENT_DATE,
                paid_date DATE NULL,
                status VARCHAR(20) DEFAULT 'Outstanding',
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
        """;
    }
    
    private void createSchema() throws SQLException {
        String schemaSQL = getSchemaSQL();
        String[] statements = schemaSQL.split(";");
        
        for (String sql : statements) {
            sql = sql.trim();
            if (!sql.isEmpty() && !sql.startsWith("--")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.err.println("Failed to execute SQL: " + sql.substring(0, Math.min(100, sql.length())));
                    System.err.println("Error: " + e.getMessage());
                    throw e;
                }
            }
        }
    }
    
    private void insertSampleData() throws SQLException {
        // Insert categories
        executeUpdate("INSERT INTO categories (name, description, loan_duration_days) VALUES " +
                "('Fiction', 'Literary fiction and novels', 14), " +
                "('Non-Fiction', 'Educational and informational books', 14), " +
                "('Science', 'Scientific literature and research', 21), " +
                "('Children', 'Books for children and young adults', 14)");
        
        // Insert authors  
        executeUpdate("INSERT INTO authors (first_name, last_name, biography, nationality) VALUES " +
                "('Harper', 'Lee', 'American novelist known for To Kill a Mockingbird', 'American'), " +
                "('George', 'Orwell', 'English author and journalist', 'British'), " +
                "('Jane', 'Austen', 'English novelist known for Pride and Prejudice', 'British')");
        
        // Insert sample media
        executeUpdate("INSERT INTO media (title, isbn, publish_year, publisher, type, total_copies, available_copies, location, author_id, category_id, description, language) VALUES " +
                "('To Kill a Mockingbird', '978-0-06-112008-4', 1960, 'J.B. Lippincott & Co.', 'Book', 3, 2, 'Fiction A-L', 1, 1, 'Classic American literature', 'English'), " +
                "('1984', '978-0-452-28423-4', 1949, 'Secker & Warburg', 'Book', 5, 4, 'Fiction M-Z', 2, 1, 'Dystopian social science fiction', 'English'), " +
                "('Pride and Prejudice', '978-0-14-143951-8', 1813, 'T. Egerton', 'Book', 2, 1, 'Fiction A-L', 3, 1, 'Romantic fiction', 'English')");
        
        // Insert sample members
        executeUpdate("INSERT INTO members (first_name, last_name, email, phone, address, birth_date, status, max_loans, current_loans, member_since, active) VALUES " +
                "('John', 'Doe', 'john.doe@email.com', '+41 79 123 45 67', 'Musterstrasse 1, 8000 Zürich', '1985-03-15', 'Active', 5, 1, CURRENT_DATE, TRUE), " +
                "('Jane', 'Smith', 'jane.smith@email.com', '+41 79 234 56 78', 'Beispielweg 12, 3000 Bern', '1990-07-22', 'Active', 5, 1, CURRENT_DATE, TRUE)");
    }
    
    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void loadAllData() {
        loadAuthors();
        loadCategories();
        loadMedia();
        loadMembers();
        loadStaff();
        loadLoans();
        loadFines();
    }
    
    // Author operations
    private void loadAuthors() {
        authorsCache.clear();
        String sql = "SELECT * FROM authors ORDER BY last_name, first_name";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Author author = new Author(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("biography"),
                    rs.getString("nationality")
                );
                authorsCache.add(author);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load authors: " + e.getMessage());
        }
    }
    
    public ObservableList<Author> getAllAuthors() { 
        return authorsCache; 
    }
    
    public void addAuthor(Author author) {
        String sql = "INSERT INTO authors (first_name, last_name, biography, nationality) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, author.getFirstName());
            stmt.setString(2, author.getLastName());
            stmt.setString(3, author.getBiography());
            stmt.setString(4, author.getNationality());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    author.setId(generatedKeys.getInt(1));
                    authorsCache.add(author);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add author: " + e.getMessage());
        }
    }
    
    public void updateAuthor(Author author) {
        String sql = "UPDATE authors SET first_name = ?, last_name = ?, biography = ?, nationality = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, author.getFirstName());
            stmt.setString(2, author.getLastName());
            stmt.setString(3, author.getBiography());
            stmt.setString(4, author.getNationality());
            stmt.setInt(5, author.getId());
            
            stmt.executeUpdate();
            
            // Update cache
            int index = authorsCache.indexOf(author);
            if (index >= 0) {
                authorsCache.set(index, author);
            }
        } catch (SQLException e) {
            System.err.println("Failed to update author: " + e.getMessage());
        }
    }
    
    public void deleteAuthor(Author author) {
        String sql = "DELETE FROM authors WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, author.getId());
            stmt.executeUpdate();
            authorsCache.remove(author);
        } catch (SQLException e) {
            System.err.println("Failed to delete author: " + e.getMessage());
        }
    }
    
    // Category operations
    private void loadCategories() {
        categoriesCache.clear();
        String sql = "SELECT * FROM categories ORDER BY name";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("loan_duration_days")
                );
                categoriesCache.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load categories: " + e.getMessage());
        }
    }
    
    public ObservableList<Category> getAllCategories() { 
        return categoriesCache; 
    }
    
    public void addCategory(Category category) {
        String sql = "INSERT INTO categories (name, description, loan_duration_days) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getLoanDurationDays());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                    categoriesCache.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add category: " + e.getMessage());
        }
    }
    
    public void updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, loan_duration_days = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getLoanDurationDays());
            stmt.setInt(4, category.getId());
            
            stmt.executeUpdate();
            
            int index = categoriesCache.indexOf(category);
            if (index >= 0) {
                categoriesCache.set(index, category);
            }
        } catch (SQLException e) {
            System.err.println("Failed to update category: " + e.getMessage());
        }
    }
    
    public void deleteCategory(Category category) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, category.getId());
            stmt.executeUpdate();
            categoriesCache.remove(category);
        } catch (SQLException e) {
            System.err.println("Failed to delete category: " + e.getMessage());
        }
    }
    
    // Media operations
    private void loadMedia() {
        mediaCache.clear();
        String sql = """
            SELECT m.*, a.first_name, a.last_name, a.biography, a.nationality,
                   c.name as category_name, c.description as category_description, c.loan_duration_days
            FROM media m
            LEFT JOIN authors a ON m.author_id = a.id
            JOIN categories c ON m.category_id = c.id
            ORDER BY m.title
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Author author = null;
                if (rs.getString("first_name") != null) {
                    author = new Author(
                        rs.getInt("author_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("biography"),
                        rs.getString("nationality")
                    );
                }
                
                Category category = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("category_description"),
                    rs.getInt("loan_duration_days")
                );
                
                Media media = new Media(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("isbn"),
                    rs.getInt("publish_year"),
                    rs.getString("publisher"),
                    rs.getString("type"),
                    rs.getInt("total_copies"),
                    rs.getString("location"),
                    author,
                    category
                );
                media.setAvailableCopies(rs.getInt("available_copies"));
                media.setDescription(rs.getString("description"));
                media.setLanguage(rs.getString("language"));
                
                mediaCache.add(media);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load media: " + e.getMessage());
        }
    }
    
    public ObservableList<Media> getAllMedia() { 
        return mediaCache; 
    }
    
    public void addMedia(Media media) {
        String sql = "INSERT INTO media (title, isbn, publish_year, publisher, type, total_copies, available_copies, location, author_id, category_id, description, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getIsbn());
            stmt.setInt(3, media.getPublishYear());
            stmt.setString(4, media.getPublisher());
            stmt.setString(5, media.getType());
            stmt.setInt(6, media.getTotalCopies());
            stmt.setInt(7, media.getAvailableCopies());
            stmt.setString(8, media.getLocation());
            stmt.setObject(9, media.getAuthor() != null ? media.getAuthor().getId() : null);
            stmt.setInt(10, media.getCategory().getId());
            stmt.setString(11, media.getDescription());
            stmt.setString(12, media.getLanguage());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    media.setId(generatedKeys.getInt(1));
                    mediaCache.add(media);
                    exportToSQLFile();
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add media: " + e.getMessage());
        }
    }
    
    public void updateMedia(Media media) {
        String sql = "UPDATE media SET title = ?, isbn = ?, publish_year = ?, publisher = ?, type = ?, total_copies = ?, available_copies = ?, location = ?, author_id = ?, category_id = ?, description = ?, language = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getIsbn());
            stmt.setInt(3, media.getPublishYear());
            stmt.setString(4, media.getPublisher());
            stmt.setString(5, media.getType());
            stmt.setInt(6, media.getTotalCopies());
            stmt.setInt(7, media.getAvailableCopies());
            stmt.setString(8, media.getLocation());
            stmt.setObject(9, media.getAuthor() != null ? media.getAuthor().getId() : null);
            stmt.setInt(10, media.getCategory().getId());
            stmt.setString(11, media.getDescription());
            stmt.setString(12, media.getLanguage());
            stmt.setInt(13, media.getId());
            
            stmt.executeUpdate();
            
            int index = mediaCache.indexOf(media);
            if (index >= 0) {
                mediaCache.set(index, media);
            }
            exportToSQLFile();
        } catch (SQLException e) {
            System.err.println("Failed to update media: " + e.getMessage());
        }
    }
    
    public void deleteMedia(Media media) {
        String sql = "DELETE FROM media WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, media.getId());
            stmt.executeUpdate();
            mediaCache.remove(media);
            exportToSQLFile();
        } catch (SQLException e) {
            System.err.println("Failed to delete media: " + e.getMessage());
        }
    }
    
    public Media findMediaById(int id) {
        return mediaCache.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }
    
    public List<Media> searchMedia(String searchTerm) {
        return mediaCache.stream()
            .filter(m -> m.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        m.getIsbn().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        m.getAuthorName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        String.valueOf(m.getId()).contains(searchTerm))
            .collect(Collectors.toList());
    }
    
    public List<Media> getAvailableMedia() {
        return mediaCache.stream().filter(Media::isAvailable).collect(Collectors.toList());
    }
    
    // Member operations
    private void loadMembers() {
        membersCache.clear();
        String sql = "SELECT * FROM members ORDER BY last_name, first_name";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Member member = new Member(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getDate("birth_date").toLocalDate()
                );
                member.setStatus(rs.getString("status"));
                member.setMaxLoans(rs.getInt("max_loans"));
                member.setCurrentLoans(rs.getInt("current_loans"));
                member.setMemberSince(rs.getDate("member_since").toLocalDate());
                // member.setActive(rs.getBoolean("active")); // Member has no setActive method
                
                membersCache.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load members: " + e.getMessage());
        }
    }
    
    public ObservableList<Member> getAllMembers() { 
        return membersCache; 
    }
    
    public void addMember(Member member) {
        String sql = "INSERT INTO members (first_name, last_name, email, phone, address, birth_date, status, max_loans, current_loans, member_since, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getAddress());
            stmt.setDate(6, Date.valueOf(member.getBirthDate()));
            stmt.setString(7, member.getStatus());
            stmt.setInt(8, member.getMaxLoans());
            stmt.setInt(9, member.getCurrentLoans());
            stmt.setDate(10, Date.valueOf(member.getMemberSince()));
            stmt.setBoolean(11, member.isActive());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setId(generatedKeys.getInt(1));
                    membersCache.add(member);
                    exportToSQLFile(); // Auto-export after data change
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add member: " + e.getMessage());
        }
    }
    
    public void updateMember(Member member) {
        String sql = "UPDATE members SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, birth_date = ?, status = ?, max_loans = ?, current_loans = ?, member_since = ?, active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getAddress());
            stmt.setDate(6, Date.valueOf(member.getBirthDate()));
            stmt.setString(7, member.getStatus());
            stmt.setInt(8, member.getMaxLoans());
            stmt.setInt(9, member.getCurrentLoans());
            stmt.setDate(10, Date.valueOf(member.getMemberSince()));
            stmt.setBoolean(11, member.isActive());
            stmt.setInt(12, member.getId());
            
            stmt.executeUpdate();
            
            int index = membersCache.indexOf(member);
            if (index >= 0) {
                membersCache.set(index, member);
            }
            exportToSQLFile(); // Auto-export after data change
        } catch (SQLException e) {
            System.err.println("Failed to update member: " + e.getMessage());
        }
    }
    
    public void deleteMember(Member member) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, member.getId());
            stmt.executeUpdate();
            membersCache.remove(member);
            exportToSQLFile(); // Auto-export after data change
        } catch (SQLException e) {
            System.err.println("Failed to delete member: " + e.getMessage());
        }
    }
    
    public Member findMemberById(int id) {
        return membersCache.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }
    
    public List<Member> searchMembers(String searchTerm) {
        return membersCache.stream()
            .filter(m -> m.getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        m.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        String.valueOf(m.getId()).contains(searchTerm))
            .collect(Collectors.toList());
    }
    
    // Staff operations
    private void loadStaff() {
        staffCache.clear();
        String sql = "SELECT * FROM staff ORDER BY last_name, first_name";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("position"),
                    rs.getString("department"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                staff.setHireDate(rs.getDate("hire_date").toLocalDate());
                staff.setSalary(rs.getDouble("salary"));
                staff.setStatus(rs.getString("status"));
                // staff.setActive(rs.getBoolean("active")); // Staff has no setActive method
                
                staffCache.add(staff);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load staff: " + e.getMessage());
        }
    }
    
    public ObservableList<Staff> getAllStaff() { 
        return staffCache; 
    }
    
    public void addStaff(Staff staffMember) {
        String sql = "INSERT INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, username, role, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, staffMember.getFirstName());
            stmt.setString(2, staffMember.getLastName());
            stmt.setString(3, staffMember.getEmail());
            stmt.setString(4, staffMember.getPhone());
            stmt.setString(5, staffMember.getPosition());
            stmt.setString(6, staffMember.getDepartment());
            stmt.setDate(7, Date.valueOf(staffMember.getHireDate()));
            stmt.setDouble(8, staffMember.getSalary());
            stmt.setString(9, staffMember.getStatus());
            stmt.setString(10, staffMember.getUsername());
            stmt.setString(11, staffMember.getRole());
            stmt.setBoolean(12, staffMember.isActive());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    staffMember.setId(generatedKeys.getInt(1));
                    staffCache.add(staffMember);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add staff member: " + e.getMessage());
        }
    }
    
    public void updateStaff(Staff staffMember) {
        String sql = "UPDATE staff SET first_name = ?, last_name = ?, email = ?, phone = ?, position = ?, department = ?, hire_date = ?, salary = ?, status = ?, username = ?, role = ?, active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, staffMember.getFirstName());
            stmt.setString(2, staffMember.getLastName());
            stmt.setString(3, staffMember.getEmail());
            stmt.setString(4, staffMember.getPhone());
            stmt.setString(5, staffMember.getPosition());
            stmt.setString(6, staffMember.getDepartment());
            stmt.setDate(7, Date.valueOf(staffMember.getHireDate()));
            stmt.setDouble(8, staffMember.getSalary());
            stmt.setString(9, staffMember.getStatus());
            stmt.setString(10, staffMember.getUsername());
            stmt.setString(11, staffMember.getRole());
            stmt.setBoolean(12, staffMember.isActive());
            stmt.setInt(13, staffMember.getId());
            
            stmt.executeUpdate();
            
            int index = staffCache.indexOf(staffMember);
            if (index >= 0) {
                staffCache.set(index, staffMember);
            }
        } catch (SQLException e) {
            System.err.println("Failed to update staff member: " + e.getMessage());
        }
    }
    
    public void deleteStaff(Staff staffMember) {
        String sql = "DELETE FROM staff WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, staffMember.getId());
            stmt.executeUpdate();
            staffCache.remove(staffMember);
        } catch (SQLException e) {
            System.err.println("Failed to delete staff member: " + e.getMessage());
        }
    }
    
    // Loan operations
    private void loadLoans() {
        loansCache.clear();
        String sql = """
            SELECT l.*, 
                   m.first_name as member_first, m.last_name as member_last, m.email as member_email,
                   m.phone as member_phone, m.address as member_address, m.birth_date as member_birth,
                   m.status as member_status, m.max_loans, m.current_loans, m.member_since, m.active as member_active,
                   media.title, media.isbn, media.publish_year, media.publisher, media.type, media.total_copies,
                   media.available_copies, media.location, media.description as media_description, media.language,
                   media.author_id, media.category_id,
                   a.first_name as author_first, a.last_name as author_last, a.biography, a.nationality,
                   c.name as category_name, c.description as category_description, c.loan_duration_days
            FROM loans l
            JOIN members m ON l.member_id = m.id
            JOIN media ON l.media_id = media.id
            LEFT JOIN authors a ON media.author_id = a.id
            JOIN categories c ON media.category_id = c.id
            ORDER BY l.loan_date DESC
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                // Create Member object
                Member member = new Member(
                    rs.getInt("member_id"),
                    rs.getString("member_first"),
                    rs.getString("member_last"),
                    rs.getString("member_email"),
                    rs.getString("member_phone"),
                    rs.getString("member_address"),
                    rs.getDate("member_birth").toLocalDate()
                );
                member.setStatus(rs.getString("member_status"));
                member.setMaxLoans(rs.getInt("max_loans"));
                member.setCurrentLoans(rs.getInt("current_loans"));
                member.setMemberSince(rs.getDate("member_since").toLocalDate());
                // member.setActive(rs.getBoolean("member_active")); // Member has no setActive method
                
                // Create Author object (if exists)
                Author author = null;
                if (rs.getString("author_first") != null) {
                    author = new Author(
                        rs.getInt("author_id"),
                        rs.getString("author_first"),
                        rs.getString("author_last"),
                        rs.getString("biography"),
                        rs.getString("nationality")
                    );
                }
                
                // Create Category object
                Category category = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("category_description"),
                    rs.getInt("loan_duration_days")
                );
                
                // Create Media object
                Media media = new Media(
                    rs.getInt("media_id"),
                    rs.getString("title"),
                    rs.getString("isbn"),
                    rs.getInt("publish_year"),
                    rs.getString("publisher"),
                    rs.getString("type"),
                    rs.getInt("total_copies"),
                    rs.getString("location"),
                    author,
                    category
                );
                media.setAvailableCopies(rs.getInt("available_copies"));
                media.setDescription(rs.getString("media_description"));
                media.setLanguage(rs.getString("language"));
                
                // Create Loan object
                Loan loan = new Loan(
                    rs.getInt("id"),
                    member,
                    media,
                    rs.getDate("loan_date").toLocalDate()
                );
                loan.setDueDate(rs.getDate("due_date").toLocalDate());
                if (rs.getDate("return_date") != null) {
                    loan.setReturnDate(rs.getDate("return_date").toLocalDate());
                }
                loan.setStatus(rs.getString("status"));
                loan.setRenewalCount(rs.getInt("renewal_count"));
                loan.setMaxRenewals(rs.getInt("max_renewals"));
                loan.setNotes(rs.getString("notes"));
                
                loansCache.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load loans: " + e.getMessage());
        }
    }
    
    public ObservableList<Loan> getAllLoans() { 
        return loansCache; 
    }
    
    public void addLoan(Loan loan) {
        String sql = "INSERT INTO loans (member_id, media_id, loan_date, due_date, return_date, status, renewal_count, max_renewals, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, loan.getMember().getId());
            stmt.setInt(2, loan.getMedia().getId());
            stmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            stmt.setDate(4, Date.valueOf(loan.getDueDate()));
            stmt.setDate(5, loan.getReturnDate() != null ? Date.valueOf(loan.getReturnDate()) : null);
            stmt.setString(6, loan.getStatus());
            stmt.setInt(7, loan.getRenewalCount());
            stmt.setInt(8, loan.getMaxRenewals());
            stmt.setString(9, loan.getNotes());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getInt(1));
                    loansCache.add(loan);
                    
                    // Update member current loans
                    loan.getMember().setCurrentLoans(loan.getMember().getCurrentLoans() + 1);
                    updateMember(loan.getMember());
                    
                    // Update media available copies
                    loan.getMedia().borrowCopy();
                    updateMedia(loan.getMedia());
                    exportToSQLFile();
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add loan: " + e.getMessage());
        }
    }
    
    public void updateLoan(Loan loan) {
        String sql = "UPDATE loans SET member_id = ?, media_id = ?, loan_date = ?, due_date = ?, return_date = ?, status = ?, renewal_count = ?, max_renewals = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, loan.getMember().getId());
            stmt.setInt(2, loan.getMedia().getId());
            stmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            stmt.setDate(4, Date.valueOf(loan.getDueDate()));
            stmt.setDate(5, loan.getReturnDate() != null ? Date.valueOf(loan.getReturnDate()) : null);
            stmt.setString(6, loan.getStatus());
            stmt.setInt(7, loan.getRenewalCount());
            stmt.setInt(8, loan.getMaxRenewals());
            stmt.setString(9, loan.getNotes());
            stmt.setInt(10, loan.getId());
            
            stmt.executeUpdate();
            
            int index = loansCache.indexOf(loan);
            if (index >= 0) {
                loansCache.set(index, loan);
            }
            exportToSQLFile();
        } catch (SQLException e) {
            System.err.println("Failed to update loan: " + e.getMessage());
        }
    }
    
    public void returnLoan(Loan loan) {
        loan.returnMedia();
        loan.getMember().setCurrentLoans(loan.getMember().getCurrentLoans() - 1);
        loan.getMedia().returnCopy();
        
        updateLoan(loan);
        updateMember(loan.getMember());
        updateMedia(loan.getMedia());
    }
    
    public List<Loan> getActiveLoans() {
        return loansCache.stream().filter(l -> "Active".equals(l.getStatus())).collect(Collectors.toList());
    }
    
    public List<Loan> getOverdueLoans() {
        return loansCache.stream().filter(Loan::isOverdue).collect(Collectors.toList());
    }
    
    public List<Loan> getMemberLoans(Member member) {
        return loansCache.stream().filter(l -> l.getMember().equals(member)).collect(Collectors.toList());
    }
    
    // Fine operations
    private void loadFines() {
        finesCache.clear();
        String sql = """
            SELECT f.*, 
                   m.first_name as member_first, m.last_name as member_last, m.email as member_email,
                   m.phone as member_phone, m.address as member_address, m.birth_date as member_birth,
                   m.status as member_status, m.max_loans, m.current_loans, m.member_since, m.active as member_active
            FROM fines f
            JOIN members m ON f.member_id = m.id
            ORDER BY f.issue_date DESC
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                // Create Member object
                Member member = new Member(
                    rs.getInt("member_id"),
                    rs.getString("member_first"),
                    rs.getString("member_last"),
                    rs.getString("member_email"),
                    rs.getString("member_phone"),
                    rs.getString("member_address"),
                    rs.getDate("member_birth").toLocalDate()
                );
                member.setStatus(rs.getString("member_status"));
                member.setMaxLoans(rs.getInt("max_loans"));
                member.setCurrentLoans(rs.getInt("current_loans"));
                member.setMemberSince(rs.getDate("member_since").toLocalDate());
                // member.setActive(rs.getBoolean("member_active")); // Member has no setActive method
                
                // Find associated loan if exists
                Loan loan = null;
                if (rs.getObject("loan_id") != null) {
                    int loanId = rs.getInt("loan_id");
                    loan = loansCache.stream().filter(l -> l.getId() == loanId).findFirst().orElse(null);
                }
                
                // Create Fine object
                Fine fine = new Fine(
                    rs.getInt("id"),
                    member,
                    loan,
                    rs.getDouble("amount"),
                    rs.getString("reason")
                );
                fine.setIssueDate(rs.getDate("issue_date").toLocalDate());
                if (rs.getDate("paid_date") != null) {
                    fine.setPaidDate(rs.getDate("paid_date").toLocalDate());
                }
                fine.setStatus(rs.getString("status"));
                fine.setDescription(rs.getString("description"));
                
                finesCache.add(fine);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load fines: " + e.getMessage());
        }
    }
    
    public ObservableList<Fine> getAllFines() { 
        return finesCache; 
    }
    
    public void addFine(Fine fine) {
        String sql = "INSERT INTO fines (member_id, loan_id, amount, reason, issue_date, paid_date, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, fine.getMember().getId());
            stmt.setObject(2, fine.getLoan() != null ? fine.getLoan().getId() : null);
            stmt.setDouble(3, fine.getAmount());
            stmt.setString(4, fine.getReason());
            stmt.setDate(5, Date.valueOf(fine.getIssueDate()));
            stmt.setDate(6, fine.getPaidDate() != null ? Date.valueOf(fine.getPaidDate()) : null);
            stmt.setString(7, fine.getStatus());
            stmt.setString(8, fine.getDescription());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fine.setId(generatedKeys.getInt(1));
                    finesCache.add(fine);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to add fine: " + e.getMessage());
        }
    }
    
    public void updateFine(Fine fine) {
        String sql = "UPDATE fines SET member_id = ?, loan_id = ?, amount = ?, reason = ?, issue_date = ?, paid_date = ?, status = ?, description = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, fine.getMember().getId());
            stmt.setObject(2, fine.getLoan() != null ? fine.getLoan().getId() : null);
            stmt.setDouble(3, fine.getAmount());
            stmt.setString(4, fine.getReason());
            stmt.setDate(5, Date.valueOf(fine.getIssueDate()));
            stmt.setDate(6, fine.getPaidDate() != null ? Date.valueOf(fine.getPaidDate()) : null);
            stmt.setString(7, fine.getStatus());
            stmt.setString(8, fine.getDescription());
            stmt.setInt(9, fine.getId());
            
            stmt.executeUpdate();
            
            int index = finesCache.indexOf(fine);
            if (index >= 0) {
                finesCache.set(index, fine);
            }
        } catch (SQLException e) {
            System.err.println("Failed to update fine: " + e.getMessage());
        }
    }
    
    public void deleteFine(Fine fine) {
        String sql = "DELETE FROM fines WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, fine.getId());
            stmt.executeUpdate();
            finesCache.remove(fine);
        } catch (SQLException e) {
            System.err.println("Failed to delete fine: " + e.getMessage());
        }
    }
    
    public List<Fine> getOutstandingFines() {
        return finesCache.stream().filter(Fine::isOutstanding).collect(Collectors.toList());
    }
    
    public List<Fine> getMemberFines(Member member) {
        return finesCache.stream().filter(f -> f.getMember().equals(member)).collect(Collectors.toList());
    }
    
    public double getTotalOutstandingFines() {
        return finesCache.stream()
            .filter(Fine::isOutstanding)
            .mapToDouble(Fine::getAmount)
            .sum();
    }
    
    // Statistics methods
    public int getTotalMembers() { return membersCache.size(); }
    public int getActiveMembers() { return (int) membersCache.stream().filter(Member::isActive).count(); }
    public int getTotalMedia() { return mediaCache.size(); }
    public int getAvailableMediaCount() { return (int) mediaCache.stream().filter(Media::isAvailable).count(); }
    public int getTotalActiveLoans() { return getActiveLoans().size(); }
    public int getTotalOverdueLoans() { return getOverdueLoans().size(); }
    public int getTotalOutstandingFinesCount() { return getOutstandingFines().size(); }
    
    // Auto-fine generation for overdue loans
    public void generateOverdueFines() {
        for (Loan loan : getOverdueLoans()) {
            boolean fineExists = finesCache.stream()
                .anyMatch(f -> f.getLoan() != null && f.getLoan().equals(loan) && f.isOutstanding());
            
            if (!fineExists) {
                Fine fine = new Fine(0, loan.getMember(), loan, 
                    loan.calculateFine(), "Overdue return - " + loan.getDaysOverdue() + " days late");
                addFine(fine);
            }
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
}