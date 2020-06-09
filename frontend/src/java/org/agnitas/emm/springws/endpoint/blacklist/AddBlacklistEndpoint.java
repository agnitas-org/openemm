/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.blacklist;

import javax.annotation.Resource;

import org.agnitas.emm.core.blacklist.service.BlacklistModel;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddBlacklistRequest;
import org.agnitas.emm.springws.jaxb.AddBlacklistResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

public class AddBlacklistEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private BlacklistService blacklistService;
	@Resource
	private ObjectFactory objectFactory;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		AddBlacklistRequest request = (AddBlacklistRequest) arg0;
		AddBlacklistResponse response = objectFactory.createAddBlacklistResponse();
		
		BlacklistModel model = new BlacklistModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setEmail(request.getEmail());
		model.setReason(request.getReason());
		response.setValue(blacklistService.insertBlacklist(model));
		return response;
	}

}
