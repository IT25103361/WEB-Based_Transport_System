package com.webtrans.school.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestControllerAdvice(basePackages = "com.webtrans.school")
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<?> missing(NotFoundException e) {
        e.printStackTrace();
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ResponseEntity<?> handleResponseStatus(org.springframework.web.server.ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return error(status, e.getReason() != null ? e.getReason() : e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> error(Exception e) {
        e.printStackTrace();
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The transport service could not complete this request.");
    }

    private ResponseEntity<?> error(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(Map.of("timestamp", LocalDateTime.now(), "status", s.value(), "message", m));
    }
}