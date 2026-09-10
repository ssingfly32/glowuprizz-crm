package com.sanghee.glowuprizzcrm.publicform.form;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        // linkToken은 방문자가 조작 가능한 쿼리파라미터 값이라, 인라인 <script>의 문자열
        // 리터럴에 삽입되기 전에 반드시 인코딩해야 한다. 인코딩 없이 넣으면 홑따옴표로 문자열을
        // 끊고 임의 JS를 실행시키는 반사형 XSS가 된다 (docs/adr/0015 참고).
        String submitUrl = "/f/" + campaign.getPublicSlug() + "/submissions"
                + (linkToken != null ? "?link=" + URLEncoder.encode(linkToken, StandardCharsets.UTF_8) : "");
        return submissionScriptInjector.inject(template.getContent(), submitUrl);
    }
}
