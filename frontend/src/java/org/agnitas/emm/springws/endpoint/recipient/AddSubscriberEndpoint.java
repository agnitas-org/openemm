/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddSubscriberRequest;
import org.agnitas.emm.springws.jaxb.AddSubscriberResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.agnitas.emm.springws.util.UserActivityLogAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;
import com.agnitas.emm.springws.subscriptionrejection.service.SubscriptionRejectionService;

import jakarta.annotation.Resource;

@Endpoint
public class AddSubscriberEndpoint extends BaseEndpoint {
	private static final transient Logger classLogger = LogManager.getLogger(AddSubscriberEndpoint.class);

	private final RecipientService recipientService;
	private final ConfigService configService;
	private final SecurityContextAccess securityContextAccess;
	private final UserActivityLogAccess userActivityLogAccess;

	@Resource
	@Lazy
	private SubscriptionRejectionService subscriptionRejectionService;

	public AddSubscriberEndpoint(RecipientService recipientService, ConfigService configService, final SecurityContextAccess securityContextAccess, final UserActivityLogAccess userActivityLogAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.configService = Objects.requireNonNull(configService, "configService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.userActivityLogAccess = Objects.requireNonNull(userActivityLogAccess, "userActivityLogAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddSubscriberRequest")
	public @ResponsePayload AddSubscriberResponse addSubscriber(@RequestPayload AddSubscriberRequest request) throws Exception {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered AddSubscriberEndpoint.addSubscriber()");
		}
			
		AddSubscriberResponse response = new AddSubscriberResponse();
		RecipientModel model = parseModel(request, configService, this.securityContextAccess);
		int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
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

		String username = this.securityContextAccess.getWebserviceUserName();
		List<UserAction> userActions = new ArrayList<>();
		response.setCustomerID(recipientService.addSubscriber(model, username, companyID, userActions));
		this.userActivityLogAccess.writeLog(userActions);

		if (classLogger.isInfoEnabled()) {
			classLogger.info("Leaving AddSubscriberEndpoint.addSubscriber()");
		}

		return response;
	}

	static RecipientModel parseModel(AddSubscriberRequest request, ConfigService configService, final SecurityContextAccess securityContextAccess) {
		if (classLogger.isInfoEnabled()) {
			classLogger.info("Parsing recipient model");
		}

		int companyId = securityContextAccess.getWebserviceUserCompanyId();

		RecipientModel model = new RecipientModel();
		model.setCompanyId(companyId);
		model.setDoubleCheck(request.isDoubleCheck());
		model.setKeyColumn(request.getKeyColumn());
		model.setOverwrite(request.isOverwrite());
		model.setParameters(Utils.toCaseInsensitiveMap(request.getParameters(), true));

		if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyId)) {
			model.getParameters().put(RecipientStandardField.DoNotTrack.getColumnName(), "1");
		}

		return model;
	}
}
