/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import org.agnitas.emm.springws.endpoint.Utils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.mailing.service.FullviewService;
import com.agnitas.emm.springws.jaxb.GetFullviewUrlRequest;
import com.agnitas.emm.springws.jaxb.GetFullviewUrlResponse;
import com.agnitas.emm.springws.jaxb.ObjectFactory;

public class GetFullviewUrlEndpoint extends AbstractMarshallingPayloadEndpoint {
    
	private ObjectFactory objectFactory;
	
	private FullviewService fullviewService;
	
    @Override
	protected final Object invokeInternal(final Object requestObject) throws Exception {
    	final GetFullviewUrlRequest request = (GetFullviewUrlRequest) requestObject;
    	
    	final int companyID = Utils.getUserCompany();
    	final int mailingID = request.getMailingID();
    	final int customerID = request.getCustomerID();
    	final String formNameOrNull = request.getFormName();
    	
    	final String url = this.fullviewService.getFullviewUrl(companyID, mailingID, customerID, formNameOrNull);
    	
    	final GetFullviewUrlResponse response = this.objectFactory.createGetFullviewUrlResponse();
    	response.setUrl(url);
    	
    	return response;
    }
    
    @Required
    public final void setObjectFactory(final ObjectFactory objectFactory) {
        this.objectFactory = Objects.requireNonNull(objectFactory, "Object factory cannot be null");
    }
    
    @Required
    public final void setFullviewService(final FullviewService service) {
    	this.fullviewService = Objects.requireNonNull(service, "Fullview service cannot be null");
    }
}
