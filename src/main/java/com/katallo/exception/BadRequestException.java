package com.katallo.exception;

import com.katallo.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(ErrorCode errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}