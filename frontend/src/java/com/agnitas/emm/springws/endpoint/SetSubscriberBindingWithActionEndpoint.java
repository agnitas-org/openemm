/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import com.agnitas.emm.core.binding.service.BindingService;
import com.agnitas.emm.springws.jaxb.extended.SetSubscriberBindingWithActionRequest;
import com.agnitas.emm.springws.jaxb.extended.SetSubscriberBindingWithActionResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.emm.core.binding.service.BindingModel;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class SetSubscriberBindingWithActionEndpoint extends BaseEndpoint {

	private static final Logger logger = LogManager.getLogger(SetSubscriberBindingWithActionEndpoint.class);

	private final BindingService bindingService;
	private final SecurityContextAccess securityContextAccess;

	@Autowired
	public SetSubscriberBindingWithActionEndpoint(@Qualifier("BindingService") BindingService bindingService, final SecurityContextAccess securityContextAccess) {
		this.bindingService = Objects.requireNonNull(bindingService, "bindingService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_COM, localPart = "SetSubscriberBindingWithActionRequest")
	public @ResponsePayload SetSubscriberBindingWithActionResponse setSubscriberBindingWithAction(@RequestPayload SetSubscriberBindingWithActionRequest request) {
		logger.info("Entered SetSubscriberBindingWithActionEndpoint.setSubscriberBindingWithAction()");
		logger.info("Parsing binding model");

		final BindingModel model = new BindingModel();
		model.setCustomerId(request.getCustomerID());
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setMailinglistId(request.getMailinglistID());
		model.setMediatype(request.getMediatype());
		model.setStatus(request.getStatus());
		model.setUserType(request.getBindingType());
		model.setRemark(request.getRemark());
		model.setExitMailingId(request.getExitMailingID());
		model.setActionId(request.getActionID());
		
		boolean runActionAsynchronous = BooleanUtils.toBooleanDefaultIfNull(request.isRunActionAsynchronous(), false);

		logger.info("Calling binding service layer");

		final SetSubscriberBindingWithActionResponse response = new SetSubscriberBindingWithActionResponse();
		response.setValue(bindingService.setBindingWithActionId(model, runActionAsynchronous));
		
		logger.info("Leaving SetSubscriberBindingWithActionEndpoint.setSubscriberBindingWithAction()");

		return response;
	}
}
