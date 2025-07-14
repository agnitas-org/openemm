/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.mailinglist;

import java.util.Objects;

import com.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.ListMailinglistsRequest;
import com.agnitas.emm.springws.jaxb.ListMailinglistsResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailinglist.service.MailinglistService;

@Endpoint
public class ListMailinglistsEndpoint extends BaseEndpoint {

    private final MailinglistService mailinglistService;
    private final SecurityContextAccess securityContextAccess;

    @Autowired
	public ListMailinglistsEndpoint(@Qualifier("MailinglistService") MailinglistService mailinglistService, final SecurityContextAccess securityContextAccess) {
		this.mailinglistService = Objects.requireNonNull(mailinglistService, "mailinglistService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListMailinglistsRequest")
    public @ResponsePayload ListMailinglistsResponse listMailinglists(@RequestPayload ListMailinglistsRequest request) {
        final MailinglistModel model = new MailinglistModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());

		final ListMailinglistsResponse response = new ListMailinglistsResponse();
        for (final Mailinglist mailinglist : mailinglistService.getMailinglists(this.securityContextAccess.getWebserviceUserCompanyId())) {
			response.getItem().add(new MailinglistResponseBuilder(mailinglist).createResponse());
		}
        
        return response;
    }
}
