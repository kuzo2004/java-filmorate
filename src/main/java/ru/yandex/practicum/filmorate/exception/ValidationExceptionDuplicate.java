package ru.yandex.practicum.filmorate.exception;

public class ValidationExceptionDuplicate extends RuntimeException {
    public ValidationExceptionDuplicate(String message) {
        super(message);
    }
}
