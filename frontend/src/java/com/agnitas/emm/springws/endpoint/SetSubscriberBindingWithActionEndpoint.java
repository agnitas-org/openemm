/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import javax.annotation.Resource;

import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.binding.service.ComBindingService;
import com.agnitas.emm.springws.jaxb.ObjectFactory;
import com.agnitas.emm.springws.jaxb.SetSubscriberBindingWithActionRequest;
import com.agnitas.emm.springws.jaxb.SetSubscriberBindingWithActionResponse;

// not tested, leaved as an example of extension webservice
public class SetSubscriberBindingWithActionEndpoint extends AbstractMarshallingPayloadEndpoint {
	/** The logger. */
	private static final Logger classLogger = Logger.getLogger(SetSubscriberBindingWithActionEndpoint.class);

	@Resource
	private ComBindingService bindingService;
	@Resource
	private ObjectFactory comObjectFactory;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Entered SetSubscriberBindingWithActionEndpoint.invokeInternal()");
		}
		
		SetSubscriberBindingWithActionRequest request = (SetSubscriberBindingWithActionRequest) arg0;
		SetSubscriberBindingWithActionResponse response = comObjectFactory.createSetSubscriberBindingWithActionResponse();
		
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Parsing binding model");
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
		
		final boolean runActionAsynchronous = request.isRunActionAsynchronous() == null ? false : request.isRunActionAsynchronous();
		
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Calling binding service layer");
		}
		response.setValue( bindingService.setBindingWithActionId(model, runActionAsynchronous));
		
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving SetSubscriberBindingWithActionEndpoint.invokeInternal()");
		}
		
		return response;
	}

}
