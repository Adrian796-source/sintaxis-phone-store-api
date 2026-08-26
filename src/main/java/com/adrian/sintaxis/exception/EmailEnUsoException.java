package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailEnUsoException extends RuntimeException {

    public EmailEnUsoException(String message) {
        super(message);
    }
}