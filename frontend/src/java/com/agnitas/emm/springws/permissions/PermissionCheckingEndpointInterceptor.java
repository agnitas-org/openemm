/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws.permissions;

import java.util.Locale;

import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.security.authorities.AllEndpointsAuthority;
import org.agnitas.emm.springws.security.authorities.EndpointAuthority;
import org.apache.log4j.Logger;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

import com.agnitas.emm.springws.common.EndpointClassUtil;

/**
 * Interceptor checking webservice permissions.
 * 
 * The required permission for an endpoint is derived from the simple name of the endpoint class by
 * cutting the <i>Endpoint</i> suffix.
 * Therefore the endpoint class must follow the form </i>&lt;endpoint-name>Endpoint</i>.
 * If the endpoint class name does not satisfy this pattern, the simple endpoint class name (class name without
 * package) is used.
 */
public final class PermissionCheckingEndpointInterceptor implements SoapEndpointInterceptor {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(PermissionCheckingEndpointInterceptor.class);

	@Override
	public final void afterCompletion(final MessageContext messageContext, final Object endpoint, final Exception arg2) throws Exception {
		// Nothing to do
	}

	@Override
	public final boolean handleFault(final MessageContext messageContext, final Object endpoint) throws Exception {
		return true;
	}

	@Override
	public final boolean handleRequest(final MessageContext messageContext, final Object endpoint) throws Exception {
		final String endpointName = permissionFromEndpointInstance(endpoint);
		
		if (Utils.isAuthorityGranted(new EndpointAuthority(endpointName)) || Utils.isAuthorityGranted(AllEndpointsAuthority.INSTANCE)) {
			return true;
		} else {
            final SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
            response.addClientOrSenderFault("Access to endpoint denied", Locale.ENGLISH);
			
			return false;
		}
	}
	
	private static final String permissionFromEndpointInstance(final Object endpoint) {
		return permissionFromEndpointClass(EndpointClassUtil.trueEndpointClass(endpoint));
	}
	
	private static final String permissionFromEndpointClass(final Class<?> clazz) {
		try {
			final String permission = EndpointClassUtil.endpointNameFromClass(clazz);
			
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Permission for endpoint class '%s' is '%s'", clazz.getCanonicalName(), permission));
			}
			
			return permission;
		} catch(final IllegalArgumentException e) {
			LOGGER.error(String.format("Could not derive permission name from endpoint class: %s", clazz.getCanonicalName()), e);
			
			// Fallback to simple class name
			return clazz.getSimpleName();
		}
	}

	@Override
	public final boolean handleResponse(final MessageContext messageContext, final Object endpoint) throws Exception {
		return true;
	}

	@Override
	public final boolean understands(final SoapHeaderElement header) {
		return true;
	}

}
