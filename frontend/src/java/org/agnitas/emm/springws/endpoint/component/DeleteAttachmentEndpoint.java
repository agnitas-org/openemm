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
import com.agnitas.emm.core.components.service.ComponentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.exception.MailingNotEditableException;
import org.agnitas.emm.springws.jaxb.DeleteAttachmentRequest;
import org.agnitas.emm.springws.jaxb.DeleteAttachmentResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteAttachmentEndpoint extends BaseEndpoint {

	private final ComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;

	public DeleteAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService, final MailingEditableCheck mailingEditableCheck, final SecurityContextAccess securityContextAccess) {
		this.componentService = Objects.requireNonNull(componentService, "componentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DeleteAttachmentRequest")
	public @ResponsePayload DeleteAttachmentResponse deleteAttachment(@RequestPayload DeleteAttachmentRequest request) throws MailingNotEditableException {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		this.mailingEditableCheck.requireMailingForComponentEditable(request.getComponentID(), companyID);
		
		DeleteAttachmentResponse response = new DeleteAttachmentResponse();
		ComponentModel model = new ComponentModel();
		model.setCompanyId(companyID);
		model.setComponentId(request.getComponentID());
		model.setComponentType(MailingComponentType.Attachment);

		componentService.deleteComponent(model);
		return response;
	}
}
