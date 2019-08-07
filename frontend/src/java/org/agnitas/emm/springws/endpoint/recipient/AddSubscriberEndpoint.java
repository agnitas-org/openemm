/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddSubscriberRequest;
import org.agnitas.emm.springws.jaxb.AddSubscriberResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.service.UserActivityLogService;
import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class AddSubscriberEndpoint extends AbstractMarshallingPayloadEndpoint {

	/** The logger. */
	@SuppressWarnings("hiding")
	private static final transient Logger logger = Logger.getLogger( AddSubscriberEndpoint.class);
	
	@Resource
	private RecipientService recipientService;
	@Resource
	private ObjectFactory objectFactory;
	@Resource
	private UserActivityLogService userActivityLogService;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		if( logger.isInfoEnabled()) {
			logger.info( "Entered AddSubscriberEndpoint.invokeInternal()");
		}
			
		AddSubscriberRequest request = (AddSubscriberRequest) arg0;
		AddSubscriberResponse response = objectFactory.createAddSubscriberResponse();
		
		RecipientModel model = parseModel(request);
		
		if( logger.isInfoEnabled()) {
			logger.info( "Calling recipient service layer");
		}
		String username = Utils.getUserName();
		int companyID = Utils.getUserCompany();
		List<UserAction> userActions = new ArrayList<>();
		response.setCustomerID(recipientService.addSubscriber(model, username, companyID, userActions));
		Utils.writeLog(userActivityLogService, userActions);

		if( logger.isInfoEnabled()) {
			logger.info( "Leaving AddSubscriberEndpoint.invokeInternal()");
		}

		return response;
	}
	
	static RecipientModel parseModel(AddSubscriberRequest request) {
		if( logger.isInfoEnabled()) {
			logger.info( "Parsing recipient model");
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
