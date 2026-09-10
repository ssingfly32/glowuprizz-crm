package com.sanghee.glowuprizzcrm.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractPublicIntegrationTest;
import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// PublicExceptionHandler가 BusinessException 외의 예상 못한 예외(예: 깨진 JSON 바디)를
// 잡지 못하면 Spring 기본 에러 포맷으로 새어나가 API의 {code,message} 계약이 깨진다.
@DisplayName("PublicExceptionHandler fallback 통합 테스트")
class PublicExceptionHandlerIntegrationTest extends AbstractPublicIntegrationTest {

    @Test
    @DisplayName("예상 못한 예외가 발생해도 500과 {code,message} 형식(INTERNAL_ERROR)을 반환한다")
    void unexpectedException_returns500_withErrorResponseFormat() throws Exception {
        Campaign campaign = createPublishedCampaign("exception-fallback", "<html></html>");

        mockMvc.perform(post("/f/" + campaign.getPublicSlug() + "/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
