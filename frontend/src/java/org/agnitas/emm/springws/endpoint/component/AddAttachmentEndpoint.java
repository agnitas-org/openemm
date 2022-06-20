/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import java.util.Objects;

import org.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddAttachmentRequest;
import org.agnitas.emm.springws.jaxb.AddAttachmentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddAttachmentEndpoint extends BaseEndpoint {

	private ComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;


	public AddAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService, @Qualifier("MailingEditableCheck") final MailingEditableCheck mailingEditableCheck) {
		this.componentService = componentService;
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck);
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddAttachmentRequest")
	public @ResponsePayload AddAttachmentResponse addAttachment(@RequestPayload AddAttachmentRequest request) throws Exception {
		final int companyID = Utils.getUserCompany();
		
		this.mailingEditableCheck.requireMailingEditable(request.getMailingID(), companyID);

		AddAttachmentResponse response = new AddAttachmentResponse();
		ComponentModel model = new ComponentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		model.setMimeType(request.getMimeType());
		model.setComponentType(MailingComponentType.Attachment);
		model.setComponentName(request.getComponentName());
		model.setData(request.getData());

		response.setComponentID(componentService.addComponent(model));
		return response;
	}
}
