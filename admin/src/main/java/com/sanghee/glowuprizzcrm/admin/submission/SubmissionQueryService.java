package com.sanghee.glowuprizzcrm.admin.submission;

import com.sanghee.glowuprizzcrm.admin.campaign.CampaignService;
import com.sanghee.glowuprizzcrm.core.submission.Submission;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

// 운영자가 캠페인별 신청자 명단(CRM 데이터)을 조회하는 서비스. 신청 데이터는 스키마리스
// JSON 문자열로 저장되어 있어(docs/adr/0003) 고정 DTO로 매핑하지 않고 JsonNode로
// 역직렬화해 그대로 반환한다.
@Service
@Transactional(readOnly = true)
public class SubmissionQueryService {

    private final SubmissionRepository submissionRepository;
    private final CampaignService campaignService;
    private final ObjectMapper objectMapper;

    public SubmissionQueryService(
            SubmissionRepository submissionRepository, CampaignService campaignService, ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.campaignService = campaignService;
        this.objectMapper = objectMapper;
    }

    public List<SubmissionResponse> findByCampaignId(Long campaignId) {
        campaignService.getOrThrow(campaignId);
        return submissionRepository.findByCampaignIdOrderBySubmittedAtDesc(campaignId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getCampaignId(),
                submission.getDistributionLinkId(),
                submission.getChannel(),
                submission.getVisitorToken(),
                objectMapper.readTree(submission.getData()),
                submission.getSubmittedAt());
    }
}
