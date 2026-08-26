package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ClienteYaAsociadoException extends RuntimeException {

    public ClienteYaAsociadoException(String message) {
        super(message);
    }
}