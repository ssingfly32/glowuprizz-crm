package com.sanghee.glowuprizzcrm.core.visit;

import com.sanghee.glowuprizzcrm.core.link.Channel;

public interface ChannelVisitStats {

    Channel getChannel();

    long getVisitCount();

    long getVisitorCount();
}
