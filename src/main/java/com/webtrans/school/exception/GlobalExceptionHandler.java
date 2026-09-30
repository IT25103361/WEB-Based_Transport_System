package com.webtrans.school.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<?> missing(NotFoundException e) {
        e.printStackTrace();
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> error(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The transport service could not complete this request.");
    }

    private ResponseEntity<?> error(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(Map.of("timestamp", LocalDateTime.now(), "status", s.value(), "message", m));
    }
}