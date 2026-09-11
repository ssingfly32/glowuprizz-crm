package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// 운영자가 AI로 만든 완성된 .html 파일을 그대로 업로드해 등록한다.
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
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        HtmlTemplate template = htmlTemplateService.register(operatorId, name, file.getOriginalFilename(), content);
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
