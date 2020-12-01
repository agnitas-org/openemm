/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.FindSubscriberRequest;
import org.agnitas.emm.springws.jaxb.FindSubscriberResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class FindSubscriberEndpoint extends BaseEndpoint {

	private RecipientService recipientService;

	public FindSubscriberEndpoint(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "FindSubscriberRequest")
	public @ResponsePayload FindSubscriberResponse findSubscriber(@RequestPayload FindSubscriberRequest request) {
		FindSubscriberResponse response = new FindSubscriberResponse();
		response.setValue(recipientService.findSubscriber(Utils.getUserCompany(), request.getKeyColumn(), request.getValue()));
		return response;
	}
}
