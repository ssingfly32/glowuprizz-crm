package com.sanghee.glowuprizzcrm.core.submission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    long countByCampaignId(Long campaignId);

    List<Submission> findByCampaignIdOrderBySubmittedAtDesc(Long campaignId);

    @Query("SELECT s.channel AS channel, COUNT(s) AS submissionCount "
            + "FROM Submission s WHERE s.campaignId = :campaignId AND s.channel IS NOT NULL "
            + "GROUP BY s.channel")
    List<ChannelSubmissionStats> countByCampaignIdGroupByChannel(@Param("campaignId") Long campaignId);

    @Query("SELECT s.channel AS channel, COUNT(s) AS submissionCount "
            + "FROM Submission s WHERE s.channel IS NOT NULL "
            + "GROUP BY s.channel")
    List<ChannelSubmissionStats> countGroupByChannel();
}
