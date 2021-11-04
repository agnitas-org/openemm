/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddSubscriberRequest;
import org.agnitas.emm.springws.jaxb.AddSubscriberResponse;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.springws.subscriptionrejection.service.SubscriptionRejectionService;

@Endpoint
public class AddSubscriberEndpoint extends BaseEndpoint {
	private static final transient Logger classLogger = Logger.getLogger(AddSubscriberEndpoint.class);
	
	private RecipientService recipientService;

	@Resource
	@Lazy
	private SubscriptionRejectionService subscriptionRejectionService;

	public AddSubscriberEndpoint(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddSubscriberRequest")
	public @ResponsePayload AddSubscriberResponse addSubscriber(@RequestPayload AddSubscriberRequest request) throws Exception {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered AddSubscriberEndpoint.addSubscriber()");
		}
			
		AddSubscriberResponse response = new AddSubscriberResponse();
		RecipientModel model = parseModel(request);
		int companyID = Utils.getUserCompany();
		
		if (subscriptionRejectionService != null) {
			subscriptionRejectionService.checkSubscriptionData(companyID, model);
		} else {
			if (classLogger.isDebugEnabled()) {
				classLogger.debug("No subscription rejection service set");
			}
		}

		if (classLogger.isInfoEnabled()) {
			classLogger.info("Calling recipient service layer");
		}

		String username = Utils.getUserName();
		List<UserAction> userActions = new ArrayList<>();
		response.setCustomerID(recipientService.addSubscriber(model, username, companyID, userActions));
		Utils.writeLog(userActivityLogService, userActions);

		if (classLogger.isInfoEnabled()) {
			classLogger.info("Leaving AddSubscriberEndpoint.addSubscriber()");
		}

		return response;
	}
	
	static RecipientModel parseModel(AddSubscriberRequest request) {
		if (classLogger.isInfoEnabled()) {
			classLogger.info("Parsing recipient model");
		}
		
		RecipientModel model = new RecipientModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setDoubleCheck(request.isDoubleCheck());
		model.setKeyColumn(request.getKeyColumn());
		model.setOverwrite(request.isOverwrite());
		model.setParameters(Utils.toCaseInsensitiveMap(request.getParameters(), true));

		return model;
	}
}
