/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.dyncontent;

import java.util.List;
import java.util.Objects;

import com.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.ListContentBlocksRequest;
import com.agnitas.emm.springws.jaxb.ListContentBlocksResponse;
import com.agnitas.emm.springws.jaxb.ListContentBlocksResponse.ContentBlock;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListContentBlocksEndpoint extends BaseEndpoint {

	private final DynamicTagContentService dynamicTagContentService;
	private final SecurityContextAccess securityContextAccess;

	public ListContentBlocksEndpoint(DynamicTagContentService dynamicTagContentService, final SecurityContextAccess securityContextAccess) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService, "dynamicTagContentService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListContentBlocksRequest")
	public @ResponsePayload ListContentBlocksResponse listContentBlocks(@RequestPayload ListContentBlocksRequest request) {
		final ContentModel model = new ContentModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setMailingId(request.getMailingID());
		
		final ListContentBlocksResponse response = new ListContentBlocksResponse();
		final List<DynamicTagContent> list = dynamicTagContentService.getContentList(model);
		final List<ContentBlock> responseList = response.getContentBlock();
		for (DynamicTagContent content : list) {
			ContentBlock responseContentBlock = new ListContentBlocksResponse.ContentBlock();
			responseContentBlock.setContentID(content.getId());
			responseContentBlock.setName(content.getDynName());
			responseContentBlock.setTargetID(content.getTargetID());
			responseContentBlock.setOrder(content.getDynOrder());
			responseList.add(responseContentBlock);
		}
		
		return response;
	}
}
