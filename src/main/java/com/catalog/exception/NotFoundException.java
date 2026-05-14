package com.catalog.exception;

import com.catalog.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(ErrorCode errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}