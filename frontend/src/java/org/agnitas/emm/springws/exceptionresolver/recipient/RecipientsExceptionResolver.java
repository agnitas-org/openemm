/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.exceptionresolver.recipient;

import org.agnitas.emm.core.recipient.service.impl.RecipientWrongRequestException;
import org.agnitas.emm.core.recipient.service.impl.RecipientsSizeLimitExceededExeption;
import org.agnitas.emm.springws.exceptionresolver.CommonExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

public class RecipientsExceptionResolver extends CommonExceptionResolver {

	@Override
	protected SoapFaultDefinition getFaultDefinition(Object endpoint,
			Exception ex) {
       if (ex instanceof RecipientWrongRequestException) {
            SoapFaultDefinition definition = getDefaultDefinition(ex);
            definition.setFaultStringOrReason("Data isn't correct: " + ex.getMessage());
            return definition;
       } else if (ex instanceof RecipientsSizeLimitExceededExeption) {
           SoapFaultDefinition definition = getDefaultDefinition(ex);
           definition.setFaultStringOrReason(ex.getMessage());
           return definition;
       }
       return super.getFaultDefinition(endpoint, ex);
	}
}
