/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.UpdateSubscriberRequest;
import org.agnitas.emm.springws.jaxb.UpdateSubscriberResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class UpdateSubscriberEndpoint extends BaseEndpoint {

	private RecipientService recipientService;

	public UpdateSubscriberEndpoint(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "UpdateSubscriberRequest")
	public @ResponsePayload UpdateSubscriberResponse updateSubscriber(@RequestPayload UpdateSubscriberRequest request) throws Exception {
		UpdateSubscriberResponse response = new UpdateSubscriberResponse();

		RecipientModel model = parseModel(request);

		String username = Utils.getUserName();

		response.setValue(recipientService.updateSubscriber(model, username));
		return response;
	}
	
	static RecipientModel parseModel(UpdateSubscriberRequest request) {
		RecipientModel model = new RecipientModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setCustomerId(request.getCustomerID());
		model.setParameters(Utils.toCaseInsensitiveMap(request.getParameters(), true));
		return model;
	}
}
