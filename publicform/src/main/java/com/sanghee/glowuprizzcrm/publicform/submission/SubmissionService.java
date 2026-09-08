package com.sanghee.glowuprizzcrm.publicform.submission;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.submission.Submission;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

// data는 스키마리스 JSON 문자열로 저장한다 (docs/adr/0003). 직렬화는 이 서비스의 책임이다.
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    public SubmissionService(SubmissionRepository submissionRepository, ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(
            Long campaignId, Long distributionLinkId, Channel channel, String visitorToken, Map<String, Object> data) {
        String json = objectMapper.writeValueAsString(data);
        submissionRepository.save(new Submission(campaignId, distributionLinkId, channel, visitorToken, json));
    }
}
