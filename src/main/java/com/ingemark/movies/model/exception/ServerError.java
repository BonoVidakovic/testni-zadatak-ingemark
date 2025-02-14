package com.ingemark.movies.model.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(code = INTERNAL_SERVER_ERROR, reason = "Internal Server Error")
public class ServerError extends RuntimeException{
    public ServerError(String message) {
        super(message);
    }

    public ServerError(String message, Throwable cause) {
        super(message, cause);
    }
}
