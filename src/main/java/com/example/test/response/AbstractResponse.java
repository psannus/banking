package com.example.test.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class AbstractResponse<T> {
    private HttpStatus status;
    private String message;
    private T data;
}
