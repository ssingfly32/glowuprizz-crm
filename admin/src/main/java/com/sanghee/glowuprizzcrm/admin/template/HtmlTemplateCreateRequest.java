package com.sanghee.glowuprizzcrm.admin.template;

import jakarta.validation.constraints.NotBlank;

public record HtmlTemplateCreateRequest(@NotBlank String name, @NotBlank String content) {
}
