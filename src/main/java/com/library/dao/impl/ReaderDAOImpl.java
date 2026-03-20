package com.library.dao.impl;

import com.library.dao.ReaderDAO;
import com.library.exception.DatabaseException;
import com.library.model.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAOImpl implements ReaderDAO {
    private final Connection connection;

    public ReaderDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addReader(Reader reader) throws DatabaseException {
        String sql = "INSERT INTO readers (name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reader.getName());
            pstmt.setString(2, reader.getEmail());
            pstmt.setString(3, reader.getPhone());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка регистрации читателя: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reader> getAllReaders() throws DatabaseException {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT id, name, email, phone FROM readers ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                readers.add(mapReader(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения списка читателей", e);
        }
        return readers;
    }

    @Override
    public Reader getReaderById(int id) throws DatabaseException {
        String sql = "SELECT id, name, email, phone FROM readers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapReader(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения читателя по ID", e);
        }
    }

    private Reader mapReader(ResultSet rs) throws SQLException {
        return new Reader(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}