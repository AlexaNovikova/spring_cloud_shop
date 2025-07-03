package ru.otus.order.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdempotenceException extends RuntimeException {
    public IdempotenceException(String message) {
        super(message);
    }
}
