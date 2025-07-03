package ru.otus.lib.idempotency;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.otus.common.error.ShopException;
import ru.otus.common.error.ErrorDto;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    private static final String UNAUTHORIZED = "unauthorized";

    @ExceptionHandler({ AuthenticationException.class })
    public ResponseEntity<ErrorDto> handleAuthenticationDeniedException(
            Exception ex, WebRequest request) {
        log.error("Authentication exception : ", ex);
        var message = ex.getMessage() != null ? ex.getMessage() : UNAUTHORIZED;
        var errorDto = ErrorDto.builder()
                .code(UNAUTHORIZED)
                .message(message)
                .build();
        return new ResponseEntity<ErrorDto>(errorDto, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<ErrorDto> handleAccessDeniedException(
            Exception ex, WebRequest request) {
        log.error("Access Denied exception occurred: ", ex);
        var errorDto = ErrorDto.builder()
                .code(UNAUTHORIZED)
                .message("Доступ запрещен")
                .build();
        return new ResponseEntity<>(errorDto, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ ShopException.class })
    public ResponseEntity<ErrorDto> handleBusinessAppException(
            Exception ex, WebRequest request) {
        log.error("Business App exception occurred: ", ex);
        var appException = (ShopException) ex;
        var errorDto = ErrorDto.builder()
                .code(appException.getCode())
                .message(appException.getMessage())
                .build();
        var status = appException.getHttpStatus() != null ?
                HttpStatus.valueOf(appException.getHttpStatus()) :
                HttpStatus.UNPROCESSABLE_ENTITY;
        return new ResponseEntity<>(errorDto, new HttpHeaders(), status);
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorDto> handleNoSuchElementException(Exception ex, WebRequest request) {
        log.error("Entity not found exception : ", ex);
        var errorDto = ErrorDto.builder()
                .code("not.found")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorDto, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({EntityExistsException.class})
    public ResponseEntity<ErrorDto> handleEntityExistsException(Exception ex, WebRequest request) {
        log.error("Entity exists exception occurred: ", ex);
        var errorDto = ErrorDto.builder()
                .code("entity.exists")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<ErrorDto>(errorDto, new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorDto> handleIllegalException(Exception ex, WebRequest request) {
        log.error("Illegal state or argument exception occurred: ", ex);
        var errorDto = ErrorDto.builder()
                .code("unprocessable.entity")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorDto, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handle(Exception ex, WebRequest request) {
        log.error("Exception occurred: ", ex);
        var errorDto = ErrorDto.builder()
                .code("internal.system.error")
                .message("Сервис недоступен. Попробуйте позднее")
                .build();
        return new ResponseEntity<ErrorDto>(errorDto, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        var errorDto = ErrorDto.builder()
                .code("bad.request")
                .message("Неверно введенные данные")
                .info(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}