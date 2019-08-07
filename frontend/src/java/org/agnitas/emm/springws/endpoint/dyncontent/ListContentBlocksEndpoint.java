/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.List;

import javax.annotation.Resource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListContentBlocksRequest;
import org.agnitas.emm.springws.jaxb.ListContentBlocksResponse;
import org.agnitas.emm.springws.jaxb.ListContentBlocksResponse.ContentBlock;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class ListContentBlocksEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private DynamicTagContentService dynamicTagContentService;
	@Resource
	private ObjectFactory objectFactory; 

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		ListContentBlocksRequest request = (ListContentBlocksRequest) arg0;
		ListContentBlocksResponse response = objectFactory.createListContentBlocksResponse();
		
		ContentModel model = new ContentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		
		List<DynamicTagContent> list = dynamicTagContentService.getContentList(model);
		List<ContentBlock> responseList = response.getContentBlock();
		for (DynamicTagContent content : list) {
			ContentBlock responseContentBlock = objectFactory.createListContentBlocksResponseContentBlock();
			responseContentBlock.setContentID(content.getId());
			responseContentBlock.setName(content.getDynName());
			responseContentBlock.setTargetID(content.getTargetID());
			responseContentBlock.setOrder(content.getDynOrder());
			responseList.add(responseContentBlock);
		}
		
		return response;
	}

}
