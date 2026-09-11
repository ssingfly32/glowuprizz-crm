package com.sanghee.glowuprizzcrm.admin.submission;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/campaigns/{campaignId}/submissions")
public class SubmissionController {

    private final SubmissionQueryService submissionQueryService;

    public SubmissionController(SubmissionQueryService submissionQueryService) {
        this.submissionQueryService = submissionQueryService;
    }

    @GetMapping
    public List<SubmissionResponse> list(@PathVariable Long campaignId) {
        return submissionQueryService.findByCampaignId(campaignId);
    }
}
