package com.sanghee.glowuprizzcrm.core.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Submission 도메인 단위 테스트")
class SubmissionTest {

    @Test
    @DisplayName("방문자 토큰/데이터가 유효하면 생성된다")
    void create_success() {
        Submission submission = new Submission(1L, 10L, Channel.YOUTUBE, "visitor-token", "{\"email\":\"a@b.com\"}");

        assertThat(submission.getCampaignId()).isEqualTo(1L);
        assertThat(submission.getChannel()).isEqualTo(Channel.YOUTUBE);
        assertThat(submission.getData()).isEqualTo("{\"email\":\"a@b.com\"}");
    }

    @Test
    @DisplayName("방문자 토큰이 비어있으면 예외가 발생한다")
    void create_throws_whenVisitorTokenBlank() {
        assertThatThrownBy(() -> new Submission(1L, null, null, " ", "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_VISITOR_TOKEN);
    }

    @Test
    @DisplayName("데이터가 비어있으면 예외가 발생한다")
    void create_throws_whenDataBlank() {
        assertThatThrownBy(() -> new Submission(1L, null, null, "visitor-token", " "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SUBMISSION_DATA);
    }
}
