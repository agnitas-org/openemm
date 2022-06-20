/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.blacklist;

import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetBlacklistItemsRequest;
import org.agnitas.emm.springws.jaxb.GetBlacklistItemsResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetBlacklistItemsEndpoint extends BaseEndpoint {

	private BlacklistService blacklistService;

	public GetBlacklistItemsEndpoint(BlacklistService blacklistService) {
		this.blacklistService = blacklistService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "GetBlacklistItemsRequest")
	public  @ResponsePayload GetBlacklistItemsResponse getBlacklistItems(@RequestPayload GetBlacklistItemsRequest request) throws Exception {
		GetBlacklistItemsResponse response = new GetBlacklistItemsResponse();
		response.getEmail().addAll(blacklistService.getEmailList(Utils.getUserCompany()));
		return response;
	}
}
