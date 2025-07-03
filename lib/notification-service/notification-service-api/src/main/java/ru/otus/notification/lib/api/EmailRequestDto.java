package ru.otus.notification.lib.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {
    private String to;
    private String subject;
    private String intro;
    private String message;
}
