package com.ryle.exception;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e){
        Map<String, Object> data = new HashMap<>();
        data.put("message", e.getMessage());
        data.put("status", HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(data);
    }
}
