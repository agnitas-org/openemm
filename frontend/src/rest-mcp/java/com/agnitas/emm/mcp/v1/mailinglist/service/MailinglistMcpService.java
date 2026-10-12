/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailinglist.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.CreateMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.dto.MailinglistResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.PatchMailinglistRequest;

public interface MailinglistMcpService {

    PageResponse<MailinglistResponse> list(McpPageable pageable, int companyId);

    MailinglistResponse getById(int id, int companyId);

    MailinglistResponse create(CreateMailinglistRequest request, int companyId);

    MailinglistResponse patch(int id, PatchMailinglistRequest request, int companyId);

    void delete(int id, Admin admin);

}
