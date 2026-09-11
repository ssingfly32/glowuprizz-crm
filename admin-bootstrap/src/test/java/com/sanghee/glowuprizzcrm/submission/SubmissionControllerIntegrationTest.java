package com.sanghee.glowuprizzcrm.submission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.campaign.CampaignCreateRequest;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.submission.Submission;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("/admin/campaigns/{campaignId}/submissions 통합 테스트")
class SubmissionControllerIntegrationTest extends AbstractAdminIntegrationTest {

    @Autowired
    private SubmissionRepository submissionRepository;

    private long createCampaign(String token, String publicSlug) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "template.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes(StandardCharsets.UTF_8));
        MvcResult templateResult = mockMvc.perform(multipart("/admin/html-templates")
                        .file(file)
                        .param("name", "신청자 명단 테스트용 템플릿")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        long templateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
                .path("id").asLong();

        CampaignCreateRequest campaignRequest = new CampaignCreateRequest(templateId, "신청자 명단 테스트 캠페인", publicSlug);
        MvcResult campaignResult = mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campaignRequest)))
                .andReturn();
        return objectMapper.readTree(campaignResult.getResponse().getContentAsString()).path("id").asLong();
    }

    @Test
    @DisplayName("인증 없이 조회하면 401을 반환한다")
    void list_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/admin/campaigns/1/submissions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 캠페인을 조회하면 404와 CAMPAIGN_NOT_FOUND를 반환한다")
    void list_returns404_whenCampaignNotFound() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(get("/admin/campaigns/999999/submissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("신청 데이터가 없으면 빈 배열을 반환한다")
    void list_returnsEmptyArray_whenNoSubmissions() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token, "crm-empty-campaign");

        mockMvc.perform(get("/admin/campaigns/" + campaignId + "/submissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("신청자 명단을 원본 데이터와 함께 최신순으로 반환한다")
    void list_returnsSubmissionsWithOriginalData_newestFirst() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token, "crm-campaign");

        submissionRepository.save(
                new Submission(campaignId, null, Channel.INSTAGRAM, "v1", "{\"name\":\"홍길동\",\"phone\":\"010-1111-2222\"}"));
        submissionRepository.save(
                new Submission(campaignId, null, Channel.X, "v2", "{\"name\":\"김철수\",\"phone\":\"010-3333-4444\"}"));

        mockMvc.perform(get("/admin/campaigns/" + campaignId + "/submissions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].channel").value("X"))
                .andExpect(jsonPath("$[0].data.name").value("김철수"))
                .andExpect(jsonPath("$[0].data.phone").value("010-3333-4444"))
                .andExpect(jsonPath("$[1].channel").value("INSTAGRAM"))
                .andExpect(jsonPath("$[1].data.name").value("홍길동"));
    }
}
