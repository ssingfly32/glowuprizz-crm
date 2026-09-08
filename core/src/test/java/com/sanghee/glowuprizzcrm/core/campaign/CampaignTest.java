package com.sanghee.glowuprizzcrm.core.campaign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Campaign 도메인 단위 테스트")
class CampaignTest {

    @Test
    @DisplayName("이름/슬러그가 유효하면 미공개 상태로 생성된다")
    void create_success_asUnpublished() {
        Campaign campaign = new Campaign(1L, 1L, "가을 웨비나", "autumn-webinar");

        assertThat(campaign.getName()).isEqualTo("가을 웨비나");
        assertThat(campaign.getPublicSlug()).isEqualTo("autumn-webinar");
        assertThat(campaign.isPublished()).isFalse();
    }

    @Test
    @DisplayName("이름이 비어있으면 예외가 발생한다")
    void create_throws_whenNameBlank() {
        assertThatThrownBy(() -> new Campaign(1L, 1L, " ", "autumn-webinar"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CAMPAIGN_NAME);
    }

    @Test
    @DisplayName("슬러그에 대문자나 공백이 있으면 예외가 발생한다")
    void create_throws_whenSlugInvalid() {
        assertThatThrownBy(() -> new Campaign(1L, 1L, "가을 웨비나", "Invalid Slug!!"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PUBLIC_SLUG);
    }

    @Test
    @DisplayName("슬러그가 비어있으면 예외가 발생한다")
    void create_throws_whenSlugBlank() {
        assertThatThrownBy(() -> new Campaign(1L, 1L, "가을 웨비나", " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PUBLIC_SLUG);
    }

    @Test
    @DisplayName("publish()를 호출하면 공개 상태가 된다")
    void publish_setsPublishedTrue() {
        Campaign campaign = new Campaign(1L, 1L, "가을 웨비나", "autumn-webinar");

        campaign.publish();

        assertThat(campaign.isPublished()).isTrue();
    }

    @Test
    @DisplayName("unpublish()를 호출하면 비공개 상태가 된다")
    void unpublish_setsPublishedFalse() {
        Campaign campaign = new Campaign(1L, 1L, "가을 웨비나", "autumn-webinar");
        campaign.publish();

        campaign.unpublish();

        assertThat(campaign.isPublished()).isFalse();
    }
}
