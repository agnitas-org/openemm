/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.send.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.mailing.send.MailingStartResponse;
import com.agnitas.emm.mcp.v1.mailing.send.MailingStopResponse;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendAdminMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendTestMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.StartSendMailingRequest;

public interface MailingSendMcpService {

    MailingStartResponse send(StartSendMailingRequest request, Admin admin);

    MailingStartResponse sendAdmin(SendAdminMailingRequest request, Admin admin);

    MailingStartResponse sendTest(SendTestMailingRequest request, Admin admin);

    MailingStopResponse stop(int mailingId, int companyId);

}
