package com.catalog.exception;

import com.catalog.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(ErrorCode errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}