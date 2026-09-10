package com.sanghee.glowuprizzcrm.admin.common;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @Valid 실패(MethodArgumentNotValidException)를 Spring 기본 처리기(/error 포워드)로
// 넘기지 않고 여기서 직접 처리한다. /error로 포워드되면 그 요청은 새로운 디스패치로 취급돼
// JwtAuthenticationFilter(OncePerRequestFilter)가 기본적으로 재적용되지 않아 인증되지 않은
// 요청처럼 401로 잘못 보일 수 있다 (라이브 검증 중 실제로 재현된 문제).
@RestControllerAdvice
public class AdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        HttpStatus status = toHttpStatus(e.getErrorCode());
        return ResponseEntity.status(status).body(new ErrorResponse(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", message));
    }

    // BusinessException/MethodArgumentNotValidException으로 분류되지 않은 예상 못한 예외를
    // 잡는 최종 안전망. 이게 없으면 Spring 기본 에러 처리로 빠져 {code,message} 계약이 깨진
    // 응답(혹은 빈 바디)이 새어나간다 (라이브 검증 중 실제로 재현: 잘못된 JSON 바디 요청 시
    // 400 + 빈 바디를 반환하고 있었음).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("unexpected exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    private HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case AUTH_INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case TEMPLATE_NOT_FOUND, CAMPAIGN_NOT_FOUND, LINK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_PUBLIC_SLUG -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
