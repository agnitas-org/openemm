/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.binding.dto.BindingResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.CreateBindingRequest;
import com.agnitas.emm.mcp.v1.binding.dto.ListBindingsFilter;
import com.agnitas.emm.mcp.v1.binding.dto.PatchBindingRequest;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;

public interface BindingMcpService {

    PageResponse<BindingResponse> list(ListBindingsFilter filter, McpPageable pageable, int companyId);

    BindingResponse getById(int recipientId, int mailinglistId, MediaTypes mediatype, int companyId);

    BindingResponse create(CreateBindingRequest request, int companyId);

    BindingResponse patch(int recipientId, int mailinglistId, MediaTypes mediatype, PatchBindingRequest request, int companyId);

    void delete(int recipientId, int mailinglistId, MediaTypes mediatype, int companyId);

    BulkDeleteBindingsResponse deleteBulk(BulkDeleteBindingsRequest request, int companyId);

    BulkUpsertBindingsResponse upsertBulk(BulkUpsertBindingsRequest request, Admin admin);

}
