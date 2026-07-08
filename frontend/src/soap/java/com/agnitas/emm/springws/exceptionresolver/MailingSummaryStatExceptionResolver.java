/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exceptionresolver;

import com.agnitas.emm.core.stat.service.impl.FailedArgumentsValidationException;
import com.agnitas.emm.core.stat.service.impl.SummaryStatJobNotExistException;
import com.agnitas.emm.core.stat.service.impl.TargetGroupsStringFormatException;
import com.agnitas.emm.core.stat.service.impl.UnknownRecipientsTypeException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

public class MailingSummaryStatExceptionResolver extends AbstractEmmExceptionResolver {

	@Override
	protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
		if (ex instanceof SummaryStatJobNotExistException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Unknown summary statistic job ID");
			return definition;
		}

		if (ex instanceof TargetGroupsStringFormatException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Error parsing target group list string argument.");
			return definition;
		}

		if (ex instanceof UnknownRecipientsTypeException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Unknown recipients type");
			return definition;
		}

		if (ex instanceof FailedArgumentsValidationException) {
			SoapFaultDefinition definition = getDefaultDefinition(ex);
			definition.setFaultStringOrReason("Invalid argument(s): " + ex.getMessage());
			return definition;
		}
		
		return super.getFaultDefinition(endpoint, ex);
	}
}
