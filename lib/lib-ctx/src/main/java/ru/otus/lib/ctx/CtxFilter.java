package ru.otus.lib.ctx;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.otus.common.ShopUser;

import java.io.IOException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CtxFilter extends OncePerRequestFilter {

    public static final String JWT_PAYLOAD_HEADER = "X-Jwt-Payload";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var base64Payload = request.getHeader(JWT_PAYLOAD_HEADER);
        if (!StringUtils.isEmpty(base64Payload)) {

            Base64.Decoder decoder = Base64.getUrlDecoder();
            var payload = decoder.decode(base64Payload);
            var userCtx = objectMapper.readValue(payload, ShopUser.class);
            request.setAttribute("userCtx", userCtx);
        }

        filterChain.doFilter(request, response);
    }
}