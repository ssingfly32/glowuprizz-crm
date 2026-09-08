package com.sanghee.glowuprizzcrm.core.link;

import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// 캠페인 x 채널 조합의 개수 제한을 두지 않는다 (create-only, 재발급/갱신 없음).
// 이유는 docs/adr/0005-channel-link-cardinality.md 참고.
@Entity
@Table(name = "distribution_links")
public class DistributionLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campaignId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false, unique = true)
    private String linkToken;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected DistributionLink() {
    }

    public DistributionLink(Long campaignId, Channel channel, String linkToken) {
        Assert.notBlank(linkToken, ErrorCode.INVALID_LINK_TOKEN);
        this.campaignId = campaignId;
        this.channel = channel;
        this.linkToken = linkToken;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getLinkToken() {
        return linkToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
