/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.recipient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.infrastructure.activitylog.McpActivityContext;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkUpsertRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.CreateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.ListRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.UpdateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.service.RecipientMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
    private static final String ID_DESCRIPTION = "Recipient ID. Provide exactly one of recipientId or email.";
    private static final String LIST_FILTER_DESCRIPTION = "Search filters for recipients. All fields are optional; omit to list all recipients.";
    private static final String EMAIL_DESCRIPTION = "Recipient email. Provide exactly one of recipientId or email.";

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
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description =
                    """
                    Returns a paginated list of recipients. \
                    All filters are optional and combined with AND: mailing list, target group, subscription status, \
                    recipient types, and an EQL expression.
                    """
    )
    public PageResponse<Map<String, Object>> getList(
            @Valid
            @McpToolParam(description = LIST_FILTER_DESCRIPTION, required = false)
            ListRecipientsRequest request,

            @Valid
            @McpToolParam(description = McpPageable.PARAM_DESCRIPTION, required = false)
            McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return recipientMcpService.list(request, McpPageable.orDefault(pageable), admin);
    }

    @McpTool(
            name = "get_recipient_ids",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description =
                    """
                    Returns an array of recipient IDs. \
                    All filters are optional and combined with AND: mailing list, target group, subscription status, \
                    recipient types, and an EQL expression.
                    """
    )
    public List<Integer> getIdsList(
            @Valid
            @McpToolParam(description = LIST_FILTER_DESCRIPTION, required = false)
            ListRecipientsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return recipientMcpService.listIds(request, admin);
    }

    @McpTool(
            name = "get_recipient",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description = "Returns a recipient by its ID or email"
    )
    public Map<String, Object> get(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer recipientId,
            @McpToolParam(description = EMAIL_DESCRIPTION, required = false) @Email String email,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        McpActivityContext.current().setObjectId(recipientId, email);
        return recipientMcpService.get(recipientId, email, admin);
    }

    @McpTool(
            name = "create_recipient",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, openWorldHint = false),
            description = "Creates a new recipient"
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public Map<String, Object> create(
            @Valid CreateRecipientRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CREATE);
        Map<String, Object> response = recipientMcpService.create(request, admin);
        McpActivityContext.current().setObjectId(
                response.get(RecipientStandardField.CustomerID.getColumnName()),
                request.fields().get(RecipientStandardField.Email.getColumnName())
        );
        return response;
    }

    @McpTool(
            name = "update_recipient",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description = "Updates an existing recipient by its ID or email. Fields not passed are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public Map<String, Object> update(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer recipientId,
            @McpToolParam(description = EMAIL_DESCRIPTION, required = false) @Email String email,
            @Valid UpdateRecipientRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        McpActivityContext.current().setObjectId(recipientId, email);
        return recipientMcpService.update(recipientId, email, request, admin);
    }

    @McpTool(
            name = "delete_recipient",
            annotations = @McpTool.McpAnnotations(idempotentHint = true, openWorldHint = false),
            description = "Deletes a recipient by its ID or email"
    )
    public void delete(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer recipientId,
            @McpToolParam(description = EMAIL_DESCRIPTION, required = false) @Email String email,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_DELETE);
        McpActivityContext.current().setObjectId(recipientId, email);
        recipientMcpService.delete(recipientId, email, admin);
    }

    @McpTool(
            name = "create_recipients",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, openWorldHint = false),
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
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = true, openWorldHint = false),
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
            annotations = @McpTool.McpAnnotations(idempotentHint = true, openWorldHint = false),
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
