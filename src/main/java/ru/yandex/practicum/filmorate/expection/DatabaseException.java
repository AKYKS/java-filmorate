package ru.yandex.practicum.filmorate.expection;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DatabaseException extends DataAccessException {
    public DatabaseException(String message) {
        super(message);
    }
}
