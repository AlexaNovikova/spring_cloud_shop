package ru.otus.common.error;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ErrorDto {
    private String code;
    private String message;
    private Map<String, String> info;
}
