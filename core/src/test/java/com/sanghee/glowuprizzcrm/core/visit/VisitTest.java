package com.sanghee.glowuprizzcrm.core.visit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Visit 도메인 단위 테스트")
class VisitTest {

    @Test
    @DisplayName("배포 링크를 통한 방문은 채널 정보와 함께 생성된다")
    void create_success_withDistributionLink() {
        Visit visit = new Visit(1L, 10L, Channel.INSTAGRAM, "visitor-token");

        assertThat(visit.getCampaignId()).isEqualTo(1L);
        assertThat(visit.getDistributionLinkId()).isEqualTo(10L);
        assertThat(visit.getChannel()).isEqualTo(Channel.INSTAGRAM);
    }

    @Test
    @DisplayName("링크 없이(직접 접근) 방문해도 채널 없이 생성된다")
    void create_success_withoutDistributionLink() {
        Visit visit = new Visit(1L, null, null, "visitor-token");

        assertThat(visit.getDistributionLinkId()).isNull();
        assertThat(visit.getChannel()).isNull();
    }

    @Test
    @DisplayName("방문자 토큰이 비어있으면 예외가 발생한다")
    void create_throws_whenVisitorTokenBlank() {
        assertThatThrownBy(() -> new Visit(1L, null, null, " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_VISITOR_TOKEN);
    }
}
