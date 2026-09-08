package com.sanghee.glowuprizzcrm.template;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.template.HtmlTemplateCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

@DisplayName("/admin/html-templates 통합 테스트")
class HtmlTemplateControllerIntegrationTest extends AbstractAdminIntegrationTest {

    @Test
    @DisplayName("인증 없이 등록하면 401을 반환한다")
    void register_returns401_whenNoToken() throws Exception {
        var request = new HtmlTemplateCreateRequest("무인증 테스트", "<html></html>");

        mockMvc.perform(post("/admin/html-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("정상 등록하면 201과 생성된 템플릿 정보를 반환한다")
    void register_returns201_whenValid() throws Exception {
        String token = obtainAccessToken();
        var request = new HtmlTemplateCreateRequest("정상 등록 테스트", "<html><body><form>x</form></body></html>");

        mockMvc.perform(post("/admin/html-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("정상 등록 테스트"));
    }

    // 이 테스트는 라이브 검증 중 실제로 재현됐던 회귀 버그를 고정한다:
    // @Valid 검증 실패가 /error 재디스패치를 타면서 JwtAuthenticationFilter가 인증정보를
    // 못 심어 401로 잘못 보이던 문제 (docs/adr 언급 없음, PR #2 설명 참고). 400이어야 한다.
    @Test
    @DisplayName("content가 비어있으면 400과 VALIDATION_FAILED를 반환한다 (401이 아니어야 함)")
    void register_returns400_whenContentIsBlank() throws Exception {
        String token = obtainAccessToken();
        var request = new HtmlTemplateCreateRequest("빈 콘텐츠", "");

        mockMvc.perform(post("/admin/html-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("등록한 템플릿 목록을 조회할 수 있다")
    void list_returnsRegisteredTemplates() throws Exception {
        String token = obtainAccessToken();
        var request = new HtmlTemplateCreateRequest("목록 조회 테스트", "<html></html>");
        mockMvc.perform(post("/admin/html-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
