-- Library Management System Data Export
-- Generated: 2025-09-13T10:10:01.195406100

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


-- Data for table authors
DELETE FROM authors;
INSERT INTO authors VALUES (1, 'Harper', 'Lee', 'American novelist known for To Kill a Mockingbird', 'American');
INSERT INTO authors VALUES (2, 'George', 'Orwell', 'English author and journalist', 'British');
INSERT INTO authors VALUES (3, 'Jane', 'Austen', 'English novelist known for Pride and Prejudice', 'British');

-- Data for table categories
DELETE FROM categories;
INSERT INTO categories VALUES (1, 'Fiction', 'Literary fiction and novels', 14);
INSERT INTO categories VALUES (2, 'Non-Fiction', 'Educational and informational books', 14);
INSERT INTO categories VALUES (3, 'Science', 'Scientific literature and research', 21);
INSERT INTO categories VALUES (4, 'Children', 'Books for children and young adults', 14);

-- Data for table media
DELETE FROM media;
INSERT INTO media VALUES (1, 'To Kill a Mockingbird', '978-0-06-112008-4', 1960, 'J.B. Lippincott & Co.', 'Book', 3, 2, 'Fiction A-L', 1, 1, 'Classic American literature', 'English', '2025-09-13 09:16:04.761672', '2025-09-13 09:16:04.761672');
INSERT INTO media VALUES (2, '1984', '978-0-452-28423-4', 1949, 'Secker & Warburg', 'Book', 5, 4, 'Fiction M-Z', 2, 1, 'Dystopian social science fiction', 'English', '2025-09-13 09:16:04.761672', '2025-09-13 09:16:04.761672');
INSERT INTO media VALUES (3, 'Pride and Prejudice', '978-0-14-143951-8', 1813, 'T. Egerton', 'Book', 2, 1, 'Fiction A-L', 3, 1, 'Romantic fiction', 'English', '2025-09-13 09:16:04.761672', '2025-09-13 09:16:04.761672');

-- Data for table members
DELETE FROM members;
INSERT INTO members VALUES (1, 'John', 'Doe', 'john.doe@email.com', '+41 79 123 45 67', 'Musterstrasse 1, 8000 Zürich', '1985-03-15', 'Active', 5, 2, '2025-09-13', true, '2025-09-13 09:16:04.766516', '2025-09-13 09:55:09.913384');
INSERT INTO members VALUES (2, 'Jane', 'Smith', 'jane.smith@email.com', '+41 79 234 56 78', 'Beispielweg 12, 3000 Bern', '1990-07-22', 'Active', 5, 1, '2025-09-13', true, '2025-09-13 09:16:04.766516', '2025-09-13 09:16:04.766516');

-- Data for table staff
DELETE FROM staff;

-- Data for table loans
DELETE FROM loans;
INSERT INTO loans VALUES (1, 1, 3, '2025-09-13', '2025-09-27', NULL, 'Active', 0, 2, '', '2025-09-13 09:55:09.907064', '2025-09-13 09:55:09.907064');
INSERT INTO loans VALUES (2, 3, 4, '2025-09-13', '2025-09-27', '2025-09-13', 'Returned', 0, 2, '', '2025-09-13 10:01:04.710954', '2025-09-13 10:07:13.968904');

-- Data for table fines
DELETE FROM fines;
