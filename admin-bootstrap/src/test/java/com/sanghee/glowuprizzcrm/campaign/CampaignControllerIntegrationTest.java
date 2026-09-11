package com.sanghee.glowuprizzcrm.campaign;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.campaign.CampaignCreateRequest;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

@DisplayName("/admin/campaigns 통합 테스트")
class CampaignControllerIntegrationTest extends AbstractAdminIntegrationTest {

    private Long registerTemplate(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "template.html", MediaType.TEXT_HTML_VALUE,
                "<html><body><form>x</form></body></html>".getBytes(StandardCharsets.UTF_8));
        MvcResult result = mockMvc.perform(multipart("/admin/html-templates")
                        .file(file)
                        .param("name", "캠페인 테스트용 템플릿")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("id").asLong();
    }

    @Test
    @DisplayName("인증 없이 생성하면 401을 반환한다")
    void create_returns401_whenNoToken() throws Exception {
        CampaignCreateRequest request = new CampaignCreateRequest(1L, "가을 웨비나", "autumn-webinar");

        mockMvc.perform(post("/admin/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("등록된 템플릿으로 정상 생성하면 201과 published=false를 반환한다")
    void create_returns201_whenValid() throws Exception {
        String token = obtainAccessToken();
        Long templateId = registerTemplate(token);
        CampaignCreateRequest request = new CampaignCreateRequest(templateId, "가을 웨비나", "autumn-webinar");

        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesRegex(".*/admin/campaigns/\\d+")))
                .andExpect(jsonPath("$.name").value("가을 웨비나"))
                .andExpect(jsonPath("$.publicSlug").value("autumn-webinar"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 id로 생성하면 404와 TEMPLATE_NOT_FOUND를 반환한다")
    void create_returns404_whenTemplateNotFound() throws Exception {
        String token = obtainAccessToken();
        CampaignCreateRequest request = new CampaignCreateRequest(999999L, "가을 웨비나", "autumn-webinar-2");

        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    @DisplayName("이미 사용 중인 publicSlug로 생성하면 409와 DUPLICATE_PUBLIC_SLUG를 반환한다")
    void create_returns409_whenPublicSlugAlreadyExists() throws Exception {
        String token = obtainAccessToken();
        Long templateId = registerTemplate(token);
        CampaignCreateRequest firstRequest = new CampaignCreateRequest(templateId, "가을 웨비나", "duplicate-slug");
        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CampaignCreateRequest secondRequest = new CampaignCreateRequest(templateId, "겨울 웨비나", "duplicate-slug");
        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_PUBLIC_SLUG"));
    }

    @Test
    @DisplayName("publicSlug 형식이 잘못되면 400을 반환한다")
    void create_returns400_whenSlugInvalid() throws Exception {
        String token = obtainAccessToken();
        Long templateId = registerTemplate(token);
        CampaignCreateRequest request = new CampaignCreateRequest(templateId, "가을 웨비나", "Invalid Slug!!");

        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PUBLIC_SLUG"));
    }

    @Test
    @DisplayName("존재하지 않는 캠페인을 조회하면 404와 CAMPAIGN_NOT_FOUND를 반환한다")
    void get_returns404_whenNotFound() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(get("/admin/campaigns/999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("캠페인을 공개하면 published가 true로 바뀐다")
    void publish_setsPublishedTrue() throws Exception {
        String token = obtainAccessToken();
        Long templateId = registerTemplate(token);
        CampaignCreateRequest createRequest = new CampaignCreateRequest(templateId, "공개 테스트", "publish-test");
        MvcResult created = mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        long campaignId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(post("/admin/campaigns/" + campaignId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("생성한 캠페인이 목록 조회에 포함된다")
    void list_returnsCreatedCampaigns() throws Exception {
        String token = obtainAccessToken();
        Long templateId = registerTemplate(token);
        CampaignCreateRequest request = new CampaignCreateRequest(templateId, "목록 테스트", "list-test");
        mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.publicSlug == 'list-test')]").exists());
    }
}
