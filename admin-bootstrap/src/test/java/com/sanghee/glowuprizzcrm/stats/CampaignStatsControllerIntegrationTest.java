package com.sanghee.glowuprizzcrm.stats;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.campaign.CampaignCreateRequest;
import com.sanghee.glowuprizzcrm.admin.template.HtmlTemplateCreateRequest;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.submission.Submission;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import com.sanghee.glowuprizzcrm.core.visit.Visit;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("/admin/campaigns/{campaignId}/stats 통합 테스트")
class CampaignStatsControllerIntegrationTest extends AbstractAdminIntegrationTest {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private long createCampaign(String token) throws Exception {
        HtmlTemplateCreateRequest templateRequest = new HtmlTemplateCreateRequest("성과 테스트용 템플릿", "<html></html>");
        MvcResult templateResult = mockMvc.perform(post("/admin/html-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andReturn();
        long templateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
                .path("id").asLong();

        CampaignCreateRequest campaignRequest = new CampaignCreateRequest(templateId, "성과 테스트 캠페인", "stats-test-campaign");
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
        mockMvc.perform(get("/admin/campaigns/1/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 캠페인을 조회하면 404와 CAMPAIGN_NOT_FOUND를 반환한다")
    void get_returns404_whenCampaignNotFound() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(get("/admin/campaigns/999999/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("방문/방문자/신청 데이터를 집계해 전체 전환율과 채널별 성과를 반환한다")
    void get_returnsAggregatedStats() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token);

        // INSTAGRAM: 같은 방문자가 두 번 방문(v1), 1번 신청 -> visit=2, visitor=1, submission=1
        visitRepository.save(new Visit(campaignId, null, Channel.INSTAGRAM, "v1"));
        visitRepository.save(new Visit(campaignId, null, Channel.INSTAGRAM, "v1"));
        submissionRepository.save(new Submission(campaignId, null, Channel.INSTAGRAM, "v1", "{\"name\":\"a\"}"));

        // X: 방문자(v2) 1번 방문, 1번 신청 -> visit=1, visitor=1, submission=1
        visitRepository.save(new Visit(campaignId, null, Channel.X, "v2"));
        submissionRepository.save(new Submission(campaignId, null, Channel.X, "v2", "{\"name\":\"b\"}"));

        // 채널 없는 직접 방문(v3): 링크 경유가 아닌 /f/{slug} 직접 접근
        visitRepository.save(new Visit(campaignId, null, null, "v3"));

        // 캠페인 전체: visit=4, visitor=3(v1,v2,v3), submission=2, conversionRate=2/3
        mockMvc.perform(get("/admin/campaigns/" + campaignId + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId").value(campaignId))
                .andExpect(jsonPath("$.visitCount").value(4))
                .andExpect(jsonPath("$.visitorCount").value(3))
                .andExpect(jsonPath("$.submissionCount").value(2))
                .andExpect(jsonPath("$.conversionRate").value(org.hamcrest.Matchers.closeTo(0.6667, 0.001)))
                .andExpect(jsonPath("$.channels.length()").value(2))
                .andExpect(jsonPath("$.channels[?(@.channel == 'INSTAGRAM')].visitCount").value(2))
                .andExpect(jsonPath("$.channels[?(@.channel == 'INSTAGRAM')].visitorCount").value(1))
                .andExpect(jsonPath("$.channels[?(@.channel == 'INSTAGRAM')].submissionCount").value(1))
                .andExpect(jsonPath("$.channels[?(@.channel == 'INSTAGRAM')].conversionRate").value(1.0))
                .andExpect(jsonPath("$.channels[?(@.channel == 'X')].visitCount").value(1))
                .andExpect(jsonPath("$.channels[?(@.channel == 'X')].submissionCount").value(1));
    }

    @Test
    @DisplayName("방문/신청이 전혀 없는 캠페인은 0과 전환율 0을 반환한다")
    void get_returnsZeroStats_whenNoData() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token);

        mockMvc.perform(get("/admin/campaigns/" + campaignId + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitCount").value(0))
                .andExpect(jsonPath("$.visitorCount").value(0))
                .andExpect(jsonPath("$.submissionCount").value(0))
                .andExpect(jsonPath("$.conversionRate").value(0.0))
                .andExpect(jsonPath("$.channels.length()").value(0));
    }
}
