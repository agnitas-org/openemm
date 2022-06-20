/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.exceptionresolver;

import java.util.Locale;

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
import org.agnitas.emm.springws.exception.DateFormatException;
import org.agnitas.emm.springws.exception.InvalidFilterSettingsException;
import org.agnitas.emm.springws.exception.MailingNotEditableException;
import org.agnitas.emm.springws.exception.MissingKeyColumnOrValueException;
import org.agnitas.exceptions.FormNotFoundException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.oxm.MarshallingException;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.springws.exception.BulkDataSizeLimitExeededExeption;
import com.agnitas.emm.springws.exception.BulkSizeLimitExeededExeption;
import com.agnitas.emm.springws.exception.WebServiceFileDataEmptyException;
import com.agnitas.emm.springws.exception.WebserviceNotAllowedException;
import com.agnitas.emm.springws.subscriptionrejection.exceptions.SubscriptionRejectedException;

public abstract class AbstractEmmExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

	private static final transient Logger classLogger = LogManager.getLogger(AbstractEmmExceptionResolver.class);

    protected AbstractEmmExceptionResolver() {
        setOrder(1);
    }

    protected SoapFaultDefinition getDefaultDefinition(Exception ex) {
		if (!(ex instanceof InvalidDataException)) {
			classLogger.error("Exception", ex);
		} else {
        	// TODO: Log output in user error log
        }

        SoapFaultDefinition definition = new SoapFaultDefinition();
        definition.setLocale(Locale.getDefault());
        definition.setFaultCode(SoapFaultDefinition.SERVER);
        definition.setFaultStringOrReason(ex.getMessage());
        return definition;
	}

    protected Exception unwrap(Exception e) {
	    if (e instanceof MarshallingException) {
	        Throwable cause = ExceptionUtils.getRootCause(e);

	        if (cause instanceof Exception) {
	            return (Exception) cause;
            }
        }

	    return e;
    }

    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
	    ex = unwrap(ex);

    	SoapFaultDefinition definition = getDefaultDefinition(ex);
        definition.setFaultCode(SoapFaultDefinition.CLIENT);
    	
        if (ex instanceof IllegalArgumentException) {
        	// Nothing to do here
        } else if (ex instanceof IncorrectResultSizeDataAccessException) {
        	// Nothing to do here
        } else if (ex instanceof DataAccessException) {
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
        } else if (ex instanceof BulkDataSizeLimitExeededExeption) {
			definition.setFaultStringOrReason(ex.getMessage());
        } else if (ex instanceof WebserviceNotAllowedException) {
        	definition.setFaultStringOrReason("Webservice not allowed");
        } else if (ex instanceof ProfileFieldNotExistException) {
            definition.setFaultStringOrReason("Requested profile field not exists: " + ex.getMessage());
        } else if (ex instanceof InvalidDataException) {
            definition.setFaultStringOrReason("Invalid data: " + ex.getMessage());
        } else if (ex instanceof TrackableLinkUnknownLinkIdException){
            definition.setFaultStringOrReason("Unknown link ID " + ((TrackableLinkUnknownLinkIdException) ex).getLinkId());
        } else if (ex instanceof FormNotFoundException) {
        	definition.setFaultStringOrReason("Unknown form name");
        } else if(ex instanceof SubscriptionRejectedException) {
        	definition.setFaultStringOrReason("Subscription rejection by anti-spam rules.");
        } else if(ex instanceof WebServiceFileDataEmptyException) {
        	definition.setFaultStringOrReason("File is missing or empty.");
        } else if(ex instanceof DateFormatException) {
            definition.setFaultStringOrReason("Invalid date format");
        } else if(ex instanceof MailingNotEditableException) {
        	definition.setFaultStringOrReason(ex.getMessage());
        } else if(ex instanceof InvalidFilterSettingsException) {
        	definition.setFaultStringOrReason(ex.getMessage());
        } else if(ex instanceof MissingKeyColumnOrValueException) {
        	definition.setFaultStringOrReason("Key column or value missing or empty");
        } else {
            definition.setFaultStringOrReason("Unknown error");
        }
        return definition;
    }
}
