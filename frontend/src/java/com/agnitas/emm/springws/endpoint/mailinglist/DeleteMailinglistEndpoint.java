/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.mailinglist;

import java.util.Objects;

import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.DeleteMailinglistRequest;
import com.agnitas.emm.springws.jaxb.DeleteMailinglistResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailinglist.service.MailinglistService;

@Endpoint
public class DeleteMailinglistEndpoint extends BaseEndpoint {

	private final MailinglistService mailinglistService;
	private final SecurityContextAccess securityContextAccess;

	@Autowired
	public DeleteMailinglistEndpoint(@Qualifier("MailinglistService") MailinglistService mailinglistService, final SecurityContextAccess securityContextAccess) {
		this.mailinglistService = Objects.requireNonNull(mailinglistService, "mailinglistService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DeleteMailinglistRequest")
	public @ResponsePayload DeleteMailinglistResponse deleteMailinglist(@RequestPayload DeleteMailinglistRequest request) {
		final DeleteMailinglistResponse response = new DeleteMailinglistResponse();
		final boolean result = mailinglistService.deleteMailinglist(request.getMailinglistID(), this.securityContextAccess.getWebserviceUserCompanyId());
		
		if(!result) {
			throw new MailinglistNotExistException(request.getMailinglistID(), this.securityContextAccess.getWebserviceUserCompanyId());
		}
		
		response.setValue(result);
		
		return response;
	}
}
