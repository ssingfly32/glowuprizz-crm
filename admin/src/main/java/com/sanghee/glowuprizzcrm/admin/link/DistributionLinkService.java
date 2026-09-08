package com.sanghee.glowuprizzcrm.admin.link;

import com.sanghee.glowuprizzcrm.admin.campaign.CampaignService;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import com.sanghee.glowuprizzcrm.core.link.DistributionLinkRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 캠페인 x 채널 조합 개수 제한 없음 (create-only). docs/adr/0005 참고.
@Service
@Transactional(readOnly = true)
public class DistributionLinkService {

    private final DistributionLinkRepository distributionLinkRepository;
    private final CampaignService campaignService;

    public DistributionLinkService(
            DistributionLinkRepository distributionLinkRepository, CampaignService campaignService) {
        this.distributionLinkRepository = distributionLinkRepository;
        this.campaignService = campaignService;
    }

    @Transactional
    public DistributionLink create(Long campaignId, Channel channel) {
        campaignService.getOrThrow(campaignId);
        String linkToken = UUID.randomUUID().toString();
        DistributionLink link = new DistributionLink(campaignId, channel, linkToken);
        return distributionLinkRepository.save(link);
    }

    public List<DistributionLink> findByCampaignId(Long campaignId) {
        campaignService.getOrThrow(campaignId);
        return distributionLinkRepository.findByCampaignId(campaignId);
    }

    public DistributionLink getOrThrow(Long id) {
        return distributionLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LINK_NOT_FOUND));
    }
}
