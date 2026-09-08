package com.sanghee.glowuprizzcrm.core.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HtmlTemplate 도메인 단위 테스트")
class HtmlTemplateTest {

    @Test
    @DisplayName("이름/내용이 유효하면 생성된다")
    void create_success() {
        HtmlTemplate template = new HtmlTemplate(1L, "가을 웨비나 폼", "<html></html>");

        assertThat(template.getOperatorId()).isEqualTo(1L);
        assertThat(template.getName()).isEqualTo("가을 웨비나 폼");
        assertThat(template.getContent()).isEqualTo("<html></html>");
    }

    @Test
    @DisplayName("이름이 비어있으면 예외가 발생한다")
    void create_throws_whenNameBlank() {
        assertThatThrownBy(() -> new HtmlTemplate(1L, " ", "<html></html>"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TEMPLATE_NAME);
    }

    @Test
    @DisplayName("이름이 255자를 넘으면 예외가 발생한다")
    void create_throws_whenNameTooLong() {
        String name = "a".repeat(256);

        assertThatThrownBy(() -> new HtmlTemplate(1L, name, "<html></html>"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TEMPLATE_NAME);
    }

    @Test
    @DisplayName("내용이 비어있으면 예외가 발생한다")
    void create_throws_whenContentBlank() {
        assertThatThrownBy(() -> new HtmlTemplate(1L, "이름", " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TEMPLATE_CONTENT);
    }
}
