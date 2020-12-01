/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.binding;

import org.agnitas.beans.BindingEntry;
import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.core.binding.service.BindingService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListSubscriberBindingRequest;
import org.agnitas.emm.springws.jaxb.ListSubscriberBindingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListSubscriberBindingEndpoint extends BaseEndpoint {

	private BindingService bindingService;

	public ListSubscriberBindingEndpoint(@Qualifier("BindingService") BindingService bindingService) {
		this.bindingService = bindingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListSubscriberBindingRequest")
	public @ResponsePayload ListSubscriberBindingResponse listSubscriberBinding(@RequestPayload ListSubscriberBindingRequest request) {
		ListSubscriberBindingResponse response = new ListSubscriberBindingResponse();
		
		BindingModel model = parseModel(request);
        if (request.isUseISODateFormat() == null) {
            request.setUseISODateFormat(false);
        }

		for (BindingEntry binding : bindingService.getBindings(model)) {
			response.getItem().add(new SubscriberBindingResponseBuilder().createResponse(binding, request.isUseISODateFormat()));
		}
			
		return response;
	}

	public static BindingModel parseModel(ListSubscriberBindingRequest request) {
		BindingModel model = new BindingModel();
		model.setCustomerId(request.getCustomerID());
		model.setCompanyId(Utils.getUserCompany());
		return model;
	}
}
