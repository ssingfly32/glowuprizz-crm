package com.sanghee.glowuprizzcrm.core.submission;

import com.sanghee.glowuprizzcrm.core.link.Channel;

public interface ChannelSubmissionStats {

    Channel getChannel();

    long getSubmissionCount();
}
