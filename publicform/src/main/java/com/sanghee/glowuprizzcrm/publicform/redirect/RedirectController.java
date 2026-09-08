package com.sanghee.glowuprizzcrm.publicform.redirect;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import com.sanghee.glowuprizzcrm.publicform.campaign.CampaignLookupService;
import com.sanghee.glowuprizzcrm.publicform.link.DistributionLinkLookupService;
import com.sanghee.glowuprizzcrm.publicform.visit.VisitService;
import com.sanghee.glowuprizzcrm.publicform.visitor.VisitorTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

// 배포 링크 클릭을 실제 폼(/f/{slug})으로 안내하기 전에 방문을 기록한다.
// 실제 폼 URL과 분리된 트래킹 링크로 설계한 이유는 docs/adr/0007 참고.
@RestController
public class RedirectController {

    private final DistributionLinkLookupService distributionLinkLookupService;
    private final CampaignLookupService campaignLookupService;
    private final VisitService visitService;
    private final VisitorTokenResolver visitorTokenResolver;

    public RedirectController(
            DistributionLinkLookupService distributionLinkLookupService,
            CampaignLookupService campaignLookupService,
            VisitService visitService,
            VisitorTokenResolver visitorTokenResolver) {
        this.distributionLinkLookupService = distributionLinkLookupService;
        this.campaignLookupService = campaignLookupService;
        this.visitService = visitService;
        this.visitorTokenResolver = visitorTokenResolver;
    }

    @GetMapping("/r/{token}")
    public ResponseEntity<Void> redirect(
            @PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        DistributionLink link = distributionLinkLookupService.getByToken(token);
        Campaign campaign = campaignLookupService.getPublishedById(link.getCampaignId());
        String visitorToken = visitorTokenResolver.resolve(request, response);
        visitService.record(campaign.getId(), link.getId(), link.getChannel(), visitorToken);

        URI location = UriComponentsBuilder.fromPath("/f/{slug}")
                .queryParam("link", link.getLinkToken())
                .buildAndExpand(campaign.getPublicSlug())
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }
}
