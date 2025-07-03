package ru.otus.auth.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.otus.auth.service.model.dto.auth.AuthRefreshRequestDto;
import ru.otus.common.ShopUser;
import ru.otus.auth.shared.service.JwtService;
import ru.otus.auth.service.model.dto.auth.AuthRequestDto;
import ru.otus.auth.shared.model.AuthContext;
import ru.otus.auth.service.mapper.UserMapper;
import ru.otus.auth.service.model.dto.auth.AuthResponseDto;
import ru.otus.common.error.ShopException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String UNPROCESSABLE_ENTITY = "unprocessable.entity";

    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthResponseDto authenticate(AuthRequestDto dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getLogin(),
                dto.getPassword())
        );

        var userDetails = userService.userDetailsService()
                .loadUserByUsername(dto.getLogin());
        var userCtx = mapper.toUserCtx((AuthContext) userDetails);

        var token = jwtService.generateToken(userCtx);
        var refreshToken = jwtService.generateRefreshToken(userCtx);
        var profile = mapper.toDto(userCtx);
        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .profile(profile)
                .build();
    }

    public AuthResponseDto refreshToken(AuthRefreshRequestDto dto) {
        var refreshJwt = dto.getRefreshToken();

        var isValidToken = jwtService.isValidToken(refreshJwt);
        if (!isValidToken.getLeft()) {
            log.error("Invalid refresh token");
            throw new ShopException(UNPROCESSABLE_ENTITY, isValidToken.getRight());
        }


        ShopUser user = jwtService.getUserCtxFromToken(refreshJwt);
        var userDetails = userService.userDetailsService()
                .loadUserByUsername(user.getLogin());
        var userCtx = mapper.toUserCtx((AuthContext) userDetails);

        var token = jwtService.generateToken(userCtx);
        var refreshToken = jwtService.generateRefreshToken(userCtx);
        var profile = mapper.toDto(userCtx);
        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .profile(profile)
                .build();
    }


}
