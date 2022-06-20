/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling;

import java.util.Locale;

import org.agnitas.emm.springws.endpoint.Utils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.springws.common.EndpointClassUtil;
import com.agnitas.emm.util.quota.api.QuotaService;

/**
 * Interceptor to limit WS API calls.
 */
public class ThrottlingInterceptor implements SoapEndpointInterceptor {
	
	/** Service for limiting WS calls. */
	private QuotaService throttlingService;

	/**
	 * Sets {@link QuotaService}.
	 * 
	 * @param throttlingService {@link QuotaService}
	 */
	public void setThrottlingService(QuotaService throttlingService) {
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
			throttlingService.checkAndTrack(webserviceUser.getUsername(), webserviceUser.getCompanyID(), endpointName);
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
