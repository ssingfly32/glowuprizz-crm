package com.sanghee.glowuprizzcrm.admin.campaign;

import com.sanghee.glowuprizzcrm.core.campaign.Campaign;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/admin/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> register(
            @AuthenticationPrincipal Long operatorId,
            @Valid @RequestBody CampaignCreateRequest request) {
        Campaign campaign = campaignService.register(
                operatorId, request.htmlTemplateId(), request.name(), request.publicSlug());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(campaign.getId())
                .toUri();
        return ResponseEntity.created(location).body(CampaignResponse.from(campaign));
    }

    @GetMapping
    public List<CampaignResponse> list() {
        return campaignService.findAll().stream().map(CampaignResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CampaignResponse get(@PathVariable Long id) {
        return CampaignResponse.from(campaignService.getOrThrow(id));
    }

    @PostMapping("/{id}/publish")
    public CampaignResponse publish(@PathVariable Long id) {
        return CampaignResponse.from(campaignService.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    public CampaignResponse unpublish(@PathVariable Long id) {
        return CampaignResponse.from(campaignService.unpublish(id));
    }
}
