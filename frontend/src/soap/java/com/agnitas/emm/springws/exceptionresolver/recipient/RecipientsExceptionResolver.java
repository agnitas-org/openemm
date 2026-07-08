/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exceptionresolver.recipient;

import com.agnitas.emm.core.recipient.exception.RecipientWrongRequestException;
import com.agnitas.emm.core.recipient.exception.RecipientsSizeLimitExceededExeption;
import com.agnitas.emm.springws.exceptionresolver.AbstractEmmExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import com.agnitas.emm.springws.exception.DuplicateKeyColumnValueException;

public class RecipientsExceptionResolver extends AbstractEmmExceptionResolver {

	@Override
	protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
       if (ex instanceof RecipientWrongRequestException) {
    	   final SoapFaultDefinition definition = getDefaultDefinition(ex);
           definition.setFaultStringOrReason("Data isn't correct: " + ex.getMessage());
           return definition;
       } else if (ex instanceof RecipientsSizeLimitExceededExeption) {
    	   final SoapFaultDefinition definition = getDefaultDefinition(ex);
           definition.setFaultStringOrReason(ex.getMessage());
           return definition;
       } else if (ex instanceof DuplicateKeyColumnValueException) {
           final SoapFaultDefinition definition = getDefaultDefinition(ex);
           definition.setFaultStringOrReason(ex.getMessage());
           return definition;
       }
       
       return super.getFaultDefinition(endpoint, ex);
	}
}
