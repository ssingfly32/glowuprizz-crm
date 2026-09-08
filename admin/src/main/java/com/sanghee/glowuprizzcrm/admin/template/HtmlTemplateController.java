package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/admin/html-templates")
public class HtmlTemplateController {

    private final HtmlTemplateService htmlTemplateService;

    public HtmlTemplateController(HtmlTemplateService htmlTemplateService) {
        this.htmlTemplateService = htmlTemplateService;
    }

    @PostMapping
    public ResponseEntity<HtmlTemplateResponse> register(
            @AuthenticationPrincipal Long operatorId,
            @Valid @RequestBody HtmlTemplateCreateRequest request) {
        HtmlTemplate template = htmlTemplateService.register(operatorId, request.name(), request.content());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(template.getId())
                .toUri();
        return ResponseEntity.created(location).body(HtmlTemplateResponse.from(template));
    }

    @GetMapping
    public List<HtmlTemplateResponse> list() {
        return htmlTemplateService.findAll().stream().map(HtmlTemplateResponse::from).toList();
    }

    @GetMapping("/{id}")
    public HtmlTemplateDetailResponse get(@PathVariable Long id) {
        return HtmlTemplateDetailResponse.from(htmlTemplateService.getOrThrow(id));
    }
}
