package com.sanghee.glowuprizzcrm.admin.stats;

import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChannelStatsService {

    private final VisitRepository visitRepository;
    private final SubmissionRepository submissionRepository;

    public ChannelStatsService(VisitRepository visitRepository, SubmissionRepository submissionRepository) {
        this.visitRepository = visitRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<ChannelStatsResponse> getStats() {
        return ChannelStatsMerger.merge(
                visitRepository.countGroupByChannel(),
                submissionRepository.countGroupByChannel());
    }
}
