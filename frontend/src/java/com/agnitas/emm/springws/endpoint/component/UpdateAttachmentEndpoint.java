/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.component;

import java.util.Objects;

import com.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.component.service.ComponentModel;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.UpdateAttachmentRequest;
import com.agnitas.emm.springws.jaxb.UpdateAttachmentResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class UpdateAttachmentEndpoint extends BaseEndpoint {

	private final ComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;

	public UpdateAttachmentEndpoint(@Qualifier("componentService") ComponentService componentService, final MailingEditableCheck mailingEditableCheck, final SecurityContextAccess securityContextAccess) {
		this.componentService = Objects.requireNonNull(componentService, "componentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "UpdateAttachmentRequest")
	public @ResponsePayload UpdateAttachmentResponse updateAttachment(@RequestPayload UpdateAttachmentRequest request) throws Exception {
		this.mailingEditableCheck.requireMailingForComponentEditable(request.getComponentID(), this.securityContextAccess.getWebserviceUserCompanyId());

		final ComponentModel model = new ComponentModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setComponentId(request.getComponentID());
		model.setMimeType(request.getMimeType());
		model.setComponentType(MailingComponentType.Attachment);
		model.setComponentName(request.getComponentName());
		model.setData(request.getData());

		componentService.updateComponent(model);

        return new UpdateAttachmentResponse();
	}

}
