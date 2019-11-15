/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.servicemail.SendServiceMailService;
import com.agnitas.emm.springws.WebserviceNotAllowedException;
import com.agnitas.emm.springws.jaxb.ObjectFactory;
import com.agnitas.emm.springws.jaxb.SendServiceMailRequest;

/**
 * Endpoint for webservice &quot;SendServiceMail&quot;.
 */
@SuppressWarnings("deprecation")
public class SendServiceMailEndpoint extends AbstractMarshallingPayloadEndpoint {

	/** The logger. */
	@SuppressWarnings("hiding")
	private static final transient Logger logger = Logger.getLogger(SendServiceMailEndpoint.class);
	
	/** Service for sending service mails. */
	private SendServiceMailService sendServiceMailingService;
	
	/** Service providing configuration. */
	private ConfigService configService;
	
	/** Object factory for response objects. */
	private ObjectFactory objectFactory;

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		if (Utils.getUserCompany() == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
    		throw new Exception("error.company.mailings.sent.forbidden");
    	} else {
			SendServiceMailRequest request = (SendServiceMailRequest) requestObject;
	
			int companyID = Utils.getUserCompany();
			int actionID = request.getActionID();
			int customerID = request.getCustomerID();
			
			if(!configService.isWebserviceSendServiceMailingEnabled(companyID)) {
				throw new WebserviceNotAllowedException("SendServiceMailing");
			}
			
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Sending service mail triggered by action (action ID: %d, customer ID %d, company ID %d)", actionID, customerID, companyID));
			}
			
			sendServiceMailingService.sendServiceMailByEmmAction(actionID, customerID, companyID);
			
			if(logger.isInfoEnabled()) {
				logger.info("Sending service mail triggered successfully");
			}
	
			return objectFactory.createSendServiceMailResponse();
    	}
	}

	/**
	 * Set service for sending service mails.
	 * 
	 * @param service service for sending service mails
	 */
	@Required
	public void setSendServiceMailService(SendServiceMailService service) {
		this.sendServiceMailingService = service;
	}
	
	/**
	 * Set object factory.
	 * 
	 * @param factory object factory
	 */
	@Required
	public void setObjectFactory(ObjectFactory factory) {
		this.objectFactory = factory;
	}
	
	/**
	 * Set service providing configuration data.
	 * 
	 * @param service service providing configuration data
	 */
	@Required
	public void setConfigService(ConfigService service) {
		this.configService = service;
	}
}
