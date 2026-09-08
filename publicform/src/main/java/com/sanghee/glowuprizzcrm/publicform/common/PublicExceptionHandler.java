package com.sanghee.glowuprizzcrm.publicform.common;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PublicExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        HttpStatus status = toHttpStatus(e.getErrorCode());
        return ResponseEntity.status(status).body(new ErrorResponse(e.getErrorCode().name(), e.getMessage()));
    }

    private HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case CAMPAIGN_NOT_FOUND, CAMPAIGN_NOT_PUBLISHED, LINK_NOT_FOUND, TEMPLATE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
