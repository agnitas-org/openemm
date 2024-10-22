/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.binding;

import java.util.Objects;

import org.agnitas.emm.core.binding.service.BindingModel;
import com.agnitas.emm.core.binding.service.BindingService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberBindingRequest;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberBindingResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteSubscriberBindingEndpoint extends BaseEndpoint {

	private final BindingService bindingService;
	private final SecurityContextAccess securityContextAccess;

	public DeleteSubscriberBindingEndpoint(@Qualifier("BindingService") BindingService bindingService, final SecurityContextAccess securityContextAccess) {
		this.bindingService = Objects.requireNonNull(bindingService, "bindingService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DeleteSubscriberBindingRequest")
	public @ResponsePayload DeleteSubscriberBindingResponse deleteSubscriberBinding(@RequestPayload DeleteSubscriberBindingRequest request) throws Exception {
		final BindingModel model = parseModel(request, this.securityContextAccess);
		bindingService.deleteBinding(model);
		
		final DeleteSubscriberBindingResponse response = new DeleteSubscriberBindingResponse();
		return response;
	}
	
	static BindingModel parseModel(DeleteSubscriberBindingRequest request, final SecurityContextAccess securityContextAccess) {
		final BindingModel model = new BindingModel();
		model.setCustomerId(request.getCustomerID());
		model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
		model.setMailinglistId(request.getMailinglistID());
		model.setMediatype(request.getMediatype());
		
		return model;
	}
}
