package com.sanghee.glowuprizzcrm.admin.stats;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/channels/stats")
public class ChannelStatsController {

    private final ChannelStatsService channelStatsService;

    public ChannelStatsController(ChannelStatsService channelStatsService) {
        this.channelStatsService = channelStatsService;
    }

    @GetMapping
    public List<ChannelStatsResponse> get() {
        return channelStatsService.getStats();
    }
}
