/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddContentBlockRequest;
import org.agnitas.emm.springws.jaxb.AddContentBlockResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddContentBlockEndpoint extends BaseEndpoint {

	private DynamicTagContentService dynamicTagContentService;

	public AddContentBlockEndpoint(DynamicTagContentService dynamicTagContentService) {
		this.dynamicTagContentService = dynamicTagContentService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddContentBlockRequest")
	public @ResponsePayload AddContentBlockResponse addContentBlock(@RequestPayload AddContentBlockRequest request) throws Exception {
		AddContentBlockResponse response = new AddContentBlockResponse();
		
		ContentModel model = new ContentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		model.setBlockName(request.getBlockName());
		model.setTargetId(request.getTargetID());
		model.setOrder(request.getOrder());
		model.setContent(request.getContent());

		List<UserAction> userActions = new ArrayList<>();
		response.setContentID(dynamicTagContentService.addContent(model, userActions));
		Utils.writeLog(userActivityLogService, userActions);

		return response;
	}
}
