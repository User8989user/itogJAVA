package com.library.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private int year;
    private String isbn;
    private boolean available;

    public Book() {}

    public Book(int id, String title, String author, int year, String isbn, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
        this.available = available;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | %d | ISBN: %s | %s",
                id, title, author, year, isbn, available ? "Доступна" : "Выдана");
    }
}