package com.sanghee.glowuprizzcrm.core.common.validation;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;

// 엔티티 생성자에서 반복되는 불변식 검증을 모아둔 헬퍼.
public final class Assert {

    private Assert() {
    }

    public static void notBlank(String value, ErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
    }

    public static void maxLength(String value, int maxLength, ErrorCode errorCode) {
        if (value.length() > maxLength) {
            throw new BusinessException(errorCode);
        }
    }

    public static void matches(String value, String regex, ErrorCode errorCode) {
        if (!value.matches(regex)) {
            throw new BusinessException(errorCode);
        }
    }
}
