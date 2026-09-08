package com.sanghee.glowuprizzcrm.admin.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CampaignCreateRequest(
        @NotNull Long htmlTemplateId,
        @NotBlank String name,
        @NotBlank String publicSlug) {
}
