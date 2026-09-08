package com.sanghee.glowuprizzcrm.admin.link;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import java.time.Instant;

public record DistributionLinkResponse(
        Long id,
        Long campaignId,
        Channel channel,
        String linkToken,
        Instant createdAt) {

    public static DistributionLinkResponse from(DistributionLink link) {
        return new DistributionLinkResponse(
                link.getId(), link.getCampaignId(), link.getChannel(), link.getLinkToken(), link.getCreatedAt());
    }
}
