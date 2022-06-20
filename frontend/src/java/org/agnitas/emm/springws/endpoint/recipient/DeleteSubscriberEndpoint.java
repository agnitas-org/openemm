/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberRequest;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteSubscriberEndpoint extends BaseEndpoint {

	private RecipientService recipientService;

	public DeleteSubscriberEndpoint(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "DeleteSubscriberRequest")
	public @ResponsePayload DeleteSubscriberResponse deleteSubscriber(@RequestPayload DeleteSubscriberRequest request) {
		DeleteSubscriberResponse response = new DeleteSubscriberResponse();

		RecipientModel model = parseModel(request);

		List<UserAction> userActions = new ArrayList<>();
		recipientService.deleteSubscriber(model, userActions);
		Utils.writeLog(userActivityLogService, userActions);

		return response;
	}

	static RecipientModel parseModel(DeleteSubscriberRequest request) {
		RecipientModel model = new RecipientModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setCustomerId(request.getCustomerID());
		return model;
	}
}
