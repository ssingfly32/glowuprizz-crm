package com.sanghee.glowuprizzcrm.admin.stats;

import java.util.List;

public record CampaignStatsResponse(
        Long campaignId,
        long visitCount,
        long visitorCount,
        long submissionCount,
        double conversionRate,
        List<ChannelStatsResponse> channels) {
}
