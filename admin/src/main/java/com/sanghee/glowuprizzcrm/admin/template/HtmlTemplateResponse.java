package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import java.time.Instant;

public record HtmlTemplateResponse(Long id, String name, Instant createdAt) {

    public static HtmlTemplateResponse from(HtmlTemplate template) {
        return new HtmlTemplateResponse(template.getId(), template.getName(), template.getCreatedAt());
    }
}
