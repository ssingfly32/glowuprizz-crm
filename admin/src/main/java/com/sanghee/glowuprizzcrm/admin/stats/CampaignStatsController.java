package com.sanghee.glowuprizzcrm.admin.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/campaigns/{campaignId}/stats")
public class CampaignStatsController {

    private final CampaignStatsService campaignStatsService;

    public CampaignStatsController(CampaignStatsService campaignStatsService) {
        this.campaignStatsService = campaignStatsService;
    }

    @GetMapping
    public CampaignStatsResponse get(@PathVariable Long campaignId) {
        return campaignStatsService.getStats(campaignId);
    }
}
