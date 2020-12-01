/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListAttachmentsRequest;
import org.agnitas.emm.springws.jaxb.ListAttachmentsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListAttachmentsEndpoint extends BaseEndpoint {

	private ComponentService componentService;

	public ListAttachmentsEndpoint(@Qualifier("componentService") ComponentService componentService) {
		this.componentService = componentService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListAttachmentsRequest")
	public @ResponsePayload ListAttachmentsResponse listAttachments(@RequestPayload ListAttachmentsRequest request) {
		ListAttachmentsResponse response = new ListAttachmentsResponse();
		
		ComponentModel model = new ComponentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		model.setComponentType(MailingComponent.TYPE_ATTACHMENT);

        if (request.isUseISODateFormat() == null) {
            request.setUseISODateFormat(false);
        }

        for (MailingComponent component : componentService.getComponents(model)) {
			response.getItem().add(new AttachmentResponseBuilder().createResponse(component, false, request.isUseISODateFormat()));
		}
			
		return response;
	}
}
