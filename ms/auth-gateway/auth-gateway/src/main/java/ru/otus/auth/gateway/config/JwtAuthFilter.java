package ru.otus.auth.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Encoders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.otus.auth.shared.service.JwtService;
import ru.otus.common.error.ErrorDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";
    private static final String JWT_PAYLOAD_HEADER = "X-Jwt-Payload";
    private static final String UNAUTHORIZED = "unauthorized";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var authHeader = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        if (!StringUtils.isEmpty(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {

            var jwt = authHeader.substring(BEARER_PREFIX.length());

            var isValidToken = jwtService.isValidToken(jwt);
            if (!isValidToken.getLeft()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                var response = ErrorDto.builder()
                        .code(UNAUTHORIZED)
                        .message(isValidToken.getRight())
                        .build();

                byte[] bytes = objectMapper.writeValueAsBytes(response);
                return exchange.getResponse().writeWith(Mono.just(
                        exchange.getResponse().bufferFactory().wrap(bytes)
                ));
            }

            var userCtx = jwtService.getUserCtxFromToken(jwt);
            var payload = Encoders.BASE64.encode(objectMapper.writeValueAsBytes(userCtx));

            var modifiedRequest = exchange.getRequest().mutate()
                    .header(HEADER_NAME, authHeader)
                    .header(JWT_PAYLOAD_HEADER, payload)
                    .build();

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userCtx,
                    null,
                    jwtService.extractAuthorities(jwt)
            );

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        return chain.filter(exchange);
    }
}