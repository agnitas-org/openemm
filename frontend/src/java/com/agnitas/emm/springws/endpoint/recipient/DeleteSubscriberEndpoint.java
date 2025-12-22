/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.recipient.service.RecipientModel;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.DeleteSubscriberRequest;
import com.agnitas.emm.springws.jaxb.DeleteSubscriberResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.emm.springws.util.UserActivityLogAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteSubscriberEndpoint extends BaseEndpoint {

	private final RecipientService recipientService;
	private final SecurityContextAccess securityContextAccess;
	private final UserActivityLogAccess userActivityLogAccess;

	public DeleteSubscriberEndpoint(final RecipientService recipientService, final SecurityContextAccess securityContextAccess, final UserActivityLogAccess userActivityLogAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.userActivityLogAccess = Objects.requireNonNull(userActivityLogAccess, "userActivityLogAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DeleteSubscriberRequest")
	public @ResponsePayload DeleteSubscriberResponse deleteSubscriber(@RequestPayload DeleteSubscriberRequest request) {
		final RecipientModel model = parseModel(request, this.securityContextAccess);

		final List<UserAction> userActions = new ArrayList<>();
		recipientService.deleteSubscriber(model, userActions);

		// Write UAL entries
		this.userActivityLogAccess.writeLog(userActions);

		return new DeleteSubscriberResponse();
	}

	static RecipientModel parseModel(DeleteSubscriberRequest request, final SecurityContextAccess securityContextAccess) {
		final RecipientModel model = new RecipientModel();
		
		model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
		model.setCustomerId(request.getCustomerID());
		
		return model;
	}
}
