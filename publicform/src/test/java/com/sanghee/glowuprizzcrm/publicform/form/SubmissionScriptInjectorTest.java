package com.sanghee.glowuprizzcrm.publicform.form;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SubmissionScriptInjector 단위 테스트")
class SubmissionScriptInjectorTest {

    private final SubmissionScriptInjector injector = new SubmissionScriptInjector();

    @Test
    @DisplayName("</body> 앞에 제출 스크립트를 삽입한다")
    void inject_insertsScriptBeforeClosingBodyTag() {
        String html = "<html><body><form><input name=\"email\"></form></body></html>";

        String result = injector.inject(html, "/f/test-slug/submissions");

        assertThat(result).contains("<script>");
        assertThat(result).contains("/f/test-slug/submissions");
        assertThat(result.indexOf("<script>")).isLessThan(result.indexOf("</body>"));
    }

    @Test
    @DisplayName("</body> 태그가 없으면 뒤에 그냥 덧붙인다")
    void inject_appendsScriptWhenNoClosingBodyTag() {
        String html = "<div>no body tag</div>";

        String result = injector.inject(html, "/f/test-slug/submissions");

        assertThat(result).startsWith(html);
        assertThat(result).contains("<script>");
    }

    @Test
    @DisplayName("원본 HTML의 나머지 내용은 그대로 보존한다")
    void inject_preservesOriginalContent() {
        String html = "<html><body><h1>제목</h1><form></form></body></html>";

        String result = injector.inject(html, "/f/test-slug/submissions");

        assertThat(result).contains("<h1>제목</h1>");
    }
}
