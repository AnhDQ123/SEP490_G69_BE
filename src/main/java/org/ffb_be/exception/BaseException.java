package org.ffb_be.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseException extends RuntimeException {
    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
