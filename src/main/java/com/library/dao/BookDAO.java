package com.library.dao;

import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.util.List;

public interface BookDAO {
    void addBook(Book book) throws DatabaseException;
    List<Book> getAllBooks() throws DatabaseException;
    List<Book> findBooksByTitle(String title) throws DatabaseException;
    Book getBookById(int id) throws DatabaseException;
    void updateBookAvailability(int bookId, boolean available) throws DatabaseException;
}