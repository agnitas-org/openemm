/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.form.dto.CreateFormRequest;
import com.agnitas.emm.mcp.v1.form.dto.FormLightResponse;
import com.agnitas.emm.mcp.v1.form.dto.FormResponse;
import com.agnitas.emm.mcp.v1.form.dto.PatchFormRequest;
import com.agnitas.emm.mcp.v1.form.service.FormMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class FormTools {

    private final FormMcpService formMcpService;
    private final McpAdminProvider adminProvider;

    public FormTools(
            FormMcpService formMcpService,
            McpAdminProvider adminProvider
    ) {
        this.formMcpService = formMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_forms",
            description = "Returns a paginated list of user forms"
    )
    public PageResponse<FormLightResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.FORMS_SHOW);
        return formMcpService.list(pageable, admin.getCompanyID());
    }

    @McpTool(
            name = "get_form_by_id",
            description = "Returns a user form by its ID"
    )
    public FormResponse getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.FORMS_SHOW);
        return formMcpService.getById(id, admin.getCompanyID());
    }

    @McpTool(
            name = "create_form",
            description = "Creates a new user form"
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public FormResponse create(
            @Valid CreateFormRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.FORMS_CHANGE);
        return formMcpService.create(request, admin);
    }

    @McpTool(
            name = "update_form",
            description = "Updates an existing user form. Fields not passed are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public FormResponse patch(
            @McpToolParam(description = "Form ID") @Positive int id,
            @Valid PatchFormRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.FORMS_CHANGE);
        return formMcpService.patch(id, request, admin);
    }

    @McpTool(
            name = "delete_form",
            description = "Deletes a user form by its ID"
    )
    public void delete(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.FORMS_DELETE);
        formMcpService.delete(id, admin.getCompanyID());
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return "request.successSettings.template".equals(path) || "request.errorSettings.template".equals(path);
    }
}
