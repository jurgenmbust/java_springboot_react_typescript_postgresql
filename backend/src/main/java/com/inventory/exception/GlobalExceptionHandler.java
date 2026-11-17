package com.inventory.exception;
import com.inventory.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.Instant;
import java.util.*;

/** Converts exceptions into JSON errors instead of exposing stack traces. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiError> error(int status, String message, HttpServletRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status, message, request.getRequestURI(), fields));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest request) { return error(404, ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest request) { return error(409, ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(field -> fields.putIfAbsent(field.getField(), field.getDefaultMessage()));
        return error(400, "Please check the submitted fields.", request, fields);
    }
    // Constraints also protect against simultaneous conflicting requests.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return error(409, "A unique value already exists or this record is still in use.", request, Map.of());
    }
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest request) { return error(400, "Invalid request. Check IDs, numbers, filters and JSON format.", request, Map.of()); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> missingRoute(Exception ex, HttpServletRequest request) { return error(404, "Route not found.", request, Map.of()); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> method(Exception ex, HttpServletRequest request) { return error(405, "HTTP method not allowed.", request, Map.of()); }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> contentType(Exception ex, HttpServletRequest request) { return error(415, "Use Content-Type: application/json.", request, Map.of()); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        // Do not log request bodies, passwords or private connection strings.
        log.error("Unexpected server error: {}", ex.getClass().getSimpleName());
        return error(500, "An unexpected server error occurred.", request, Map.of());
    }
}
