/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.content.service;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.mailing.content.MailingContentResponse;
import com.agnitas.emm.mcp.v1.mailing.content.dto.PatchMailingContentRequest;

public interface MailingContentMcpService {

    List<MailingContentResponse> list(int mailingId, int companyId);

    MailingContentResponse getById(int contentId, int mailingId, int companyId);

    MailingContentResponse getByName(String name, int mailingId, int companyId);

    MailingContentResponse patch(int contentId, int mailingId, PatchMailingContentRequest request, Admin admin);

    MailingContentResponse patch(String name, int mailingId, PatchMailingContentRequest request, Admin admin);

    void delete(int contentId, int mailingId, int companyId);

    void delete(String name, int mailingId, int companyId);

}
