package com.sanghee.glowuprizzcrm.core.submission;

import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// data는 스키마리스 JSON 문자열(jsonb 컬럼)로 저장한다. AI가 만드는 HTML마다
// 폼 필드명이 달라질 수 있어 고정 컬럼으로 모델링할 수 없다
// (docs/adr/0003-submission-data-json.md 참고). 직렬화/역직렬화는 application 레이어 책임.
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campaignId;

    @Column
    private Long distributionLinkId;

    @Enumerated(EnumType.STRING)
    @Column
    private Channel channel;

    @Column(nullable = false)
    private String visitorToken;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String data;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    protected Submission() {
    }

    public Submission(Long campaignId, Long distributionLinkId, Channel channel, String visitorToken, String data) {
        Assert.notBlank(visitorToken, ErrorCode.INVALID_VISITOR_TOKEN);
        Assert.notBlank(data, ErrorCode.INVALID_SUBMISSION_DATA);
        this.campaignId = campaignId;
        this.distributionLinkId = distributionLinkId;
        this.channel = channel;
        this.visitorToken = visitorToken;
        this.data = data;
        this.submittedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public Long getDistributionLinkId() {
        return distributionLinkId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getVisitorToken() {
        return visitorToken;
    }

    public String getData() {
        return data;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
