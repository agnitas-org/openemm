/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.recipient.service;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkUpsertRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.CreateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.UpdateRecipientRequest;

public interface RecipientMcpService {

    PageResponse<Map<String, Object>> list(Map<String, String> filters, McpPageable pageable, Admin admin);

    Map<String, Object> getById(int id, Admin admin);

    Map<String, Object> create(CreateRecipientRequest request, Admin admin);

    Map<String, Object> update(int id, UpdateRecipientRequest request, Admin admin);

    void delete(int id, Admin admin);

    BulkUpsertRecipientsResponse createBulk(BulkRecipientsRequest request, Admin admin);

    BulkUpsertRecipientsResponse addOrUpdateBulk(BulkRecipientsRequest request, Admin admin);

    BulkDeleteRecipientsResponse deleteBulk(BulkDeleteRecipientsRequest request, Admin admin);

    boolean isAllowedToUseHtml(String columnName, Admin admin);

}
