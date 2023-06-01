/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import jakarta.xml.bind.JAXBElement;

import java.util.Objects;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.GetTemplateRequest;
import org.agnitas.emm.springws.jaxb.Template;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class GetTemplateEndpoint extends BaseEndpoint {

	private final MailingService mailingService;
	private final SecurityContextAccess securityContextAccess;

	public GetTemplateEndpoint(@Qualifier("MailingService") MailingService mailingService, final SecurityContextAccess securityContextAccess) {
		this.mailingService = Objects.requireNonNull(mailingService, "mailingService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "GetTemplateRequest")
	public @ResponsePayload JAXBElement<Template> getTemplate(@RequestPayload GetTemplateRequest request) {
		final MailingModel model = new MailingModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setMailingId(request.getTemplateID());
		model.setTemplate(true);

		return objectFactory.createGetTemplateResponse(new MailingResponseBuilder().createTemplateResponse(mailingService.getMailing(model)));
	}
}
