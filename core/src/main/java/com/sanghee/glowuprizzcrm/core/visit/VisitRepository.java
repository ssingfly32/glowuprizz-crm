package com.sanghee.glowuprizzcrm.core.visit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    long countByCampaignId(Long campaignId);

    @Query("SELECT COUNT(DISTINCT v.visitorToken) FROM Visit v WHERE v.campaignId = :campaignId")
    long countDistinctVisitorTokenByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT v.channel AS channel, COUNT(v) AS visitCount, "
            + "COUNT(DISTINCT v.visitorToken) AS visitorCount "
            + "FROM Visit v WHERE v.campaignId = :campaignId AND v.channel IS NOT NULL "
            + "GROUP BY v.channel")
    List<ChannelVisitStats> countByCampaignIdGroupByChannel(@Param("campaignId") Long campaignId);

    @Query("SELECT v.channel AS channel, COUNT(v) AS visitCount, "
            + "COUNT(DISTINCT v.visitorToken) AS visitorCount "
            + "FROM Visit v WHERE v.channel IS NOT NULL "
            + "GROUP BY v.channel")
    List<ChannelVisitStats> countGroupByChannel();
}
