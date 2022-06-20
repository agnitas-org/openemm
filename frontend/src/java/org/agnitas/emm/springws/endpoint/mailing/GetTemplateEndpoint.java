/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import jakarta.xml.bind.JAXBElement;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetTemplateRequest;
import org.agnitas.emm.springws.jaxb.Template;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class GetTemplateEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public GetTemplateEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "GetTemplateRequest")
	public @ResponsePayload JAXBElement<Template> getTemplate(@RequestPayload GetTemplateRequest request) {
		MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getTemplateID());
		model.setTemplate(true);

		return objectFactory.createGetTemplateResponse(new MailingResponseBuilder().createTemplateResponse(mailingService.getMailing(model)));
	}
}
