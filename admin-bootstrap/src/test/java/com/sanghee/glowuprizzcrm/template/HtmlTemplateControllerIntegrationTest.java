package com.sanghee.glowuprizzcrm.template;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("/admin/html-templates 통합 테스트")
class HtmlTemplateControllerIntegrationTest extends AbstractAdminIntegrationTest {

    private MockMultipartFile htmlFile(String filename, String content) {
        return new MockMultipartFile("file", filename, MediaType.TEXT_HTML_VALUE, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("인증 없이 등록하면 401을 반환한다")
    void register_returns401_whenNoToken() throws Exception {
        mockMvc.perform(multipart("/admin/html-templates")
                        .file(htmlFile("template.html", "<html></html>"))
                        .param("name", "무인증 테스트"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("정상 등록하면 201과 생성된 템플릿 정보를 반환한다")
    void register_returns201_whenValid() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(multipart("/admin/html-templates")
                        .file(htmlFile("template.html", "<html><body><form>x</form></body></html>"))
                        .param("name", "정상 등록 테스트")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesRegex(".*/admin/html-templates/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("정상 등록 테스트"));
    }

    @Test
    @DisplayName("파일 내용이 비어있으면 400과 INVALID_TEMPLATE_CONTENT를 반환한다 (401이 아니어야 함)")
    void register_returns400_whenFileContentIsBlank() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(multipart("/admin/html-templates")
                        .file(htmlFile("empty.html", ""))
                        .param("name", "빈 파일")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TEMPLATE_CONTENT"));
    }

    @Test
    @DisplayName("확장자가 .html이 아니면 400과 INVALID_TEMPLATE_FILE_EXTENSION을 반환한다")
    void register_returns400_whenFileExtensionIsNotHtml() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(multipart("/admin/html-templates")
                        .file(htmlFile("template.txt", "<html></html>"))
                        .param("name", "잘못된 확장자")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TEMPLATE_FILE_EXTENSION"));
    }

    @Test
    @DisplayName("등록한 템플릿 목록을 조회할 수 있다")
    void list_returnsRegisteredTemplates() throws Exception {
        String token = obtainAccessToken();
        mockMvc.perform(multipart("/admin/html-templates")
                        .file(htmlFile("template.html", "<html></html>"))
                        .param("name", "목록 조회 테스트")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/admin/html-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '목록 조회 테스트')]").exists());
    }

    @Test
    @DisplayName("존재하지 않는 id를 조회하면 404와 TEMPLATE_NOT_FOUND를 반환한다")
    void get_returns404_whenNotFound() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(get("/admin/html-templates/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEMPLATE_NOT_FOUND"));
    }
}
