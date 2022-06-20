/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.servicemail.SendServiceMailService;
import com.agnitas.emm.springws.exception.WebserviceNotAllowedException;
import com.agnitas.emm.springws.jaxb.SendServiceMailRequest;
import com.agnitas.emm.springws.jaxb.SendServiceMailResponse;

/**
 * Endpoint for webservice &quot;SendServiceMail&quot;.
 */
@Endpoint
public class SendServiceMailEndpoint extends BaseEndpoint {
	/** The logger. */
	private static final transient Logger classLogger = LogManager.getLogger(SendServiceMailEndpoint.class);
	
	/** Service for sending service mails. */
	private SendServiceMailService sendServiceMailingService;
	
	/** Service providing configuration. */
	private ConfigService configService;

	public SendServiceMailEndpoint(SendServiceMailService sendServiceMailingService, ConfigService configService) {
		this.sendServiceMailingService = sendServiceMailingService;
		this.configService = configService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "SendServiceMailRequest")
	public @ResponsePayload SendServiceMailResponse sendServiceMail(@RequestPayload SendServiceMailRequest request) throws Exception {
		if (Utils.getUserCompany() == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
    		throw new Exception("error.company.mailings.sent.forbidden");
    	} else {
			int companyID = Utils.getUserCompany();
			int actionID = request.getActionID();
			int customerID = request.getCustomerID();
			
			if(!configService.isWebserviceSendServiceMailingEnabled(companyID)) {
				throw new WebserviceNotAllowedException("SendServiceMailing");
			}
			
			if(classLogger.isInfoEnabled()) {
				classLogger.info(String.format("Sending service mail triggered by action (action ID: %d, customer ID %d, company ID %d)", actionID, customerID, companyID));
			}
			
			sendServiceMailingService.sendServiceMailByEmmAction(actionID, customerID, companyID);
			
			if(classLogger.isInfoEnabled()) {
				classLogger.info("Sending service mail triggered successfully");
			}
	
			return new SendServiceMailResponse();
    	}
	}
}
