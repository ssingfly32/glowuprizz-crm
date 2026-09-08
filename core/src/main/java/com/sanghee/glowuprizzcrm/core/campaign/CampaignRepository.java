package com.sanghee.glowuprizzcrm.core.campaign;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByPublicSlug(String publicSlug);
}
