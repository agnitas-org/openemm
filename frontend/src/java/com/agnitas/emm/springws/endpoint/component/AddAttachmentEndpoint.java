/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.component;

import java.util.Objects;

import com.agnitas.beans.MailingComponentType;
import com.agnitas.emm.core.components.entity.ComponentModel;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.AddAttachmentRequest;
import com.agnitas.emm.springws.jaxb.AddAttachmentResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddAttachmentEndpoint extends BaseEndpoint {

	private final ComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;

	public AddAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService, @Qualifier("MailingEditableCheck") final MailingEditableCheck mailingEditableCheck, final SecurityContextAccess securityContextAccess) {
		this.componentService =  Objects.requireNonNull(componentService, "componentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddAttachmentRequest")
	public @ResponsePayload AddAttachmentResponse addAttachment(@RequestPayload AddAttachmentRequest request) throws Exception {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		this.mailingEditableCheck.requireMailingEditable(request.getMailingID(), companyID);

		final ComponentModel model = new ComponentModel();
		model.setCompanyId(companyID);
		model.setMailingId(request.getMailingID());
		model.setMimeType(request.getMimeType());
		model.setComponentType(MailingComponentType.Attachment);
		model.setComponentName(request.getComponentName());
		model.setData(request.getData());

		final AddAttachmentResponse response = new AddAttachmentResponse();
		response.setComponentID(componentService.addComponent(model));
		return response;
	}
}
