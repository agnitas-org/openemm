/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.usage;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

import com.agnitas.emm.springws.common.EndpointClassUtil;

public final class UsageInterceptor implements EndpointInterceptor {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UsageInterceptor.class);
	
	/** Usage logger. */
	private UsageLogger usageLogger;
	
	@Required
	public final void setUsageLogger(final UsageLogger logger) {
		this.usageLogger = Objects.requireNonNull(logger, "Logger cannot be null");
	}

	@Override
	public final void afterCompletion(MessageContext messageContext, final Object endpoint, Exception exception) throws Exception {
		// Nothing to do here
	}

	@Override
	public final boolean handleFault(final MessageContext messageContext, final Object endpoint) throws Exception {
		// Nothing to do here
		
		return true;	// Continue processing of Interceptor chain
	}

	@Override
	public final boolean handleRequest(final MessageContext messageContext, final Object endpoint) throws Exception {
		try {
			final ZonedDateTime now = ZonedDateTime.now();
			final String username = Utils.getUserName();
			final int companyID = Utils.getUserCompany();
			
			final Class<?> endpointClass = EndpointClassUtil.trueEndpointClass(endpoint);
			
			this.usageLogger.logWebserviceUsage(now, endpointClass.getCanonicalName(), companyID, username);
		} catch(final Exception e) {
			logger.warn("Error logging webservice usage", e);
		}

		return true;	// Continue processing of Interceptor chain

	}

	@Override
	public final boolean handleResponse(final MessageContext messageContext, final Object endpoint) throws Exception {
		// Nothing to do here

		return true;	// Continue processing of Interceptor chain
	}

}
