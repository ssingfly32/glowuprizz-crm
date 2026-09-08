package com.sanghee.glowuprizzcrm.publicform.submission;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import com.sanghee.glowuprizzcrm.publicform.campaign.CampaignLookupService;
import com.sanghee.glowuprizzcrm.publicform.link.DistributionLinkLookupService;
import com.sanghee.glowuprizzcrm.publicform.visitor.VisitorTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {

    private final CampaignLookupService campaignLookupService;
    private final DistributionLinkLookupService distributionLinkLookupService;
    private final SubmissionService submissionService;
    private final VisitorTokenResolver visitorTokenResolver;

    public SubmissionController(
            CampaignLookupService campaignLookupService,
            DistributionLinkLookupService distributionLinkLookupService,
            SubmissionService submissionService,
            VisitorTokenResolver visitorTokenResolver) {
        this.campaignLookupService = campaignLookupService;
        this.distributionLinkLookupService = distributionLinkLookupService;
        this.submissionService = submissionService;
        this.visitorTokenResolver = visitorTokenResolver;
    }

    @PostMapping("/f/{slug}/submissions")
    public ResponseEntity<Void> submit(
            @PathVariable String slug,
            @RequestParam(required = false) String link,
            @RequestBody Map<String, Object> data,
            HttpServletRequest request,
            HttpServletResponse response) {
        Campaign campaign = campaignLookupService.getPublishedBySlug(slug);
        String visitorToken = visitorTokenResolver.resolve(request, response);

        Long distributionLinkId = null;
        Channel channel = null;
        if (link != null) {
            DistributionLink distributionLink = distributionLinkLookupService.getByToken(link);
            distributionLinkId = distributionLink.getId();
            channel = distributionLink.getChannel();
        }

        submissionService.record(campaign.getId(), distributionLinkId, channel, visitorToken, data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
