package com.catalog.exception;

import com.catalog.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(HttpStatus.FORBIDDEN, errorCode, message);
    }
}