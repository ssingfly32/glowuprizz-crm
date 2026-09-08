package com.sanghee.glowuprizzcrm;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import com.sanghee.glowuprizzcrm.core.campaign.CampaignRepository;
import com.sanghee.glowuprizzcrm.core.link.Channel;
import com.sanghee.glowuprizzcrm.core.link.DistributionLink;
import com.sanghee.glowuprizzcrm.core.link.DistributionLinkRepository;
import com.sanghee.glowuprizzcrm.core.operator.Operator;
import com.sanghee.glowuprizzcrm.core.operator.OperatorRepository;
import com.sanghee.glowuprizzcrm.core.submission.SubmissionRepository;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplate;
import com.sanghee.glowuprizzcrm.core.template.HtmlTemplateRepository;
import com.sanghee.glowuprizzcrm.core.visit.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

// public-bootstrap은 운영 환경에서는 Flyway를 돌리지 않는다(admin-bootstrap이 마이그레이션을
// 소유함, docs/adr/0011). 하지만 테스트는 매번 새 Testcontainers 컨테이너라 스키마가
// 없으므로, 테스트에서만 spring.flyway.enabled=true로 덮어써서 직접 마이그레이션한다.
//
// admin 모듈에 의존할 수 없으므로(publicform이 admin을 모르는 게 격리의 핵심), 테스트
// 픽스처(운영자/템플릿/캠페인/링크)는 admin API를 거치지 않고 core 리포지토리로 직접 만든다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.flyway.enabled=true")
@Transactional
public abstract class AbstractPublicIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected OperatorRepository operatorRepository;

    @Autowired
    protected HtmlTemplateRepository htmlTemplateRepository;

    @Autowired
    protected CampaignRepository campaignRepository;

    @Autowired
    protected DistributionLinkRepository distributionLinkRepository;

    @Autowired
    protected VisitRepository visitRepository;

    @Autowired
    protected SubmissionRepository submissionRepository;

    protected Campaign createPublishedCampaign(String publicSlug, String htmlContent) {
        Operator operator = operatorRepository.save(new Operator("fixture-" + publicSlug + "@glowuprizz.com", "hash"));
        HtmlTemplate template = htmlTemplateRepository.save(
                new HtmlTemplate(operator.getId(), "fixture-template", htmlContent));
        Campaign campaign = new Campaign(operator.getId(), template.getId(), "fixture-campaign", publicSlug);
        campaign.publish();
        return campaignRepository.save(campaign);
    }

    protected DistributionLink createLink(Campaign campaign, Channel channel) {
        return distributionLinkRepository.save(
                new DistributionLink(campaign.getId(), channel, "fixture-token-" + campaign.getId() + "-" + channel));
    }
}
