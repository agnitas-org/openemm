/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.stats.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsOpts;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsOptsFactory;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService;
import com.agnitas.emm.core.birtstatistics.service.impl.MailingStatisticsServiceImpl;
import com.agnitas.emm.mcp.v1.mailing.stats.dto.MailingStatsResponse;
import com.agnitas.emm.mcp.v1.mailing.stats.mapper.MailingStatsMcpMapper;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.springframework.stereotype.Service;

@Service
public class MailingStatsMcpServiceImpl implements MailingStatsMcpService {

    private final MailingSummaryStatsService summaryStatsService;
    private final MailingStatsMcpMapper mapper;
    private final MailingSummaryStatsOptsFactory statsOptsFactory;

    public MailingStatsMcpServiceImpl(
            MailingSummaryStatsService summaryStatsService,
            MailingStatsMcpMapper mapper,
            MailingSummaryStatsOptsFactory statsOptsFactory
    ) {
        this.summaryStatsService = summaryStatsService;
        this.mapper = mapper;
        this.statsOptsFactory = statsOptsFactory;
    }

    @Override
    public MailingStatsResponse getStats(int mailingId, Admin admin) {
        MailingSummaryStatsOpts opts = statsOptsFactory.createOpts(mailingId, admin);
        LightTarget allSubscribersTarget = MailingStatisticsServiceImpl.createAllSubscribersTarget(admin.getLocale());
        return mapper.toResponse(summaryStatsService.getStats(allSubscribersTarget, opts));
    }

}
