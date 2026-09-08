package com.sanghee.glowuprizzcrm.core.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Operator 도메인 단위 테스트")
class OperatorTest {

    @Test
    @DisplayName("이메일/비밀번호 해시가 유효하면 생성된다")
    void create_success() {
        Operator operator = new Operator("operator@glowuprizz.com", "hashed-password");

        assertThat(operator.getEmail()).isEqualTo("operator@glowuprizz.com");
        assertThat(operator.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(operator.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일이 비어있으면 예외가 발생한다")
    void create_throws_whenEmailBlank() {
        assertThatThrownBy(() -> new Operator(" ", "hashed-password"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 예외가 발생한다")
    void create_throws_whenEmailMalformed() {
        assertThatThrownBy(() -> new Operator("not-an-email", "hashed-password"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);
    }

    @Test
    @DisplayName("비밀번호 해시가 비어있으면 예외가 발생한다")
    void create_throws_whenPasswordHashBlank() {
        assertThatThrownBy(() -> new Operator("operator@glowuprizz.com", " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD_HASH);
    }
}
