package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAOImpl implements BookDAO {
    private final Connection connection;

    public BookDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addBook(Book book) throws DatabaseException {
        String sql = "INSERT INTO books (title, author, year, isbn, available) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getYear());
            pstmt.setString(4, book.getIsbn());
            pstmt.setBoolean(5, book.isAvailable());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка добавления книги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> getAllBooks() throws DatabaseException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, year, isbn, available FROM books ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения списка книг", e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws DatabaseException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, year, isbn, available FROM books WHERE title LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка поиска книг", e);
        }
        return books;
    }

    @Override
    public Book getBookById(int id) throws DatabaseException {
        String sql = "SELECT id, title, author, year, isbn, available FROM books WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapBook(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения книги по ID", e);
        }
    }

    @Override
    public void updateBookAvailability(int bookId, boolean available) throws DatabaseException {
        String sql = "UPDATE books SET available = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, available);
            pstmt.setInt(2, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка обновления статуса книги", e);
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("year"),
                rs.getString("isbn"),
                rs.getBoolean("available")
        );
    }
}