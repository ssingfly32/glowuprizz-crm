package com.sanghee.glowuprizzcrm.admin.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HtmlTemplateService 단위 테스트")
class HtmlTemplateServiceTest {

    private final HtmlTemplateRepository htmlTemplateRepository = mock(HtmlTemplateRepository.class);
    private final HtmlTemplateService htmlTemplateService = new HtmlTemplateService(htmlTemplateRepository);

    // 업로드된 파일의 원본 파일명이 .html로 끝나는지는 서비스가 엔티티 생성 전에 검증한다
    // (docs/adr/0016 참고). CampaignService가 publicSlug 중복을 엔티티 생성 전에
    // 서비스에서 검사하는 것과 같은 패턴이다.
    @Test
    @DisplayName("파일명이 .html로 끝나지 않으면 INVALID_TEMPLATE_FILE_EXTENSION을 던진다")
    void register_throwsInvalidTemplateFileExtension_whenFilenameIsNotHtml() {
        assertThatThrownBy(() -> htmlTemplateService.register(1L, "이름", "template.txt", "<html></html>"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_TEMPLATE_FILE_EXTENSION));
    }

    @Test
    @DisplayName("원본 파일명이 없으면 INVALID_TEMPLATE_FILE_EXTENSION을 던진다")
    void register_throwsInvalidTemplateFileExtension_whenFilenameIsNull() {
        assertThatThrownBy(() -> htmlTemplateService.register(1L, "이름", null, "<html></html>"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_TEMPLATE_FILE_EXTENSION));
    }

    @Test
    @DisplayName("파일명이 .html로 끝나면 저장하고 반환한다")
    void register_savesTemplate_whenFilenameEndsWithHtml() {
        when(htmlTemplateRepository.save(any(HtmlTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HtmlTemplate result = htmlTemplateService.register(1L, "이름", "template.html", "<html></html>");

        assertThat(result.getName()).isEqualTo("이름");
        verify(htmlTemplateRepository).save(any(HtmlTemplate.class));
    }

    @Test
    @DisplayName("확장자 대소문자와 무관하게 .html로 끝나면 통과한다")
    void register_savesTemplate_whenExtensionIsUpperCase() {
        when(htmlTemplateRepository.save(any(HtmlTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HtmlTemplate result = htmlTemplateService.register(1L, "이름", "TEMPLATE.HTML", "<html></html>");

        assertThat(result).isNotNull();
    }
}
