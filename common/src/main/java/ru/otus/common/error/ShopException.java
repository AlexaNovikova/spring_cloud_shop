package ru.otus.common.error;

import lombok.Data;

@Data
public class ShopException extends RuntimeException {
    private String code;
    private String httpStatus;

    public ShopException(String code, String message, String status) {
        super(message);
        this.code = code;
        this.httpStatus = status;
    }

    public ShopException(String code, String message) {
        super(message);
        this.code = code;
    }
}
