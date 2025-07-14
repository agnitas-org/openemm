/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.recipient;

import java.util.Map;
import java.util.Objects;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.endpoint.Utils;
import com.agnitas.emm.springws.jaxb.GetSubscriberRequest;
import com.agnitas.emm.springws.jaxb.GetSubscriberResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.util.CaseInsensitiveSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetSubscriberEndpoint extends BaseEndpoint {

	private final RecipientService recipientService;
	private final SecurityContextAccess securityContextAccess;

	public GetSubscriberEndpoint(final RecipientService recipientService, final SecurityContextAccess securityContextAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "GetSubscriberRequest")
	public @ResponsePayload GetSubscriberResponse getSubscriber(@RequestPayload GetSubscriberRequest request) {
		final RecipientModel model = parseModel(request, this.securityContextAccess);

		// Validate requested profile fields
		recipientService.checkColumnsAvailable(model);

		final Map<String, Object> parameters = recipientService.getSubscriber(model);
		
		final GetSubscriberResponse response = new GetSubscriberResponse();
		populateResponse(request, response, parameters);
		
		return response;
	}
	
	static RecipientModel parseModel(GetSubscriberRequest request, final SecurityContextAccess securityContextAccess) {
		final RecipientModel model = new RecipientModel();

		model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
		model.setCustomerId(request.getCustomerID());

		GetSubscriberRequest.Profilefields profileFields = request.getProfilefields();
		if (profileFields != null && CollectionUtils.isNotEmpty(profileFields.getName())) {
			CaseInsensitiveSet columns = new CaseInsensitiveSet();
			for (String columnName : profileFields.getName()) {
				if (StringUtils.isNotEmpty(columnName)) {
					columns.add(columnName);
				}
			}
			model.setColumns(columns);
		}

		return model;
	}

	static void populateResponse(GetSubscriberRequest request, GetSubscriberResponse response, Map<String, Object> parameters) {
		if (parameters != null && parameters.size() > 0) {
			response.setParameters(Utils.toJaxbMap(parameters));
			response.setCustomerID(request.getCustomerID());
		} else {
			response.setCustomerID(0);
		}
	}
}
