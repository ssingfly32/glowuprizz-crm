package com.sanghee.glowuprizzcrm.link;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.campaign.CampaignCreateRequest;
import com.sanghee.glowuprizzcrm.admin.link.DistributionLinkCreateRequest;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

@DisplayName("/admin/campaigns/{campaignId}/links 통합 테스트")
class DistributionLinkControllerIntegrationTest extends AbstractAdminIntegrationTest {

    private long createCampaign(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "template.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes(StandardCharsets.UTF_8));
        MvcResult templateResult = mockMvc.perform(multipart("/admin/html-templates")
                        .file(file)
                        .param("name", "링크 테스트용 템플릿")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        long templateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
                .path("id").asLong();

        CampaignCreateRequest campaignRequest = new CampaignCreateRequest(templateId, "링크 테스트 캠페인", "link-test-campaign");
        MvcResult campaignResult = mockMvc.perform(post("/admin/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campaignRequest)))
                .andReturn();
        return objectMapper.readTree(campaignResult.getResponse().getContentAsString()).path("id").asLong();
    }

    @Test
    @DisplayName("인증 없이 생성하면 401을 반환한다")
    void create_returns401_whenNoToken() throws Exception {
        DistributionLinkCreateRequest request = new DistributionLinkCreateRequest(Channel.INSTAGRAM);

        mockMvc.perform(post("/admin/campaigns/1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하는 캠페인에 채널을 지정해 생성하면 201과 linkToken을 반환한다")
    void create_returns201_whenCampaignExists() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token);
        DistributionLinkCreateRequest request = new DistributionLinkCreateRequest(Channel.INSTAGRAM);

        mockMvc.perform(post("/admin/campaigns/" + campaignId + "/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesRegex(".*/admin/campaigns/" + campaignId + "/links/\\d+")))
                .andExpect(jsonPath("$.channel").value("INSTAGRAM"))
                .andExpect(jsonPath("$.linkToken").isNotEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 캠페인에 생성하면 404와 CAMPAIGN_NOT_FOUND를 반환한다")
    void create_returns404_whenCampaignNotFound() throws Exception {
        String token = obtainAccessToken();
        DistributionLinkCreateRequest request = new DistributionLinkCreateRequest(Channel.X);

        mockMvc.perform(post("/admin/campaigns/999999/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    @DisplayName("같은 채널로 여러 번 생성해도 막지 않고 서로 다른 링크를 만든다")
    void create_allowsMultipleLinksPerChannel() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token);
        DistributionLinkCreateRequest request = new DistributionLinkCreateRequest(Channel.YOUTUBE);

        MvcResult first = mockMvc.perform(post("/admin/campaigns/" + campaignId + "/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult second = mockMvc.perform(post("/admin/campaigns/" + campaignId + "/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode firstJson = objectMapper.readTree(first.getResponse().getContentAsString());
        JsonNode secondJson = objectMapper.readTree(second.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(firstJson.path("linkToken").asString())
                .isNotEqualTo(secondJson.path("linkToken").asString());
    }

    @Test
    @DisplayName("생성한 배포 링크들을 목록으로 조회할 수 있다")
    void list_returnsCreatedLinks() throws Exception {
        String token = obtainAccessToken();
        long campaignId = createCampaign(token);
        DistributionLinkCreateRequest request = new DistributionLinkCreateRequest(Channel.THREADS);
        mockMvc.perform(post("/admin/campaigns/" + campaignId + "/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/admin/campaigns/" + campaignId + "/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.channel == 'THREADS')]").exists());
    }
}
