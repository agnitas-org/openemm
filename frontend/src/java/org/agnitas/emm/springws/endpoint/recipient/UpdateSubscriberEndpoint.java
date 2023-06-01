/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.Objects;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.UpdateSubscriberRequest;
import org.agnitas.emm.springws.jaxb.UpdateSubscriberResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class UpdateSubscriberEndpoint extends BaseEndpoint {

	private RecipientService recipientService;
	private SecurityContextAccess securityContextAccess;

	public UpdateSubscriberEndpoint(RecipientService recipientService, final SecurityContextAccess securityContextAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "UpdateSubscriberRequest")
	public @ResponsePayload UpdateSubscriberResponse updateSubscriber(@RequestPayload UpdateSubscriberRequest request) throws Exception {
		final RecipientModel model = parseModel(request, this.securityContextAccess);

		final String username = this.securityContextAccess.getWebserviceUserName();

		final UpdateSubscriberResponse response = new UpdateSubscriberResponse();
		response.setValue(recipientService.updateSubscriber(model, username));
		
		return response;
	}
	
	static RecipientModel parseModel(UpdateSubscriberRequest request, final SecurityContextAccess securityContextAccess) {
		RecipientModel model = new RecipientModel();
		model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
		model.setCustomerId(request.getCustomerID());
		model.setParameters(Utils.toCaseInsensitiveMap(request.getParameters(), true));
		return model;
	}
}
