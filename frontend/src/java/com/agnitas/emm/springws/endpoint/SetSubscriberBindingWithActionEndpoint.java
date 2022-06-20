/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.binding.service.ComBindingService;
import com.agnitas.emm.springws.jaxb.SetSubscriberBindingWithActionRequest;
import com.agnitas.emm.springws.jaxb.SetSubscriberBindingWithActionResponse;

@Endpoint
public class SetSubscriberBindingWithActionEndpoint extends BaseEndpoint {
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(SetSubscriberBindingWithActionEndpoint.class);

	private ComBindingService bindingService;

	@Autowired
	public SetSubscriberBindingWithActionEndpoint(@Qualifier("BindingService") ComBindingService bindingService) {
		this.bindingService = bindingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "SetSubscriberBindingWithActionRequest")
	public @ResponsePayload SetSubscriberBindingWithActionResponse setSubscriberBindingWithAction(@RequestPayload SetSubscriberBindingWithActionRequest request) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info( "Entered SetSubscriberBindingWithActionEndpoint.setSubscriberBindingWithAction()");
		}
		
		SetSubscriberBindingWithActionResponse response = new SetSubscriberBindingWithActionResponse();
		
		if (logger.isInfoEnabled()) {
			logger.info( "Parsing binding model");
		}
		BindingModel model = new BindingModel();
		model.setCustomerId(request.getCustomerID());
		model.setCompanyId(Utils.getUserCompany());
		model.setMailinglistId(request.getMailinglistID());
		model.setMediatype(request.getMediatype());
		model.setStatus(request.getStatus());
		model.setUserType(request.getBindingType());
		model.setRemark(request.getRemark());
		model.setExitMailingId(request.getExitMailingID());
		model.setActionId(request.getActionID());
		
		boolean runActionAsynchronous = BooleanUtils.toBooleanDefaultIfNull(request.isRunActionAsynchronous(), false);

		if (logger.isInfoEnabled()) {
			logger.info( "Calling binding service layer");
		}
		response.setValue(bindingService.setBindingWithActionId(model, runActionAsynchronous));
		
		if (logger.isInfoEnabled()) {
			logger.info( "Leaving SetSubscriberBindingWithActionEndpoint.setSubscriberBindingWithAction()");
		}
		
		return response;
	}
}
