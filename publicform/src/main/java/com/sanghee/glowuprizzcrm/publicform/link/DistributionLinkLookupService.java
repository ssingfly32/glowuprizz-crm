package com.sanghee.glowuprizzcrm.publicform.link;

import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import com.sanghee.glowuprizzcrm.core.link.DistributionLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DistributionLinkLookupService {

    private final DistributionLinkRepository distributionLinkRepository;

    public DistributionLinkLookupService(DistributionLinkRepository distributionLinkRepository) {
        this.distributionLinkRepository = distributionLinkRepository;
    }

    public DistributionLink getByToken(String token) {
        return distributionLinkRepository.findByLinkToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.LINK_NOT_FOUND));
    }
}
