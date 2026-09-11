package com.sanghee.glowuprizzcrm.admin.campaign;

import com.sanghee.glowuprizzcrm.admin.template.HtmlTemplateService;
import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.campaign.CampaignRepository;
import com.sanghee.glowuprizzcrm.core.common.exception.BusinessException;
import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
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
        // 위 findByPublicSlug 조회와 save 사이는 원자적이지 않다. 동시에 같은 publicSlug로
        // 요청이 들어오면 둘 다 조회를 통과할 수 있어, DB 유니크 제약(V1__init.sql)이 최종
        // 방어선이 된다. 그 위반을 여기서 잡아 같은 에러 코드로 변환하지 않으면 예상 못한
        // 예외로 분류돼 500이 나간다 (AdminExceptionHandler 참고).
        try {
            return campaignRepository.save(campaign);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_PUBLIC_SLUG);
        }
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
