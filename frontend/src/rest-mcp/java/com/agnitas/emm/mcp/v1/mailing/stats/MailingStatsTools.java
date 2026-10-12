/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.stats;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.mailing.stats.dto.MailingStatsResponse;
import com.agnitas.emm.mcp.v1.mailing.stats.service.MailingStatsMcpService;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingStatsTools {

    private final MailingStatsMcpService statsService;
    private final McpAdminProvider adminProvider;

    public MailingStatsTools(MailingStatsMcpService statsService, McpAdminProvider adminProvider) {
        this.statsService = statsService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_stats",
            description = "Returns sending and engagement statistics for a mailing (opens, clicks, bounces, unsubscribes, revenue, etc.)"
    )
    public MailingStatsResponse getStats(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.STATS_SHOW);
        return statsService.getStats(mailingId, admin);
    }

}
