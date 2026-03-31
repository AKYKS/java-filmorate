package ru.yandex.practicum.filmorate.expection;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
