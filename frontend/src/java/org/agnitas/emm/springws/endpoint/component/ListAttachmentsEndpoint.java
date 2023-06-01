/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import java.util.Objects;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.ListAttachmentsRequest;
import org.agnitas.emm.springws.jaxb.ListAttachmentsResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListAttachmentsEndpoint extends BaseEndpoint {

	private final ComponentService componentService;
	private final SecurityContextAccess securityContextAccess;

	public ListAttachmentsEndpoint(@Qualifier("componentService") ComponentService componentService, final SecurityContextAccess securityContextAccess) {
		this.componentService = Objects.requireNonNull(componentService, "componentService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListAttachmentsRequest")
	public @ResponsePayload ListAttachmentsResponse listAttachments(@RequestPayload ListAttachmentsRequest request) {
		final ComponentModel model = new ComponentModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setMailingId(request.getMailingID());
		model.setComponentType(MailingComponentType.Attachment);

        if (request.isUseISODateFormat() == null) {
            request.setUseISODateFormat(false);
        }

        final ListAttachmentsResponse response = new ListAttachmentsResponse();
        for (MailingComponent component : componentService.getComponents(model)) {
			response.getItem().add(new AttachmentResponseBuilder().createResponse(component, false, request.isUseISODateFormat()));
		}
			
		return response;
	}
}
