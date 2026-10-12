/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.recipient;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkUpsertRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.CreateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.UpdateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.service.RecipientMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class RecipientTools {

    // e.g. request.fields.email or request.recipients[0].email
    private static final Pattern RECIPIENT_FIELD_PATH = Pattern.compile("request\\.(?:fields|recipients\\[\\d+])\\.([^.\\[]+)");

    private final RecipientMcpService recipientMcpService;
    private final McpAdminProvider adminProvider;

    public RecipientTools(
            RecipientMcpService recipientMcpService,
            McpAdminProvider adminProvider
    ) {
        this.recipientMcpService = recipientMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_recipients",
            description = "Returns a paginated list of recipients. Supports filtering by exact field values (e.g. email)."
    )
    public PageResponse<Map<String, Object>> getList(
            @McpToolParam(description = "Filters recipients by exact field value, e.g. {\"email\": \"foo@example.com\"}", required = false)
            Map<String, String> filters,

            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return recipientMcpService.list(filters, pageable, admin);
    }

    @McpTool(
            name = "get_recipient_by_id",
            description = "Returns a recipient by its ID"
    )
    public Map<String, Object> getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return recipientMcpService.getById(id, admin);
    }

    @McpTool(
            name = "create_recipient",
            description = "Creates a new recipient"
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public Map<String, Object> create(
            @Valid CreateRecipientRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CREATE);
        return recipientMcpService.create(request, admin);
    }

    @McpTool(
            name = "update_recipient",
            description = "Updates an existing recipient. Fields not passed are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public Map<String, Object> update(
            @McpToolParam(description = "Recipient ID") @Positive int id,
            @Valid UpdateRecipientRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        return recipientMcpService.update(id, request, admin);
    }

    @McpTool(
            name = "delete_recipient",
            description = "Deletes a recipient by its ID"
    )
    public void delete(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_DELETE);
        recipientMcpService.delete(id, admin);
    }

    @McpTool(
            name = "create_recipients",
            description = "Creates multiple recipients in bulk (insert only). Recipients that already exist are reported as errors; "
                    + "use add_or_update_recipients to also update existing ones."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public BulkUpsertRecipientsResponse createBulk(
            @Valid BulkRecipientsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CREATE, Permission.IMPORT_MODE_ADD);
        return recipientMcpService.createBulk(request, admin);
    }

    @McpTool(
            name = "add_or_update_recipients",
            description = "Creates or updates multiple recipients in bulk. Recipients matched by email are updated, others are created. "
                    + "Unlike update_recipient, this runs as an import: fields left out of a recipient's entry are cleared to null on "
                    + "an existing recipient, not left unchanged. Always pass every field that recipient should keep."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public BulkUpsertRecipientsResponse addOrUpdateBulk(
            @Valid BulkRecipientsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE, Permission.IMPORT_MODE_ADD_UPDATE);
        return recipientMcpService.addOrUpdateBulk(request, admin);
    }

    @McpTool(
            name = "delete_recipients",
            description = "Deletes multiple recipients by ID in bulk"
    )
    public BulkDeleteRecipientsResponse deleteBulk(
            @Valid BulkDeleteRecipientsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_DELETE);
        return recipientMcpService.deleteBulk(request, admin);
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        Matcher matcher = RECIPIENT_FIELD_PATH.matcher(path);
        return admin != null && matcher.matches() && recipientMcpService.isAllowedToUseHtml(matcher.group(1), admin);
    }

}
