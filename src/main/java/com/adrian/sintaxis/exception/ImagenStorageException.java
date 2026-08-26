package com.adrian.sintaxis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ImagenStorageException extends RuntimeException {

    public ImagenStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}