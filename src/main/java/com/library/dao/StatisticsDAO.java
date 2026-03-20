package com.library.dao;

import com.library.exception.DatabaseException;
import com.library.model.Book;
import java.util.List;
import java.util.Map;

public interface StatisticsDAO {
    List<BookPopularity> getPopularBooks(int limit) throws DatabaseException;
    List<Book> getCurrentlyLoanedBooks() throws DatabaseException;

    // Вспомогательный класс для статистики
    class BookPopularity {
        private final Book book;
        private final int loanCount;

        public BookPopularity(Book book, int loanCount) {
            this.book = book;
            this.loanCount = loanCount;
        }

        public Book getBook() { return book; }
        public int getLoanCount() { return loanCount; }

        @Override
        public String toString() {
            return book.getTitle() + " (автор: " + book.getAuthor() + ") — выдавалась " + loanCount + " раз(а)";
        }
    }
}