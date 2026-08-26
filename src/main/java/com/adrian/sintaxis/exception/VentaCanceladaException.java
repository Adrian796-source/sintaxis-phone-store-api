package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VentaCanceladaException extends RuntimeException {

    public VentaCanceladaException(String message) {
        super(message);
    }
}