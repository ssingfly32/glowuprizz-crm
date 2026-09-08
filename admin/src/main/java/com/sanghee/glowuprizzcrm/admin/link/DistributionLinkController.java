package com.sanghee.glowuprizzcrm.admin.link;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/admin/campaigns/{campaignId}/links")
public class DistributionLinkController {

    private final DistributionLinkService distributionLinkService;

    public DistributionLinkController(DistributionLinkService distributionLinkService) {
        this.distributionLinkService = distributionLinkService;
    }

    @PostMapping
    public ResponseEntity<DistributionLinkResponse> create(
            @PathVariable Long campaignId, @Valid @RequestBody DistributionLinkCreateRequest request) {
        var link = distributionLinkService.create(campaignId, request.channel());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(link.getId())
                .toUri();
        return ResponseEntity.created(location).body(DistributionLinkResponse.from(link));
    }

    @GetMapping
    public List<DistributionLinkResponse> list(@PathVariable Long campaignId) {
        return distributionLinkService.findByCampaignId(campaignId).stream()
                .map(DistributionLinkResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DistributionLinkResponse get(@PathVariable Long campaignId, @PathVariable Long id) {
        return DistributionLinkResponse.from(distributionLinkService.getOrThrow(id));
    }
}
