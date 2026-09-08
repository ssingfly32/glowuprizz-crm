package com.sanghee.glowuprizzcrm.core.link;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionLinkRepository extends JpaRepository<DistributionLink, Long> {

    Optional<DistributionLink> findByLinkToken(String linkToken);

    List<DistributionLink> findByCampaignId(Long campaignId);
}
