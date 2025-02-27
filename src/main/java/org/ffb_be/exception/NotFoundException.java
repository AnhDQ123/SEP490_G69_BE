package org.ffb_be.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String resourceName) {
        super(resourceName + " không tồn tại.", HttpStatus.NOT_FOUND);
    }
}
