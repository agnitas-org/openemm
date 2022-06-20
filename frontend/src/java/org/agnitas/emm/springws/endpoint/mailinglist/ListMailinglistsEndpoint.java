/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailinglist;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import org.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListMailinglistsRequest;
import org.agnitas.emm.springws.jaxb.ListMailinglistsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListMailinglistsEndpoint extends BaseEndpoint {

    private MailinglistService mailinglistService;

    @Autowired
	public ListMailinglistsEndpoint(@Qualifier("WS_mailinglistService") MailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListMailinglistsRequest")
    public @ResponsePayload ListMailinglistsResponse listMailinglists(@RequestPayload ListMailinglistsRequest request) throws Exception {
        ListMailinglistsResponse response = new ListMailinglistsResponse();
        
		MailinglistModel model = new MailinglistModel();
		model.setCompanyId(Utils.getUserCompany());

        for (Mailinglist mailinglist : mailinglistService.listMailinglists(Utils.getUserCompany())) {
			response.getItem().add(new MailinglistResponseBuilder(mailinglist).createResponse());
		}
        return response;
    }
}
