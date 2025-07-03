package ru.otus.auth.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.auth.service.model.dto.reg.RegRequestDto;
import ru.otus.auth.service.model.dto.reg.RegResponseDto;
import ru.otus.auth.service.service.RegService;

@Slf4j
@Validated
@RestController
@RequestMapping(RegController.BASE_PATH)
@RequiredArgsConstructor
public class RegController {

    public final static String BASE_PATH = "/api/v1/register";

    private final RegService service;

    @PostMapping
    public RegResponseDto register(@Valid @RequestBody RegRequestDto dto) {
        log.debug("Trying to register user with email: {}", dto.getEmail());
        return service.register(dto);
    }
}
