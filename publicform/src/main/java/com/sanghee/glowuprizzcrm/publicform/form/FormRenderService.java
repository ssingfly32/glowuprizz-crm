package com.sanghee.glowuprizzcrm.publicform.form;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 캠페인에 연결된 HTML 템플릿을 찾아, 제출 스크립트를 주입한 뒤 반환한다.
@Service
@Transactional(readOnly = true)
public class FormRenderService {

    private final HtmlTemplateRepository htmlTemplateRepository;
    private final SubmissionScriptInjector submissionScriptInjector;

    public FormRenderService(
            HtmlTemplateRepository htmlTemplateRepository, SubmissionScriptInjector submissionScriptInjector) {
        this.htmlTemplateRepository = htmlTemplateRepository;
        this.submissionScriptInjector = submissionScriptInjector;
    }

    public String render(Campaign campaign, String linkToken) {
        HtmlTemplate template = htmlTemplateRepository.findById(campaign.getHtmlTemplateId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
        String submitUrl = "/f/" + campaign.getPublicSlug() + "/submissions"
                + (linkToken != null ? "?link=" + linkToken : "");
        return submissionScriptInjector.inject(template.getContent(), submitUrl);
    }
}
