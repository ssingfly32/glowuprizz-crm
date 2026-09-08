package com.sanghee.glowuprizzcrm.admin.campaign;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import java.time.Instant;

public record CampaignResponse(
        Long id,
        Long htmlTemplateId,
        String name,
        String publicSlug,
        boolean published,
        Instant createdAt) {

    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getHtmlTemplateId(),
                campaign.getName(),
                campaign.getPublicSlug(),
                campaign.isPublished(),
                campaign.getCreatedAt());
    }
}
