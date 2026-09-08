package com.sanghee.glowuprizzcrm.admin.link;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import jakarta.validation.constraints.NotNull;

public record DistributionLinkCreateRequest(@NotNull Channel channel) {
}
