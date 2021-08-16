/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import org.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DeleteAttachmentRequest;
import org.agnitas.emm.springws.jaxb.DeleteAttachmentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteAttachmentEndpoint extends BaseEndpoint {

	private ComponentService componentService;

	public DeleteAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService) {
		this.componentService = componentService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "DeleteAttachmentRequest")
	public @ResponsePayload DeleteAttachmentResponse deleteAttachment(@RequestPayload DeleteAttachmentRequest request) {
		DeleteAttachmentResponse response = new DeleteAttachmentResponse();
		ComponentModel model = new ComponentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setComponentId(request.getComponentID());
		model.setComponentType(MailingComponentType.Attachment);

		componentService.deleteComponent(model);
		return response;
	}
}
