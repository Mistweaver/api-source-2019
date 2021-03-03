package com.api.demo.filestore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FsFileNotFoundException extends RuntimeException {
    public FsFileNotFoundException(String message) {
        super(message);
    }

    public FsFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}