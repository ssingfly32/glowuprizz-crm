package com.sanghee.glowuprizzcrm.publicform.form;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.publicform.campaign.CampaignLookupService;
import com.sanghee.glowuprizzcrm.publicform.visit.VisitService;
import com.sanghee.glowuprizzcrm.publicform.visitor.VisitorTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FormController {

    private final CampaignLookupService campaignLookupService;
    private final FormRenderService formRenderService;
    private final VisitService visitService;
    private final VisitorTokenResolver visitorTokenResolver;

    public FormController(
            CampaignLookupService campaignLookupService,
            FormRenderService formRenderService,
            VisitService visitService,
            VisitorTokenResolver visitorTokenResolver) {
        this.campaignLookupService = campaignLookupService;
        this.formRenderService = formRenderService;
        this.visitService = visitService;
        this.visitorTokenResolver = visitorTokenResolver;
    }

    // link 파라미터가 있으면 /r/{token} 리다이렉트를 거쳐 온 것이라 방문이 이미 기록됐다.
    // 없으면(직접 접근) 여기서 채널 없이 방문을 기록한다 (docs/adr/0007).
    @GetMapping(value = "/f/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public String getForm(
            @PathVariable String slug,
            @RequestParam(required = false) String link,
            HttpServletRequest request,
            HttpServletResponse response) {
        Campaign campaign = campaignLookupService.getPublishedBySlug(slug);
        String visitorToken = visitorTokenResolver.resolve(request, response);
        if (link == null) {
            visitService.record(campaign.getId(), null, null, visitorToken);
        }
        return formRenderService.render(campaign, link);
    }
}
