package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailYaExistenteException extends RuntimeException {

    public EmailYaExistenteException(String message) {
        super(message);
    }
}