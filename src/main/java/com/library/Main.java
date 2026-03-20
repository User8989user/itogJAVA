package com.library;

import com.library.config.DatabaseManager;
import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import com.library.model.Reader;
import com.library.util.InputHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static BookDAO bookDAO;
    private static ReaderDAO readerDAO;
    private static LoanDAO loanDAO;
    private static StatisticsDAO statisticsDAO;

    public static void main(String[] args) {
        try {
            // Инициализация БД
            DatabaseManager.initializeDatabase();
            Connection connection = DatabaseManager.getConnection();

            // Инициализация DAO
            bookDAO = new BookDAOImpl(connection);
            readerDAO = new ReaderDAOImpl(connection);
            loanDAO = new LoanDAOImpl(connection, bookDAO);
            statisticsDAO = new StatisticsDAOImpl(connection);

            // Запуск меню
            runMenu();

        } catch (SQLException e) {
            System.err.println("Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeConnection();
            InputHelper.close();
        }
    }

    private static void runMenu() {
        while (true) {
            printMainMenu();
            int choice = InputHelper.readInt("Выберите пункт: ");
            try {
                switch (choice) {
                    case 1 -> manageBooks();
                    case 2 -> manageReaders();
                    case 3 -> manageLoans();
                    case 4 -> showStatistics();
                    case 5 -> {
                        System.out.println("До свидания!");
                        return;
                    }
                    default -> System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (DatabaseException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n=== БИБЛИОТЕЧНАЯ СИСТЕМА ===");
        System.out.println("1. Работа с книгами");
        System.out.println("2. Работа с читателями");
        System.out.println("3. Операции выдачи");
        System.out.println("4. Статистика");
        System.out.println("5. Выход");
    }

    // ---------- Книги ----------
    private static void manageBooks() throws DatabaseException {
        while (true) {
            System.out.println("\n--- КНИГИ ---");
            System.out.println("1. Добавить книгу");
            System.out.println("2. Показать все книги");
            System.out.println("3. Найти книгу по названию");
            System.out.println("4. Назад");
            int choice = InputHelper.readInt("Выбор: ");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> showAllBooks();
                case 3 -> findBookByTitle();
                case 4 -> { return; }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void addBook() throws DatabaseException {
        System.out.println("\n--- Добавление книги ---");
        String title = InputHelper.readString("Название: ");
        String author = InputHelper.readString("Автор: ");
        int year = InputHelper.readInt("Год издания: ");
        String isbn = InputHelper.readString("ISBN: ");
        boolean available = InputHelper.readBoolean("Доступна ли книга сразу?");

        Book book = new Book(0, title, author, year, isbn, available);
        bookDAO.addBook(book);
        System.out.println("Книга успешно добавлена!");
    }

    private static void showAllBooks() throws DatabaseException {
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("Список книг пуст.");
        } else {
            System.out.println("\n=== СПИСОК ВСЕХ КНИГ ===");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }

    private static void findBookByTitle() throws DatabaseException {
        String title = InputHelper.readString("Введите название (или часть): ");
        List<Book> books = bookDAO.findBooksByTitle(title);
        if (books.isEmpty()) {
            System.out.println("Книги не найдены.");
        } else {
            System.out.println("\n=== РЕЗУЛЬТАТЫ ПОИСКА ===");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }

    // ---------- Читатели ----------
    private static void manageReaders() throws DatabaseException {
        while (true) {
            System.out.println("\n--- ЧИТАТЕЛИ ---");
            System.out.println("1. Зарегистрировать читателя");
            System.out.println("2. Показать всех читателей");
            System.out.println("3. Назад");
            int choice = InputHelper.readInt("Выбор: ");
            switch (choice) {
                case 1 -> addReader();
                case 2 -> showAllReaders();
                case 3 -> { return; }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void addReader() throws DatabaseException {
        System.out.println("\n--- Регистрация читателя ---");
        String name = InputHelper.readString("ФИО: ");
        String email = InputHelper.readString("Email: ");
        String phone = InputHelper.readString("Телефон: ");

        Reader reader = new Reader(0, name, email, phone);
        readerDAO.addReader(reader);
        System.out.println("Читатель успешно зарегистрирован!");
    }

    private static void showAllReaders() throws DatabaseException {
        List<Reader> readers = readerDAO.getAllReaders();
        if (readers.isEmpty()) {
            System.out.println("Список читателей пуст.");
        } else {
            System.out.println("\n=== СПИСОК ВСЕХ ЧИТАТЕЛЕЙ ===");
            for (Reader r : readers) {
                System.out.println(r);
            }
        }
    }

    // ---------- Операции выдачи ----------
    private static void manageLoans() throws DatabaseException {
        while (true) {
            System.out.println("\n--- ОПЕРАЦИИ ВЫДАЧИ ---");
            System.out.println("1. Выдать книгу читателю");
            System.out.println("2. Вернуть книгу");
            System.out.println("3. Показать книги, выданные читателю");
            System.out.println("4. Назад");
            int choice = InputHelper.readInt("Выбор: ");
            switch (choice) {
                case 1 -> lendBook();
                case 2 -> returnBook();
                case 3 -> showBooksLoanedToReader();
                case 4 -> { return; }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void lendBook() throws DatabaseException {
        System.out.println("\n--- Выдача книги ---");

        // Выбор читателя
        List<Reader> readers = readerDAO.getAllReaders();
        if (readers.isEmpty()) {
            System.out.println("Нет зарегистрированных читателей. Сначала зарегистрируйте читателя.");
            return;
        }
        System.out.println("Список читателей:");
        for (Reader r : readers) {
            System.out.println(r);
        }
        int readerId = InputHelper.readInt("Введите ID читателя: ");
        Reader reader = readerDAO.getReaderById(readerId);
        if (reader == null) {
            System.out.println("Читатель с таким ID не найден.");
            return;
        }

        // Выбор книги
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("Нет книг в библиотеке. Сначала добавьте книгу.");
            return;
        }
        System.out.println("Доступные книги:");
        for (Book b : books) {
            if (b.isAvailable()) {
                System.out.println(b);
            }
        }
        int bookId = InputHelper.readInt("Введите ID книги для выдачи: ");
        Book book = bookDAO.getBookById(bookId);
        if (book == null || !book.isAvailable()) {
            System.out.println("Книга недоступна для выдачи.");
            return;
        }

        loanDAO.lendBook(bookId, readerId, LocalDate.now());
        System.out.println("Книга '" + book.getTitle() + "' выдана читателю " + reader.getName());
    }

    private static void returnBook() throws DatabaseException {
        System.out.println("\n--- Возврат книги ---");

        int readerId = InputHelper.readInt("Введите ID читателя, возвращающего книгу: ");
        Reader reader = readerDAO.getReaderById(readerId);
        if (reader == null) {
            System.out.println("Читатель не найден.");
            return;
        }

        List<Book> loanedBooks = loanDAO.getBooksLoanedToReader(readerId);
        if (loanedBooks.isEmpty()) {
            System.out.println("У этого читателя нет выданных книг.");
            return;
        }

        System.out.println("Книги, выданные читателю " + reader.getName() + ":");
        for (Book b : loanedBooks) {
            System.out.println(b);
        }

        int bookId = InputHelper.readInt("Введите ID книги для возврата: ");
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            System.out.println("Книга не найдена.");
            return;
        }

        int loanId = loanDAO.findActiveLoanId(bookId, readerId);
        if (loanId == -1) {
            System.out.println("Эта книга не выдана данному читателю или уже возвращена.");
            return;
        }

        loanDAO.returnBook(loanId, LocalDate.now());
        System.out.println("Книга '" + book.getTitle() + "' возвращена.");
    }

    private static void showBooksLoanedToReader() throws DatabaseException {
        int readerId = InputHelper.readInt("Введите ID читателя: ");
        Reader reader = readerDAO.getReaderById(readerId);
        if (reader == null) {
            System.out.println("Читатель не найден.");
            return;
        }

        List<Book> books = loanDAO.getBooksLoanedToReader(readerId);
        if (books.isEmpty()) {
            System.out.println("У читателя " + reader.getName() + " нет выданных книг.");
        } else {
            System.out.println("\n=== КНИГИ, ВЫДАННЫЕ " + reader.getName().toUpperCase() + " ===");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }

    // ---------- Статистика ----------
    private static void showStatistics() throws DatabaseException {
        while (true) {
            System.out.println("\n--- СТАТИСТИКА ---");
            System.out.println("1. Популярные книги");
            System.out.println("2. Список выданных книг");
            System.out.println("3. Назад");
            int choice = InputHelper.readInt("Выбор: ");
            switch (choice) {
                case 1 -> showPopularBooks();
                case 2 -> showCurrentlyLoanedBooks();
                case 3 -> { return; }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void showPopularBooks() throws DatabaseException {
        int limit = InputHelper.readInt("Сколько книг показать? (например, 5): ");
        List<StatisticsDAO.BookPopularity> popular = statisticsDAO.getPopularBooks(limit);
        if (popular.isEmpty()) {
            System.out.println("Нет данных о выдачах.");
        } else {
            System.out.println("\n=== ТОП ПОПУЛЯРНЫХ КНИГ ===");
            for (int i = 0; i < popular.size(); i++) {
                System.out.println((i+1) + ". " + popular.get(i));
            }
        }
    }

    private static void showCurrentlyLoanedBooks() throws DatabaseException {
        List<Book> loaned = statisticsDAO.getCurrentlyLoanedBooks();
        if (loaned.isEmpty()) {
            System.out.println("В данный момент все книги в библиотеке.");
        } else {
            System.out.println("\n=== СПИСОК ВЫДАННЫХ КНИГ ===");
            for (Book b : loaned) {
                System.out.println(b);
            }
        }
    }
}