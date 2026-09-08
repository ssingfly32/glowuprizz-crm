package com.sanghee.glowuprizzcrm.publicform.visit;

import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.visit.Visit;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitService {

    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Transactional
    public void record(Long campaignId, Long distributionLinkId, Channel channel, String visitorToken) {
        visitRepository.save(new Visit(campaignId, distributionLinkId, channel, visitorToken));
    }
}
