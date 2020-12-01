/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import javax.xml.bind.JAXBElement;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.Attachment;
import org.agnitas.emm.springws.jaxb.GetAttachmentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetAttachmentEndpoint extends BaseEndpoint {

	private ComponentService componentService;

	public GetAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService) {
		this.componentService = componentService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "GetAttachmentRequest")
	public @ResponsePayload JAXBElement<Attachment> getAttachment(@RequestPayload GetAttachmentRequest request) {
		ComponentModel model = new ComponentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setComponentId(request.getComponentID());
		model.setComponentType(MailingComponent.TYPE_ATTACHMENT);

        if (request.isUseISODateFormat() == null) {
            request.setUseISODateFormat(false);
        }

		return objectFactory.createGetAttachmentResponse(new AttachmentResponseBuilder().createResponse(componentService.getComponent(model), true, request.isUseISODateFormat()));
	}
}
