package com.sanghee.glowuprizzcrm.admin.stats;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.submission.ChannelSubmissionStats;
import com.sanghee.glowuprizzcrm.core.visit.ChannelVisitStats;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 방문/신청 중 한쪽에만 채널 값이 존재할 수 있으므로 두 집계 결과의 채널 합집합을 기준으로
// 병합한다. 캠페인 스코프(CampaignStatsService)와 전역 스코프(ChannelStatsService) 양쪽에서
// 공유한다.
final class ChannelStatsMerger {

    private static final long[] ZERO_VISIT_STATS = {0L, 0L};

    private ChannelStatsMerger() {
    }

    static List<ChannelStatsResponse> merge(
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
