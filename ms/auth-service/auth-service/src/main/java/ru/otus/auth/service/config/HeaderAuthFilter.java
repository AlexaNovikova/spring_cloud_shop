package ru.otus.auth.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.otus.auth.shared.service.JwtService;
import ru.otus.common.error.ErrorDto;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class HeaderAuthFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    private static final String UNAUTHORIZED = "unauthorized";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader(HEADER_NAME);
        if (!StringUtils.isEmpty(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {

            var jwt = authHeader.substring(BEARER_PREFIX.length());

            var isValidToken = jwtService.isValidToken(jwt);
            if (!isValidToken.getLeft()) {
                var errorResponse = ErrorDto.builder()
                        .code(UNAUTHORIZED)
                        .message(isValidToken.getRight())
                        .build();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                objectMapper.writeValue(response.getWriter(), errorResponse);
                return;
            }

            var userCtx = jwtService.getUserCtxFromToken(jwt);

            if (userCtx != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userCtx,
                        null,
                        null
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }

        filterChain.doFilter(request, response);
    }
}