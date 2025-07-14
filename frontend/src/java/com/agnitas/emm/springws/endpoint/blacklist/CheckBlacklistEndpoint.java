/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.blacklist;

import java.util.Objects;

import org.agnitas.emm.core.blacklist.service.BlacklistModel;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.CheckBlacklistRequest;
import com.agnitas.emm.springws.jaxb.CheckBlacklistResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CheckBlacklistEndpoint extends BaseEndpoint {

	private final BlacklistService blacklistService;
	private final SecurityContextAccess securityContextAccess;

	public CheckBlacklistEndpoint(BlacklistService blacklistService, final SecurityContextAccess securityContextAccess) {
		this.blacklistService = Objects.requireNonNull(blacklistService, "blacklistService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "CheckBlacklistRequest")
	public @ResponsePayload CheckBlacklistResponse checkBlacklist(@RequestPayload CheckBlacklistRequest request) {
		final BlacklistModel model = new BlacklistModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setEmail(request.getEmail());
		
		final CheckBlacklistResponse response = new CheckBlacklistResponse();
		response.setValue(blacklistService.checkBlacklist(model));
		
		return response;
	}
}
