package com.sanghee.glowuprizzcrm.core.visit;

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

// distributionLinkId/channel이 nullable인 이유: /f/{slug}로 직접 접근한 방문은
// 출처 링크가 없다 (docs/adr/0007-distribution-link-redirect.md 참고).
@Entity
@Table(name = "visits")
public class Visit {

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

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected Visit() {
    }

    public Visit(Long campaignId, Long distributionLinkId, Channel channel, String visitorToken) {
        Assert.notBlank(visitorToken, ErrorCode.INVALID_VISITOR_TOKEN);
        this.campaignId = campaignId;
        this.distributionLinkId = distributionLinkId;
        this.channel = channel;
        this.visitorToken = visitorToken;
        this.occurredAt = Instant.now();
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

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
