/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddMailingFromTemplateRequest;
import org.agnitas.emm.springws.jaxb.AddMailingFromTemplateResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class AddMailingFromTemplateEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public AddMailingFromTemplateEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddMailingFromTemplateRequest")
	public @ResponsePayload AddMailingFromTemplateResponse addMailingFromTemplate(@RequestPayload AddMailingFromTemplateRequest request) {
		AddMailingFromTemplateResponse response = new AddMailingFromTemplateResponse();

		MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setTemplateId(request.getTemplateID());
		model.setShortname(request.getShortname());
		model.setDescription(request.getDescription());
		model.setAutoUpdate(request.isAutoUpdate());

		response.setMailingID(mailingService.addMailingFromTemplate(model));
		return response;
	}
}
