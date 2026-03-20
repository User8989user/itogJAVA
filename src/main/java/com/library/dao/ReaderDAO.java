package com.library.dao;

import com.library.exception.DatabaseException;
import com.library.model.Reader;
import java.util.List;

public interface ReaderDAO {
    void addReader(Reader reader) throws DatabaseException;
    List<Reader> getAllReaders() throws DatabaseException;
    Reader getReaderById(int id) throws DatabaseException;
}