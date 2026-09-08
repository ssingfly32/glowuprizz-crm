package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import java.time.Instant;

public record HtmlTemplateDetailResponse(Long id, String name, String content, Instant createdAt) {

    public static HtmlTemplateDetailResponse from(HtmlTemplate template) {
        return new HtmlTemplateDetailResponse(
                template.getId(), template.getName(), template.getContent(), template.getCreatedAt());
    }
}
