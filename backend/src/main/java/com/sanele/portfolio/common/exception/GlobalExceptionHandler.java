package com.sanele.portfolio.common.exception;

import com.sanele.portfolio.media.exception.InvalidMediaException;
import com.sanele.portfolio.messages.dto.ContactRateLimitExceededException;
import com.sanele.portfolio.analytics.service.AnalyticsDateRangeException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiFieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "The request contains invalid fields.", request, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        List<ApiFieldError> errors = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ApiFieldError(result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "The request contains invalid fields.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "The request body is missing or contains an invalid value.", request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AnalyticsDateRangeException.class)
    ResponseEntity<ApiError> handleAnalyticsDateRange(AnalyticsDateRangeException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ContactRateLimitExceededException.class)
    ResponseEntity<ApiError> handleContactRateLimit(ContactRateLimitExceededException exception,
            HttpServletRequest request) {
        return response(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED",
                exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidMediaException.class)
    ResponseEntity<ApiError> handleInvalidMedia(InvalidMediaException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_MEDIA", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleOversizedUpload(MaxUploadSizeExceededException exception,
            HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "MEDIA_TOO_LARGE",
                "Uploaded files must be 10 MB or smaller.", request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLICT", "The request conflicts with existing data.", request, List.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> handleForbidden(ForbiddenException exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid username or password.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", request, List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
            HttpServletRequest request, List<ApiFieldError> errors) {
        ApiError error = new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(error);
    }
}