package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordIncorrectaException extends RuntimeException {

    public PasswordIncorrectaException(String message) {
        super(message);
    }
}