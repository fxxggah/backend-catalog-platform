package com.catalog.exception;

import com.catalog.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(HttpStatus.UNAUTHORIZED, errorCode, message);
    }
}