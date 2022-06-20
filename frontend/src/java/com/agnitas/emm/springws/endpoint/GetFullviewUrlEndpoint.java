/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.FullviewService;
import com.agnitas.emm.springws.jaxb.GetFullviewUrlRequest;
import com.agnitas.emm.springws.jaxb.GetFullviewUrlResponse;

@Endpoint
public class GetFullviewUrlEndpoint extends BaseEndpoint {
    
	private FullviewService fullviewService;

	public GetFullviewUrlEndpoint(FullviewService fullviewService) {
		this.fullviewService = fullviewService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "GetFullviewUrlRequest")
	public @ResponsePayload GetFullviewUrlResponse getFullviewUrl(@RequestPayload GetFullviewUrlRequest request) throws Exception {
    	final int companyID = Utils.getUserCompany();
    	final int mailingID = request.getMailingID();
    	final int customerID = request.getCustomerID();
    	final String formNameOrNull = request.getFormName();
    	
    	final String url = fullviewService.getFullviewUrl(companyID, mailingID, customerID, formNameOrNull);
    	
    	final GetFullviewUrlResponse response = new GetFullviewUrlResponse();
    	response.setUrl(url);
    	
    	return response;
    }
}
