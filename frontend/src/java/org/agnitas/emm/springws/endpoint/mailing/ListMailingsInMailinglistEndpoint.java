/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import com.agnitas.beans.Mailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListMailingsInMailinglistRequest;
import org.agnitas.emm.springws.jaxb.ListMailingsInMailinglistResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class ListMailingsInMailinglistEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public ListMailingsInMailinglistEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListMailingsInMailinglistRequest")
	public @ResponsePayload ListMailingsInMailinglistResponse listMailingsInMailinglist(@RequestPayload ListMailingsInMailinglistRequest request) throws Exception {
		ListMailingsInMailinglistResponse response = new ListMailingsInMailinglistResponse();

		MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailinglistId(request.getMailinglistID());
		
		for (Mailing mailing : mailingService.getMailingsForMLID(model)) {
			response.getItem().add(new MailingResponseBuilder().createResponse(mailing));
		}
			
		return response;
	}
}
