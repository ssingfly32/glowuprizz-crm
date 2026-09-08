package com.sanghee.glowuprizzcrm.publicform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractPublicIntegrationTest;
import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GET /r/{token} 통합 테스트")
class RedirectControllerIntegrationTest extends AbstractPublicIntegrationTest {

    @Test
    @DisplayName("공개된 캠페인의 링크면 /f/{slug}로 리다이렉트하고 방문을 기록한다")
    void redirect_toPublicForm_andRecordsVisit_whenLinkExists() throws Exception {
        Campaign campaign = createPublishedCampaign("redirect-success", "<html><body><form></form></body></html>");
        DistributionLink link = createLink(campaign, Channel.INSTAGRAM);

        mockMvc.perform(get("/r/" + link.getLinkToken()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/f/redirect-success")))
                .andExpect(header().string("Location", Matchers.containsString("link=" + link.getLinkToken())))
                .andExpect(cookie().exists("visitor_token"));

        assertThat(visitRepository.findAll())
                .anySatisfy(visit -> {
                    assertThat(visit.getCampaignId()).isEqualTo(campaign.getId());
                    assertThat(visit.getDistributionLinkId()).isEqualTo(link.getId());
                    assertThat(visit.getChannel()).isEqualTo(Channel.INSTAGRAM);
                });
    }

    @Test
    @DisplayName("존재하지 않는 토큰이면 404와 LINK_NOT_FOUND를 반환한다")
    void redirect_returns404_whenTokenNotFound() throws Exception {
        mockMvc.perform(get("/r/no-such-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LINK_NOT_FOUND"));
    }

    @Test
    @DisplayName("미공개 캠페인의 링크면 404와 CAMPAIGN_NOT_PUBLISHED를 반환한다")
    void redirect_returns404_whenCampaignNotPublished() throws Exception {
        Campaign campaign = createPublishedCampaign("redirect-unpublished", "<html></html>");
        campaign.unpublish();
        campaignRepository.save(campaign);
        DistributionLink link = createLink(campaign, Channel.X);

        mockMvc.perform(get("/r/" + link.getLinkToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_PUBLISHED"));
    }
}
