/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.blacklist;

import org.agnitas.emm.core.blacklist.service.BlacklistModel;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddBlacklistRequest;
import org.agnitas.emm.springws.jaxb.AddBlacklistResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddBlacklistEndpoint extends BaseEndpoint {

	private BlacklistService blacklistService;

	public AddBlacklistEndpoint(BlacklistService blacklistService) {
		this.blacklistService = blacklistService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddBlacklistRequest")
	public @ResponsePayload AddBlacklistResponse addBlacklist(@RequestPayload AddBlacklistRequest request) throws Exception {
		AddBlacklistResponse response = new AddBlacklistResponse();
		
		BlacklistModel model = new BlacklistModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setEmail(request.getEmail());
		model.setReason(request.getReason());
		response.setValue(blacklistService.insertBlacklist(model));
		return response;
	}
}
