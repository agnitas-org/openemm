/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exceptionresolver.binding;

import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import com.agnitas.emm.springws.exceptionresolver.AbstractEmmExceptionResolver;
import com.agnitas.exception.InvalidUserStatusException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

public class BindingExceptionResolver extends AbstractEmmExceptionResolver {

	@Override
	protected SoapFaultDefinition getFaultDefinition(Object endpoint,
			Exception ex) {
		if (ex instanceof MailingNotExistException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Unknown exit mailing ID");
			return definition;
		} else if (ex instanceof UIDParseException 
				|| ex instanceof InvalidUIDException 
				|| ex instanceof DeprecatedUIDVersionException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Invalid UID");
			return definition;
		} else if (ex instanceof InvalidUserStatusException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Invalid user status");
			return definition;
		}
		return super.getFaultDefinition(endpoint, ex);
	}
}
