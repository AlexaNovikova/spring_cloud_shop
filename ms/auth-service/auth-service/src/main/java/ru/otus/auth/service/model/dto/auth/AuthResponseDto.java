package ru.otus.auth.service.model.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String refreshToken;
    private String token;
    private UserDto profile;
}