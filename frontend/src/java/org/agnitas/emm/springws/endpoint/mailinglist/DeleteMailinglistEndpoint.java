/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailinglist;

import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import org.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DeleteMailinglistRequest;
import org.agnitas.emm.springws.jaxb.DeleteMailinglistResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteMailinglistEndpoint extends BaseEndpoint {

	private MailinglistService mailinglistService;

	@Autowired
	public DeleteMailinglistEndpoint(@Qualifier("WS_mailinglistService") MailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "DeleteMailinglistRequest")
	public @ResponsePayload DeleteMailinglistResponse deleteMailinglist(@RequestPayload DeleteMailinglistRequest request) throws Exception {
		DeleteMailinglistResponse response = new DeleteMailinglistResponse();
		
		MailinglistModel model = new MailinglistModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailinglistId(request.getMailinglistID());

		response.setValue(mailinglistService.deleteMailinglist(model));
		return response;
	}
}
