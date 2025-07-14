/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.common;

import org.springframework.ws.server.endpoint.MethodEndpoint;

public final class EndpointClassUtil {

	private static final String ENDPOINT_SUFFIX = "Endpoint"; 

	/**
	 * Returns the true endpoint class.
	 * 
	 * @param endpoint endpoint
	 * 
	 * @return true endpoint class
	 */
	public static Class<?> trueEndpointClass(final Object endpoint) {
		if (endpoint instanceof MethodEndpoint) {
			final MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
			
			return methodEndpoint.getBean().getClass();
		} else {
			return endpoint.getClass();
		}
	}
	
	/**
	 * Returns the endpoint name for given endpoint instance.
	 * 
	 * @param endpoint endpoint
	 * 
	 * @return endpoint name

	 * @throws IllegalArugmentException if name of true endpoint class of given instance does not end with {@value #ENDPOINT_SUFFIX}
	 */
	public static String endpointNameFromInstance(final Object endpoint) {
		final Class<?> clazz = trueEndpointClass(endpoint);

		return endpointNameFromClass(clazz);
	}
	
	/**
	 * Returns the endpoint name from given endpoint class.
	 * 
	 * @param clazz endpoint class
	 * 
	 * @return endpoint name
	 * 
	 * @throws IllegalArugmentException if name of given class does not end with <i>{@value #ENDPOINT_SUFFIX}</i>.
	 */
	public static String endpointNameFromClass(final Class<?> clazz) {
		final String className = clazz.getSimpleName();
		
		if(!className.endsWith(ENDPOINT_SUFFIX))
			throw new IllegalArgumentException(String.format("Class name does not end with '%s': %s", ENDPOINT_SUFFIX, className));
		
		return className.substring(0, className.length() - ENDPOINT_SUFFIX.length());
	}
}
