package com.example.test.exception;

import com.example.test.response.AbstractResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AbstractResponse<Object>> handleExceptions(Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof DataAccessException
            || e instanceof InsufficientFundsException || e instanceof HttpMessageNotReadableException
            || e instanceof MethodArgumentTypeMismatchException || e instanceof MissingServletRequestParameterException) {
            return new ResponseEntity<>(new AbstractResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST);
        } else if (e instanceof NotFoundException) {
            return new ResponseEntity<>(new AbstractResponse<>(HttpStatus.NOT_FOUND, e.getMessage(), null),
                    HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(new AbstractResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}