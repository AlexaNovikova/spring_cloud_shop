package ru.otus.auth.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.otus.auth.service.model.dto.user.UpdateUserRequestDto;
import ru.otus.auth.service.model.dto.user.UserListResponseDto;
import ru.otus.auth.service.model.dto.user.UserResponseDto;
import ru.otus.auth.service.service.UserService;
import ru.otus.common.ShopUser;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping(UserController.BASE_PATH)
@RequiredArgsConstructor
public class UserController {

    public final static String BASE_PATH = "/api/v1/user";

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponseDto getById(@AuthenticationPrincipal ShopUser shopUser,
                                   @PathVariable UUID id) {
        log.debug("Trying to get user by id: {}", id);
        return userService.getById(id, shopUser);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(@AuthenticationPrincipal ShopUser shopUser,
                                  @PathVariable UUID id, @RequestBody UpdateUserRequestDto dto) {
        log.debug("Trying to update user by id: {}", id);
        return userService.update(id, dto, shopUser);
    }


    @GetMapping
    public UserListResponseDto getAll() {
        log.debug("Trying to get all users");
        return userService.getAll();
    }
}
