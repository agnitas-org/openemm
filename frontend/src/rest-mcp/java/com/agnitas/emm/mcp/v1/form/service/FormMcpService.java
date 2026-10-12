/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.form.dto.CreateFormRequest;
import com.agnitas.emm.mcp.v1.form.dto.FormLightResponse;
import com.agnitas.emm.mcp.v1.form.dto.FormResponse;
import com.agnitas.emm.mcp.v1.form.dto.PatchFormRequest;

public interface FormMcpService {

    PageResponse<FormLightResponse> list(McpPageable pageable, int companyId);

    FormResponse getById(int formId, int companyId);

    FormResponse create(CreateFormRequest request, Admin admin);

    FormResponse patch(int formId, PatchFormRequest request, Admin admin);

    void delete(int formId, int companyId);

}
