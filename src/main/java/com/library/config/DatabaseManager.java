package com.library.config;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());



// Явная загрузка драйвера SQLite
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            logger.info("SQLite JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC driver not found in classpath");
            throw new RuntimeException("SQLite driver missing", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createBooks = """
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT,
                    year INTEGER,
                    isbn TEXT UNIQUE,
                    available BOOLEAN DEFAULT 1
                )""";
        String createReaders = """
                CREATE TABLE IF NOT EXISTS readers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    phone TEXT
                )""";
        String createLoans = """
                CREATE TABLE IF NOT EXISTS loans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    reader_id INTEGER NOT NULL,
                    loan_date DATE NOT NULL,
                    return_date DATE,
                    FOREIGN KEY (book_id) REFERENCES books(id),
                    FOREIGN KEY (reader_id) REFERENCES readers(id)
                )""";
        String createIndexes = """
                CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
                CREATE INDEX IF NOT EXISTS idx_books_available ON books(available);
                CREATE INDEX IF NOT EXISTS idx_readers_name ON readers(name);
                CREATE INDEX IF NOT EXISTS idx_loans_book_return ON loans(book_id, return_date);
                CREATE INDEX IF NOT EXISTS idx_loans_reader ON loans(reader_id);
                """;
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createBooks);
            stmt.execute(createReaders);
            stmt.execute(createLoans);
            for (String sql : createIndexes.split(";")) {
                if (!sql.trim().isEmpty()) stmt.execute(sql);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка инициализации БД", e);
            throw new RuntimeException("Не удалось создать таблицы", e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Ошибка закрытия соединения", e);
            }
        }
    }
}