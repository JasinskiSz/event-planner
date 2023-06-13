package com.szymon_jasinski.eventplanner.controllers;

import com.szymon_jasinski.eventplanner.dtos.RestExceptionResponse;
import com.szymon_jasinski.eventplanner.exceptions.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<RestExceptionResponse> handleObjectNotFoundException(ObjectNotFoundException e) {
        return new ResponseEntity<>(RestExceptionResponse.builder()
                .reason(e.getMessage())
                .build(),
                HttpStatus.NOT_FOUND);
    }
}
