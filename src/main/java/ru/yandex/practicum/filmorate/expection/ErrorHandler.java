package ru.yandex.practicum.filmorate.expection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> ConditionsNotMet(final ConditionsNotMetException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Отсутствует значение",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> DuplicatedData(final DuplicatedDataException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Дублирование значения",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> NotFound(final NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Значение не найдено",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> Validation(final ValidationException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Ошибка валидации",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
