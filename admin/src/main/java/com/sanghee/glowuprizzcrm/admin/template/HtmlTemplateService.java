package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HtmlTemplateService {

    // 대소문자 구분 없이 .html로 끝나는지만 본다. 검증 대상은 원본 파일명이며, 그 이유는
    // docs/adr/0016 참고 (Content-Type 파트 헤더는 신뢰성이 낮아 기준으로 쓰지 않는다).
    private static final String HTML_FILENAME_PATTERN = "(?i)^.*\\.html$";

    private final HtmlTemplateRepository htmlTemplateRepository;

    public HtmlTemplateService(HtmlTemplateRepository htmlTemplateRepository) {
        this.htmlTemplateRepository = htmlTemplateRepository;
    }

    // filename은 엔티티에 저장되지 않는 검증 전용 입력이라 엔티티 생성자가 아니라
    // 여기서 검증한다.
    @Transactional
    public HtmlTemplate register(Long operatorId, String name, String originalFilename, String content) {
        Assert.notBlank(originalFilename, ErrorCode.INVALID_TEMPLATE_FILE_EXTENSION);
        Assert.matches(originalFilename, HTML_FILENAME_PATTERN, ErrorCode.INVALID_TEMPLATE_FILE_EXTENSION);
        HtmlTemplate template = new HtmlTemplate(operatorId, name, content);
        return htmlTemplateRepository.save(template);
    }

    public List<HtmlTemplate> findAll() {
        return htmlTemplateRepository.findAll();
    }

    public HtmlTemplate getOrThrow(Long id) {
        return htmlTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
    }
}
