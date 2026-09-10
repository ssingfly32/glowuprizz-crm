package com.sanghee.glowuprizzcrm.admin.campaign;

import com.sanghee.glowuprizzcrm.admin.template.HtmlTemplateService;
import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.campaign.CampaignRepository;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final HtmlTemplateService htmlTemplateService;

    public CampaignService(CampaignRepository campaignRepository, HtmlTemplateService htmlTemplateService) {
        this.campaignRepository = campaignRepository;
        this.htmlTemplateService = htmlTemplateService;
    }

    @Transactional
    public Campaign register(Long operatorId, Long htmlTemplateId, String name, String publicSlug) {
        htmlTemplateService.getOrThrow(htmlTemplateId);
        if (campaignRepository.findByPublicSlug(publicSlug).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PUBLIC_SLUG);
        }
        Campaign campaign = new Campaign(operatorId, htmlTemplateId, name, publicSlug);
        return campaignRepository.save(campaign);
    }

    public List<Campaign> findAll() {
        return campaignRepository.findAll();
    }

    public Campaign getOrThrow(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));
    }

    @Transactional
    public Campaign publish(Long id) {
        Campaign campaign = getOrThrow(id);
        campaign.publish();
        return campaign;
    }

    @Transactional
    public Campaign unpublish(Long id) {
        Campaign campaign = getOrThrow(id);
        campaign.unpublish();
        return campaign;
    }
}
