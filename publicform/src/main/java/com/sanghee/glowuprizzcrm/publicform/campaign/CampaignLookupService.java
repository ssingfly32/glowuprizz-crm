package com.sanghee.glowuprizzcrm.publicform.campaign;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.campaign.CampaignRepository;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 방문자에게 노출되는 캠페인은 published=true인 것만 있어야 한다 (docs/adr/0007).
@Service
@Transactional(readOnly = true)
public class CampaignLookupService {

    private final CampaignRepository campaignRepository;

    public CampaignLookupService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public Campaign getPublishedBySlug(String slug) {
        return requirePublished(campaignRepository.findByPublicSlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND)));
    }

    public Campaign getPublishedById(Long id) {
        return requirePublished(campaignRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND)));
    }

    private Campaign requirePublished(Campaign campaign) {
        if (!campaign.isPublished()) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_PUBLISHED);
        }
        return campaign;
    }
}
