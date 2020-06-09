/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import javax.annotation.Resource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetContentBlockRequest;
import org.agnitas.emm.springws.jaxb.GetContentBlockResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

public class GetContentBlockEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private DynamicTagContentService dynamicTagContentService;
	@Resource
	private ObjectFactory objectFactory;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		GetContentBlockRequest request = (GetContentBlockRequest) arg0;
		GetContentBlockResponse response = objectFactory.createGetContentBlockResponse();
		
		ContentModel model = new ContentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setContentId(request.getContentID());
		
		DynamicTagContent content = dynamicTagContentService.getContent(model);
		response.setContentID(content.getId());
		response.setName(content.getDynName());
		response.setTargetID(content.getTargetID());
		response.setOrder(content.getDynOrder());
		response.setContent(content.getDynContent());
		response.setMailingID(content.getMailingID());
		
		return response;
	}

}
