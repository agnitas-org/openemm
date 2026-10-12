/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.links.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.links.MailingLinkResponse;
import com.agnitas.emm.mcp.v1.mailing.links.dto.PatchMailingLinkRequest;

public interface MailingLinkMcpService {

    PageResponse<MailingLinkResponse> list(int mailingId, McpPageable pageable, int companyId);

    MailingLinkResponse getById(int linkId, int mailingId, int companyId);

    MailingLinkResponse patch(int linkId, int mailingId, PatchMailingLinkRequest request, Admin admin);

}
