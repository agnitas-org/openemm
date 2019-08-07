/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.exceptionresolver;

import java.util.Locale;
import java.util.Set;

import org.agnitas.emm.core.binding.service.BindingNotExistException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentInvalid;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentNotExistException;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentWithSameOrderAlreadyExist;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentWithSameTargetIdAlreadyExist;
import org.agnitas.emm.core.dynname.service.DynamicTagNameNotExistException;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailing.service.SendDateNotInFutureException;
import org.agnitas.emm.core.mailing.service.TemplateNotExistException;
import org.agnitas.emm.core.mailing.service.WorldMailingAlreadySentException;
import org.agnitas.emm.core.mailing.service.WorldMailingWithoutNormalTypeException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.recipient.service.InvalidDataException;
import org.agnitas.emm.core.recipient.service.RecipientNotExistException;
import org.agnitas.emm.core.recipient.service.impl.ProfileFieldNotExistException;
import org.agnitas.emm.core.target.service.TargetNotExistException;
import org.agnitas.exceptions.FormNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.springws.WebserviceNotAllowedException;
import com.agnitas.emm.springws.endpoint.BulkSizeLimitExeededExeption;

public class CommonExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {
	
	@SuppressWarnings("hiding")
	private static final transient Logger logger = Logger.getLogger(CommonExceptionResolver.class);

    @SuppressWarnings("rawtypes")
	private Set mappedEndpoints;

	protected SoapFaultDefinition getDefaultDefinition(Exception ex) {
		if (!(ex instanceof InvalidDataException)) {
			logger.error("Exception", ex);
		} else {
        	// TODO: Log output in user error log
        }
		
        SoapFaultDefinition definition = new SoapFaultDefinition();
        definition.setLocale(Locale.getDefault());
        definition.setFaultCode(SoapFaultDefinition.SERVER);
        definition.setFaultStringOrReason(ex.getMessage());
        return definition;
	}
	
    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
    	SoapFaultDefinition definition = getDefaultDefinition(ex);
        if (ex instanceof IllegalArgumentException) {
            definition.setFaultCode(SoapFaultDefinition.CLIENT);
        } else if (ex instanceof org.springframework.dao.IncorrectResultSizeDataAccessException) {
        	// Nothing to do here
        } else if (ex instanceof org.springframework.dao.DataAccessException) {
            definition.setFaultStringOrReason("Data access error");
        } else if (ex instanceof MailingNotExistException) {
			definition.setFaultStringOrReason("Unknown mailing ID");
		} else if (ex instanceof DynamicTagContentNotExistException) {
			definition.setFaultStringOrReason("Unknown content ID");
		} else if (ex instanceof DynamicTagNameNotExistException) {
			definition.setFaultStringOrReason("Unknown block name");
		} else if (ex instanceof TargetNotExistException) {
			definition.setFaultStringOrReason("Unknown target id (" + ((TargetNotExistException)ex).getTargetID() + ")");
		} else if (ex instanceof MailinglistNotExistException) {
			definition.setFaultStringOrReason("Unknown mailinglist ID");
		} else if (ex instanceof TemplateNotExistException) {
			definition.setFaultStringOrReason("Unknown template ID");
		} else if (ex instanceof DynamicTagContentWithSameOrderAlreadyExist) {
			definition.setFaultStringOrReason("Content with the same order already exist");
		} else if (ex instanceof DynamicTagContentInvalid) {
			definition.setFaultStringOrReason("Invalid content: " + ex.getMessage());
		} else if (ex instanceof DynamicTagContentWithSameTargetIdAlreadyExist) {
			definition.setFaultStringOrReason("Content with the same target id already exist");
		} else if (ex instanceof RecipientNotExistException) {
            definition.setFaultStringOrReason("Unknown customer ID");
        } else if (ex instanceof BindingNotExistException) {
            definition.setFaultStringOrReason("Binding not exist");
        } else if (ex instanceof SendDateNotInFutureException) {
            definition.setFaultStringOrReason("Send date not in future");
        } else if (ex instanceof WorldMailingAlreadySentException) {
            definition.setFaultStringOrReason("World mailing already sent");
        } else if (ex instanceof WorldMailingWithoutNormalTypeException) {
            definition.setFaultStringOrReason("Mailing type needs to be Normal for World mailing.");
        } else if(ex instanceof InvalidUIDException) {
            definition.setFaultStringOrReason("Invalid UID");
        } else if (ex instanceof BulkSizeLimitExeededExeption) {
			definition.setFaultStringOrReason(ex.getMessage());
        } else if (ex instanceof WebserviceNotAllowedException) {
        	definition.setFaultStringOrReason("Webservice not allowed");
        } else if (ex instanceof ProfileFieldNotExistException) {
            definition.setFaultCode(SoapFaultDefinition.CLIENT);
            definition.setFaultStringOrReason("Requested profile field not exists: " + ex.getMessage());
        } else if (ex instanceof InvalidDataException) {
            definition.setFaultStringOrReason("Invalid data: " + ex.getMessage());
        } else if (ex instanceof TrackableLinkUnknownLinkIdException){
            definition.setFaultStringOrReason("Unknown link ID " + ((TrackableLinkUnknownLinkIdException) ex).getLinkId());
        } else if (ex instanceof FormNotFoundException) {
        	definition.setFaultStringOrReason("Unknown form name");
        } else {
            definition.setFaultStringOrReason("Unknown error");
        }
        return definition;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
	public void setMappedEndpoints(Set mappedEndpoints) {
    	super.setMappedEndpoints(mappedEndpoints);
        this.mappedEndpoints = mappedEndpoints;
    }
    
    public SoapFaultDefinition resolveException(Object endpoint, Exception ex) {
        Object mappedEndpoint = endpoint instanceof MethodEndpoint ? ((MethodEndpoint) endpoint).getBean() : endpoint;
        if (mappedEndpoints != null && !mappedEndpoints.contains(mappedEndpoint)) {
            return null;
        }
        return getFaultDefinition(mappedEndpoint, ex);
    }

}
