package com.sanghee.glowuprizzcrm.admin.submission;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

public record SubmissionResponse(
        Long id,
        Long campaignId,
        Long distributionLinkId,
        Channel channel,
        String visitorToken,
        JsonNode data,
        Instant submittedAt) {
}
