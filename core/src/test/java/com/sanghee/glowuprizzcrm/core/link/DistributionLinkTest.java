package com.sanghee.glowuprizzcrm.core.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DistributionLink 도메인 단위 테스트")
class DistributionLinkTest {

    @Test
    @DisplayName("채널/토큰이 유효하면 생성된다")
    void create_success() {
        DistributionLink link = new DistributionLink(1L, Channel.INSTAGRAM, "token-123");

        assertThat(link.getCampaignId()).isEqualTo(1L);
        assertThat(link.getChannel()).isEqualTo(Channel.INSTAGRAM);
        assertThat(link.getLinkToken()).isEqualTo("token-123");
    }

    @Test
    @DisplayName("링크 토큰이 비어있으면 예외가 발생한다")
    void create_throws_whenLinkTokenBlank() {
        assertThatThrownBy(() -> new DistributionLink(1L, Channel.X, " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_LINK_TOKEN);
    }
}
