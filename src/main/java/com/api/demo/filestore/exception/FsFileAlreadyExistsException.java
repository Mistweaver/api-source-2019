package com.api.demo.filestore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class FsFileAlreadyExistsException extends RuntimeException {
    public FsFileAlreadyExistsException(String message) {
        super(message);
    }

    public FsFileAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}