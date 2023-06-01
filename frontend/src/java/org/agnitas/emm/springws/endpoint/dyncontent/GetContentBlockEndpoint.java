/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.Objects;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.GetContentBlockRequest;
import org.agnitas.emm.springws.jaxb.GetContentBlockResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetContentBlockEndpoint extends BaseEndpoint {

	private final DynamicTagContentService dynamicTagContentService;
	private final SecurityContextAccess securityContextAccess;

	public GetContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, final SecurityContextAccess securityContextAccess) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService, "dynamicTagContentService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "GetContentBlockRequest")
	public @ResponsePayload GetContentBlockResponse getContentBlock(@RequestPayload GetContentBlockRequest request) {
		final ContentModel model = new ContentModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setContentId(request.getContentID());
		
		final GetContentBlockResponse response = new GetContentBlockResponse();
		final DynamicTagContent content = dynamicTagContentService.getContent(model);
		response.setContentID(content.getId());
		response.setName(content.getDynName());
		response.setTargetID(content.getTargetID());
		response.setOrder(content.getDynOrder());
		response.setContent(content.getDynContent());
		response.setMailingID(content.getMailingID());
		
		return response;
	}
}
