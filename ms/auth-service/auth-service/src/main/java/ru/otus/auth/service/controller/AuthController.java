package ru.otus.auth.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.auth.service.model.dto.auth.AuthRefreshRequestDto;
import ru.otus.auth.service.model.dto.auth.AuthRequestDto;
import ru.otus.auth.service.model.dto.auth.AuthResponseDto;
import ru.otus.auth.service.service.AuthService;

import static ru.otus.auth.service.controller.AuthController.BASE_PATH;

@Slf4j
@Validated
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
public class AuthController {

    public final static String BASE_PATH = "/auth/login";

    private final AuthService service;

    @PostMapping("/pwd")
    public AuthResponseDto authenticate(@Valid @RequestBody AuthRequestDto dto) {
        log.debug("Trying to authenticate user with login: {}", dto.getLogin());
        return service.authenticate(dto);
    }

    @PostMapping("/token")
    public AuthResponseDto refreshToken(@Valid @RequestBody AuthRefreshRequestDto dto) {
        log.debug("Trying to refresh token: {}", dto.getRefreshToken());
        return service.refreshToken(dto);
    }
}
