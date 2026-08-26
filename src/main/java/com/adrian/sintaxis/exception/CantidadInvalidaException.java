package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CantidadInvalidaException extends RuntimeException {

    public CantidadInvalidaException(String message) {
        super(message);
    }
}