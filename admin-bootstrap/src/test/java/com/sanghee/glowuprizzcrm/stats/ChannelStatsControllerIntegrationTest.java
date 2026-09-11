package com.sanghee.glowuprizzcrm.stats;

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
import com.sanghee.glowuprizzcrm.core.visit.Visit;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("/admin/channels/stats 통합 테스트")
class ChannelStatsControllerIntegrationTest extends AbstractAdminIntegrationTest {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private long createCampaign(String token, String publicSlug) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "template.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes(StandardCharsets.UTF_8));
        MvcResult templateResult = mockMvc.perform(multipart("/admin/html-templates")
                        .file(file)
                        .param("name", "전역 채널 성과 테스트용 템플릿")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        long templateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
                .path("id").asLong();

        CampaignCreateRequest campaignRequest = new CampaignCreateRequest(templateId, "전역 채널 성과 테스트 캠페인", publicSlug);
        MvcResult campaignResult = mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campaignRequest)))
                .andReturn();
        return objectMapper.readTree(campaignResult.getResponse().getContentAsString()).path("id").asLong();
    }

    @Test
    @DisplayName("인증 없이 조회하면 401을 반환한다")
    void get_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/admin/channels/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("데이터가 전혀 없으면 빈 배열을 반환한다")
    void get_returnsEmptyArray_whenNoData() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(get("/admin/channels/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("여러 캠페인의 방문/신청을 채널 기준으로 합산해 반환한다")
    void get_returnsStatsAggregatedAcrossCampaigns() throws Exception {
        String token = obtainAccessToken();
        long campaignA = createCampaign(token, "global-stats-campaign-a");
        long campaignB = createCampaign(token, "global-stats-campaign-b");

        // INSTAGRAM: 캠페인 A에서 방문자(v1) 1번 방문 + 1번 신청, 캠페인 B에서 방문자(v2) 1번 방문
        // -> visit=2, visitor=2, submission=1
        visitRepository.save(new Visit(campaignA, null, Channel.INSTAGRAM, "v1"));
        submissionRepository.save(new Submission(campaignA, null, Channel.INSTAGRAM, "v1", "{\"name\":\"a\"}"));
        visitRepository.save(new Visit(campaignB, null, Channel.INSTAGRAM, "v2"));

        // X: 캠페인 B에서만 방문자(v3) 1번 방문, 1번 신청 -> visit=1, visitor=1, submission=1
        visitRepository.save(new Visit(campaignB, null, Channel.X, "v3"));
        submissionRepository.save(new Submission(campaignB, null, Channel.X, "v3", "{\"name\":\"b\"}"));

        mockMvc.perform(get("/admin/channels/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.channel == 'INSTAGRAM')].visitCount").value(2))
                .andExpect(jsonPath("$[?(@.channel == 'INSTAGRAM')].visitorCount").value(2))
                .andExpect(jsonPath("$[?(@.channel == 'INSTAGRAM')].submissionCount").value(1))
                .andExpect(jsonPath("$[?(@.channel == 'X')].visitCount").value(1))
                .andExpect(jsonPath("$[?(@.channel == 'X')].visitorCount").value(1))
                .andExpect(jsonPath("$[?(@.channel == 'X')].submissionCount").value(1))
                .andExpect(jsonPath("$[?(@.channel == 'X')].conversionRate").value(1.0));
    }
}
