package com.library.dao.impl;

import com.library.dao.StatisticsDAO;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDAOImpl implements StatisticsDAO {
    private final Connection connection;

    public StatisticsDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<BookPopularity> getPopularBooks(int limit) throws DatabaseException {
        List<BookPopularity> popular = new ArrayList<>();
        String sql = """
            SELECT b.id, b.title, b.author, COUNT(l.id) as loan_count
            FROM books b
            LEFT JOIN loans l ON b.id = l.book_id
            GROUP BY b.id
            ORDER BY loan_count DESC
            LIMIT ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            0, null, false
                    );
                    int count = rs.getInt("loan_count");
                    popular.add(new BookPopularity(book, count));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения популярных книг", e);
        }
        return popular;
    }

    @Override
    public List<Book> getCurrentlyLoanedBooks() throws DatabaseException {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT DISTINCT b.id, b.title, b.author, b.year, b.isbn, b.available
            FROM books b
            JOIN loans l ON b.id = l.book_id
            WHERE l.return_date IS NULL
            ORDER BY b.title
        """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getString("isbn"),
                        rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения выданных книг", e);
        }
        return books;
    }
}