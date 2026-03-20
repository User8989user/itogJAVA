package com.library.dao;

import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.time.LocalDate;
import java.util.List;

public interface LoanDAO {
    void lendBook(int bookId, int readerId, LocalDate loanDate) throws DatabaseException;
    void returnBook(int loanId, LocalDate returnDate) throws DatabaseException;
    List<Book> getBooksLoanedToReader(int readerId) throws DatabaseException;
    // Для возврата книги по паре (книга, читатель) поиск активной выдачи 
    int findActiveLoanId(int bookId, int readerId) throws DatabaseException;
}