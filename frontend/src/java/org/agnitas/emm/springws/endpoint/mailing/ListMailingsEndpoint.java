/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import com.agnitas.beans.Mailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListMailingsRequest;
import org.agnitas.emm.springws.jaxb.ListMailingsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class ListMailingsEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public ListMailingsEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListMailingsRequest")
	public @ResponsePayload ListMailingsResponse listMailings(@RequestPayload ListMailingsRequest request) {
		ListMailingsResponse response = new ListMailingsResponse();

		MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setTemplate(false);
		
		for (Mailing mailing : mailingService.getMailings(model)) {
			response.getItem().add(new MailingResponseBuilder().createResponse(mailing));
		}
			
		return response;
	}
}
