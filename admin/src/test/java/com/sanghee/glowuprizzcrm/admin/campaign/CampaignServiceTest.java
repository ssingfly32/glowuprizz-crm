package com.sanghee.glowuprizzcrm.admin.campaign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sanghee.glowuprizzcrm.admin.template.HtmlTemplateService;
import com.sanghee.glowuprizzcrm.core.campaign.CampaignRepository;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("CampaignService 단위 테스트")
class CampaignServiceTest {

    private final CampaignRepository campaignRepository = mock(CampaignRepository.class);
    private final HtmlTemplateService htmlTemplateService = mock(HtmlTemplateService.class);
    private final CampaignService campaignService = new CampaignService(campaignRepository, htmlTemplateService);

    // findByPublicSlug 사전 조회와 save 사이는 원자적이지 않다. 동시에 같은 publicSlug로
    // 요청이 들어오면 사전 조회는 둘 다 통과할 수 있고, DB 유니크 제약(V1__init.sql)이 최종
    // 방어선이 된다. 실제 동시 요청은 통합 테스트로 안정적으로 재현하기 어려워, save가 던지는
    // 제약 위반을 리포지토리 목으로 흉내낸다.
    @Test
    @DisplayName("사전 조회를 통과해도 save 시점에 유니크 제약 위반이 나면 DUPLICATE_PUBLIC_SLUG로 변환한다")
    void register_translatesConstraintViolation_toDuplicatePublicSlug() {
        when(htmlTemplateService.getOrThrow(1L)).thenReturn(mock(HtmlTemplate.class));
        when(campaignRepository.findByPublicSlug("race-slug")).thenReturn(Optional.empty());
        when(campaignRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> campaignService.register(1L, 1L, "가을 웨비나", "race-slug"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_PUBLIC_SLUG));
    }
}
