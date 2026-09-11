package com.sanghee.glowuprizzcrm.admin.stats;

import com.sanghee.glowuprizzcrm.admin.campaign.CampaignService;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CampaignStatsService {

    private final CampaignService campaignService;
    private final VisitRepository visitRepository;
    private final SubmissionRepository submissionRepository;

    public CampaignStatsService(
            CampaignService campaignService,
            VisitRepository visitRepository,
            SubmissionRepository submissionRepository) {
        this.campaignService = campaignService;
        this.visitRepository = visitRepository;
        this.submissionRepository = submissionRepository;
    }

    public CampaignStatsResponse getStats(Long campaignId) {
        campaignService.getOrThrow(campaignId);

        long visitCount = visitRepository.countByCampaignId(campaignId);
        long visitorCount = visitRepository.countDistinctVisitorTokenByCampaignId(campaignId);
        long submissionCount = submissionRepository.countByCampaignId(campaignId);
        double conversionRate = ConversionRateCalculator.calculate(submissionCount, visitorCount);

        List<ChannelStatsResponse> channels = ChannelStatsMerger.merge(
                visitRepository.countByCampaignIdGroupByChannel(campaignId),
                submissionRepository.countByCampaignIdGroupByChannel(campaignId));

        return new CampaignStatsResponse(campaignId, visitCount, visitorCount, submissionCount, conversionRate, channels);
    }
}
