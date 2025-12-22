/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.recipient;

import java.util.Objects;

import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.exception.MissingKeyColumnOrValueException;
import com.agnitas.emm.springws.jaxb.FindSubscriberRequest;
import com.agnitas.emm.springws.jaxb.FindSubscriberResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.bval.util.StringUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class FindSubscriberEndpoint extends BaseEndpoint {

	private final RecipientService recipientService;
	private final SecurityContextAccess securityContextAccess;

	public FindSubscriberEndpoint(final RecipientService recipientService, final SecurityContextAccess securityContextAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "FindSubscriberRequest")
	public @ResponsePayload FindSubscriberResponse findSubscriber(@RequestPayload FindSubscriberRequest request) throws MissingKeyColumnOrValueException {
		if(StringUtils.isBlank(request.getKeyColumn()) || StringUtils.isBlank(request.getValue())) {
			throw new MissingKeyColumnOrValueException();
		}
		
		final FindSubscriberResponse response = new FindSubscriberResponse();
		response.setValue(recipientService.findSubscriber(this.securityContextAccess.getWebserviceUserCompanyId(), request.getKeyColumn(), request.getValue()));
		
		return response;
	}
}
