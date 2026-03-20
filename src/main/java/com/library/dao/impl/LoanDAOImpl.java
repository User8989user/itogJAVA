package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {
    private final Connection connection;
    private final BookDAO bookDAO;

    public LoanDAOImpl(Connection connection, BookDAO bookDAO) {
        this.connection = connection;
        this.bookDAO = bookDAO;
    }

    @Override
    public void lendBook(int bookId, int readerId, LocalDate loanDate) throws DatabaseException {
        // Проверка, что книга доступна
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new DatabaseException("Книга с ID " + bookId + " не найдена.");
        }
        if (!book.isAvailable()) {
            throw new DatabaseException("Книга '" + book.getTitle() + "' уже выдана.");
        }

        String insertSql = "INSERT INTO loans (book_id, reader_id, loan_date) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                pstmt.setInt(1, bookId);
                pstmt.setInt(2, readerId);
                pstmt.setDate(3, Date.valueOf(loanDate));
                pstmt.executeUpdate();
            }

            bookDAO.updateBookAvailability(bookId, false);
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new DatabaseException("Ошибка при выдаче книги: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    @Override
    public void returnBook(int loanId, LocalDate returnDate) throws DatabaseException {
        String sql = "UPDATE loans SET return_date = ? WHERE id = ? AND return_date IS NULL";
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(returnDate));
                pstmt.setInt(2, loanId);
                int updated = pstmt.executeUpdate();
                if (updated == 0) {
                    throw new DatabaseException("Выдача с ID " + loanId + " не найдена или уже возвращена.");
                }
            }

            // Находим book_id по loanId
            int bookId = getBookIdByLoanId(loanId);
            if (bookId != -1) {
                bookDAO.updateBookAvailability(bookId, true);
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new DatabaseException("Ошибка при возврате книги: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    @Override
    public List<Book> getBooksLoanedToReader(int readerId) throws DatabaseException {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT b.id, b.title, b.author, b.year, b.isbn, b.available
            FROM loans l
            JOIN books b ON l.book_id = b.id
            WHERE l.reader_id = ? AND l.return_date IS NULL
            ORDER BY l.loan_date DESC
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, readerId);
            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения книг читателя", e);
        }
        return books;
    }

    @Override
    public int findActiveLoanId(int bookId, int readerId) throws DatabaseException {
        String sql = "SELECT id FROM loans WHERE book_id = ? AND reader_id = ? AND return_date IS NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, readerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка поиска активной выдачи", e);
        }
        return -1;
    }

    private int getBookIdByLoanId(int loanId) throws DatabaseException {
        String sql = "SELECT book_id FROM loans WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, loanId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("book_id");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения book_id по loanId", e);
        }
        return -1;
    }
}