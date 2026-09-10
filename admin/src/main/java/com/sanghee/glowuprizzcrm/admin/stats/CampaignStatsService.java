package com.sanghee.glowuprizzcrm.admin.stats;

import com.sanghee.glowuprizzcrm.admin.campaign.CampaignService;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.submission.ChannelSubmissionStats;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import com.sanghee.glowuprizzcrm.core.visit.ChannelVisitStats;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CampaignStatsService {

    private static final long[] ZERO_VISIT_STATS = {0L, 0L};

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

        var channels = mergeChannelStats(
                visitRepository.countByCampaignIdGroupByChannel(campaignId),
                submissionRepository.countByCampaignIdGroupByChannel(campaignId));

        return new CampaignStatsResponse(campaignId, visitCount, visitorCount, submissionCount, conversionRate, channels);
    }

    // 채널별 성과는 캠페인 범위로 스코프한다 (docs/adr/0013 참고). 방문/신청 중 한쪽에만
    // 채널 값이 존재할 수 있으므로 두 집계 결과의 채널 합집합을 기준으로 병합한다.
    private List<ChannelStatsResponse> mergeChannelStats(
            List<ChannelVisitStats> visitStats, List<ChannelSubmissionStats> submissionStats) {
        Map<Channel, long[]> visitByChannel = visitStats.stream()
                .collect(Collectors.toMap(
                        ChannelVisitStats::getChannel,
                        s -> new long[] {s.getVisitCount(), s.getVisitorCount()}));
        Map<Channel, Long> submissionByChannel = submissionStats.stream()
                .collect(Collectors.toMap(ChannelSubmissionStats::getChannel, ChannelSubmissionStats::getSubmissionCount));

        Set<Channel> channels = new LinkedHashSet<>();
        channels.addAll(visitByChannel.keySet());
        channels.addAll(submissionByChannel.keySet());

        return channels.stream()
                .map(channel -> {
                    long[] visit = visitByChannel.getOrDefault(channel, ZERO_VISIT_STATS);
                    long submissionCount = submissionByChannel.getOrDefault(channel, 0L);
                    double conversionRate = ConversionRateCalculator.calculate(submissionCount, visit[1]);
                    return new ChannelStatsResponse(channel, visit[0], visit[1], submissionCount, conversionRate);
                })
                .sorted(Comparator.comparing(ChannelStatsResponse::channel))
                .toList();
    }
}
