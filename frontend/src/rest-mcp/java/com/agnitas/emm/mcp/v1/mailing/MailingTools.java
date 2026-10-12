/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingFullResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.service.MailingMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingTools {

    private final MailingMcpService mailingMcpService;
    private final McpAdminProvider adminProvider;

    public MailingTools(
            MailingMcpService mailingMcpService,
            McpAdminProvider adminProvider
    ) {
        this.mailingMcpService = mailingMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailings",
            description = "Returns a paginated list of mailings"
    )
    public PageResponse<MailingLightResponse> getList(
            @Valid
            @McpToolParam(description = "Search filters for mailings. All fields are optional and used only to narrow down results.")
            ListMailingsRequest query,
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_SHOW);
        return mailingMcpService.list(query, pageable, admin);
    }

    @McpTool(
            name = "get_mailing_by_id",
            description = "Returns mailing settings by its ID"
    )
    public MailingResponse getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_SHOW);
        return mailingMcpService.getById(id, admin.getCompanyID());
    }

    @McpTool(
            name = "get_full_mailing_by_id",
            description = "Returns a full mailing data (settings, content blocks, components, links and targets) by its ID"
    )
    public MailingFullResponse getFullById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_SHOW);
        return mailingMcpService.getFull(id, admin.getCompanyID());
    }

    @McpTool(
            name = "create_mailing",
            description = "Creates a new mailing"
    )
    @Validated(MailingGroup.class)
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public MailingResponse create(
            @Valid CreateMailingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        return mailingMcpService.create(request, admin);
    }

    @McpTool(
            name = "create_mailing_from_template",
            description = "Creates a new mailing based on template"
    )
    public MailingLightResponse createFromTemplate(
            @Positive int templateId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        return mailingMcpService.createFromTemplate(templateId, admin.getCompanyID());
    }

    @McpTool(
            name = "copy_mailing",
            description = "Copies a mailing"
    )
    public MailingLightResponse copyById(
            @Positive int sourceId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        return mailingMcpService.copy(sourceId, admin.getCompanyID());
    }

    @McpTool(
            name = "update_mailing",
            description = "Updates an existing mailing. Fields not passed (null) are left unchanged."
    )
    @Validated(MailingGroup.class)
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public MailingResponse patch(
            @McpToolParam(description = "Mailing ID") @Positive int id,
            @Valid PatchMailingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        return mailingMcpService.patch(id, request, admin);
    }

    @McpTool(
            name = "delete_mailing",
            description = "Deletes a mailing by its ID"
    )
    public void delete(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_DELETE);
        mailingMcpService.delete(id, admin);
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return "request.htmlTemplate".equals(path) || "request.textTemplate".equals(path);
    }
}
