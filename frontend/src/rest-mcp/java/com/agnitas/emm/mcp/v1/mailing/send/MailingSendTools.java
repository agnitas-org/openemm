/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.send;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendAdminMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendTestMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.StartSendMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.service.MailingSendMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingSendTools {

    private final MailingSendMcpService mailingSendMcpService;
    private final McpAdminProvider adminProvider;

    public MailingSendTools(MailingSendMcpService mailingSendMcpService, McpAdminProvider adminProvider) {
        this.mailingSendMcpService = mailingSendMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "send_mailing",
            description = "Starts delivery of a mailing to its recipients. Sends regular/follow-up mailings " +
                    "to their recipients, or activates action-based, date-based and interval " +
                    "mailings - whichever applies to the given mailing."
    )
    public MailingStartResponse send(
            @Valid StartSendMailingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_SEND_WORLD);
        return mailingSendMcpService.send(request, admin);
    }

    @McpTool(
            name = "send_admin_mailing",
            description = "Sends the mailing to its admin recipients."
    )
    public MailingStartResponse sendAdmin(
            @Valid SendAdminMailingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin();
        return mailingSendMcpService.sendAdmin(request, admin);
    }

    @McpTool(
            name = "send_test_mailing",
            description = "Sends the mailing to its test recipients."
    )
    public MailingStartResponse sendTest(
            @Valid SendTestMailingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin();
        return mailingSendMcpService.sendTest(request, admin);
    }

    @McpTool(
            name = "stop_mailing",
            description = "Stops an active or already-sent mailing. Cancels regular/follow-up mailings, " +
                    "or deactivates action-based, date-based and interval mailings - whichever applies to the given mailing."
    )
    public MailingStopResponse stop(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_SEND_WORLD);
        return mailingSendMcpService.stop(mailingId, admin.getCompanyID());
    }

}
