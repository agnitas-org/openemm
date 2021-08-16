/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling;

import java.util.Locale;

import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.springws.common.EndpointClassUtil;
import com.agnitas.emm.springws.throttling.service.ThrottlingService;

/**
 * Interceptor to limit WS API calls.
 */
public class ThrottlingInterceptor implements SoapEndpointInterceptor {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ThrottlingInterceptor.class);
	
	/** Service for limiting WS calls. */
	private ThrottlingService throttlingService;

	/**
	 * Sets {@link ThrottlingService}.
	 * 
	 * @param throttlingService {@link ThrottlingService}
	 */
	public void setThrottlingService(ThrottlingService throttlingService) {
		this.throttlingService = throttlingService;
	}

	@Override
	public boolean handleFault(MessageContext paramMessageContext, Object paramObject) throws Exception {
		return true;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object paramObject) throws Exception {
		final WebserviceUserDetails webserviceUser = Utils.getWebserviceUserDetails();
		final String endpointName = EndpointClassUtil.endpointNameFromInstance(paramObject);
		
		try {
			if (!throttlingService.checkAndTrack(webserviceUser, endpointName)) {
				if(logger.isDebugEnabled()) {
					logger.debug(String.format("Intercepted! User: '%s', Endpoint: '%s'", webserviceUser.getUsername(), endpointName));
				}
				
				//TODO: exception resolver?
				throw new Exception("API call limit exceeded");
			}
		} catch(Exception ex) {
            SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
            response.addClientOrSenderFault(ex.getMessage(), Locale.ENGLISH);
            return false;
		}
		
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext paramMessageContext, Object paramObject) throws Exception {
		return true;
	}

	@Override
	public boolean understands(SoapHeaderElement header) {
		return true;
	}

	@Override
	public void afterCompletion(MessageContext arg0, Object arg1, Exception arg2) throws Exception {
		// do nothing
	}
}
