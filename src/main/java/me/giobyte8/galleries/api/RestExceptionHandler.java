package me.giobyte8.galleries.api;

import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class RestExceptionHandler {

    public record ErrorResponse(Instant timestamp, String message) {}

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDirectoryNotFound(DirectoryNotFoundException ex) {
        String message = ex == null ? "Directory not found" : ex.getMessage();
        ErrorResponse body = new ErrorResponse(Instant.now(), message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
