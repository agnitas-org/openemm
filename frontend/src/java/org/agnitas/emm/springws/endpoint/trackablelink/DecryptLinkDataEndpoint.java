/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.trackablelink;

import java.util.Objects;

import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.DecryptLinkDataRequest;
import org.agnitas.emm.springws.jaxb.DecryptLinkDataResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

@Endpoint
public class DecryptLinkDataEndpoint extends BaseEndpoint {

	private final ExtensibleUIDService uidService;
	private final SecurityContextAccess securityContextAccess;

	public DecryptLinkDataEndpoint(final ExtensibleUIDService uidService, final SecurityContextAccess securityContextAccess) {
		this.uidService = Objects.requireNonNull(uidService, "uidService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DecryptLinkDataRequest")
	public @ResponsePayload DecryptLinkDataResponse decryptLinkData(@RequestPayload DecryptLinkDataRequest request) throws Exception {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		final ComExtensibleUID uid = uidService.parse(request.getLinkparam());
		
		if(uid.getCompanyID() == companyID) {
			final DecryptLinkDataResponse response = new DecryptLinkDataResponse();
			
			response.setCompanyID(companyID);
			response.setCustomerID(uid.getCustomerID());
			response.setMailingID(uid.getMailingID());
			response.setUrlID(uid.getUrlID());
	
			return response;
		} else {
			throw new UIDParseException("Unable to parse UID", request.getLinkparam());
		}
	}
}
