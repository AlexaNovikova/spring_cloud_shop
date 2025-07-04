package ru.otus.order.service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.otus.order.service.config.annotations.IdempotenceKey;
import ru.otus.order.service.exception.IdempotenceException;
import ru.otus.order.service.service.IdempotenceKeyService;

import java.lang.reflect.Method;

@Slf4j
@Component
public class IdempotentInterceptor implements HandlerInterceptor {

    private final IdempotenceKeyService idempotenceKeyService;

    public IdempotentInterceptor(IdempotenceKeyService idempotenceKeyService) {
        this.idempotenceKeyService = idempotenceKeyService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod)) { return true; }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        IdempotenceKey methodAnnotation = method.getAnnotation(IdempotenceKey.class);
        if (methodAnnotation != null) {
            try {
                log.debug("Check ");
                return idempotenceKeyService.checkIdempotenceRequest(request);
            } catch (IdempotenceException e) {
                throw e;
            } catch (Exception e) {
                throw new IdempotenceException("check idempotence exception:" + e.getMessage());
            }
        }
        return true;
    }
}
