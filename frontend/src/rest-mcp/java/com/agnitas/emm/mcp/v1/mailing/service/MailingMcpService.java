/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingFullResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;

public interface MailingMcpService {

    PageResponse<MailingLightResponse> list(ListMailingsRequest query, McpPageable pageable, Admin admin);

    MailingResponse getById(int id, int companyId);

    MailingFullResponse getFull(int id, int companyId);

    MailingResponse create(CreateMailingRequest request, Admin admin);

    MailingLightResponse createFromTemplate(int templateId, int companyId);

    MailingLightResponse copy(int copyFromId, int companyId);

    MailingResponse patch(int id, PatchMailingRequest request, Admin admin);

    void delete(int id, Admin admin);

    PageResponse<MailingLightResponse> listTemplates(ListTemplatesRequest query, McpPageable pageable, Admin admin);

    MailingResponse getTemplate(int id, int companyId);

    MailingFullResponse getFullTemplate(int id, int companyId);

    MailingResponse createTemplate(CreateMailingRequest request, Admin admin);

    MailingLightResponse copyTemplate(int copyFromId, int companyId);

    MailingResponse patchTemplate(int id, PatchMailingRequest request, Admin admin);

    void deleteTemplate(int id, Admin admin);

}
