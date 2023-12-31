package ru.netology.cloudstorage.exception;


import lombok.extern.java.Log;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.netology.cloudstorage.dto.ErrorDTO;

import java.util.Locale;
import java.util.Objects;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Log
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;
    private final Locale locale = Locale.getDefault();

    @Autowired
    public ExceptionHandlers(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull).reduce(String::concat).orElse(
                        messageSource.getMessage("format-invalid", null, locale));

        ErrorDTO error = new ErrorDTO(message, 102);
        log.info(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorDTO error = new ErrorDTO(messageSource.getMessage("bad-request", null, locale), 101);
        log.info(ex.getMessage());
        return handleExceptionInternal(ex, error, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, 0);
        }
        if (HttpStatus.BAD_REQUEST.equals(status)) {
            ErrorDTO error = new ErrorDTO(messageSource.getMessage("bad-request", null, locale), 100);
            log.info(ex.getMessage());
            return new ResponseEntity<>(error, status);
        }
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity<ErrorDTO> handleUnauthorized(BadCredentialsException e) {
        ErrorDTO error = new ErrorDTO(messageSource.getMessage("bad-credentials", null, locale), 100);
        log.info(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {DataAccessException.class})
    public ResponseEntity<ErrorDTO> handleDataAccessException(DataAccessException e) {
        ErrorDTO error;

        if (e.getCause() instanceof ConstraintViolationException) {
            var ex = (ConstraintViolationException) e.getCause();
            if (ex.getConstraintName().equals("uk_files")) {
                error = new ErrorDTO(messageSource.getMessage("duplicate.object", null, locale), 103);
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
        }

        error = new ErrorDTO(messageSource.getMessage("internal.error", null, locale), 104);
        log.info(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> handleRuntimeException(Exception e) {

        ErrorDTO error = new ErrorDTO(messageSource.getMessage("internal.error", null, locale), 106);
        log.info(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
