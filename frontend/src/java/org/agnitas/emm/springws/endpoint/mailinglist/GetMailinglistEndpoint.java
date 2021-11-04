/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailinglist;

import jakarta.xml.bind.JAXBElement;

import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import org.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetMailinglistRequest;
import org.agnitas.emm.springws.jaxb.Mailinglist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetMailinglistEndpoint extends BaseEndpoint {

    private MailinglistService mailinglistService;

    @Autowired
	public GetMailinglistEndpoint(@Qualifier("WS_mailinglistService") MailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

    @PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "GetMailinglistRequest")
    public @ResponsePayload JAXBElement<Mailinglist> getMailinglist(@RequestPayload GetMailinglistRequest request) throws Exception {
		MailinglistModel model = new MailinglistModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailinglistId(request.getMailinglistID());

		Mailinglist mailinglist = new MailinglistResponseBuilder(mailinglistService.getMailinglist(model)).createResponse();
		return objectFactory.createGetMailinglistResponse(mailinglist);
    }
}
