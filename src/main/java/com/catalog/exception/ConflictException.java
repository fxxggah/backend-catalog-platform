package com.catalog.exception;

import com.catalog.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}