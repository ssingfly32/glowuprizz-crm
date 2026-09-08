package com.sanghee.glowuprizzcrm.admin.template;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HtmlTemplateService {

    private final HtmlTemplateRepository htmlTemplateRepository;

    public HtmlTemplateService(HtmlTemplateRepository htmlTemplateRepository) {
        this.htmlTemplateRepository = htmlTemplateRepository;
    }

    @Transactional
    public HtmlTemplate register(Long operatorId, String name, String content) {
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
