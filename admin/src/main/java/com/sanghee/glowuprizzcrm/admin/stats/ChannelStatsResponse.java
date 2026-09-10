package com.sanghee.glowuprizzcrm.admin.stats;

import com.sanghee.glowuprizzcrm.core.link.Channel;

public record ChannelStatsResponse(
        Channel channel,
        long visitCount,
        long visitorCount,
        long submissionCount,
        double conversionRate) {
}
