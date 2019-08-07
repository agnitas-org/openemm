/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dynname;

import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.core.dynname.service.DynamicTagNameService;
import org.agnitas.emm.core.dynname.service.NameModel;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesRequest;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse.ContentBlockName;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.DynamicTag;

@SuppressWarnings("deprecation")
public class ListContentBlockNamesEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private DynamicTagNameService dynamicTagNameService;
	@Resource
	private ObjectFactory objectFactory; 

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		ListContentBlockNamesRequest request = (ListContentBlockNamesRequest) arg0;
		ListContentBlockNamesResponse response = objectFactory.createListContentBlockNamesResponse();
		
		NameModel model = new NameModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		
		List<DynamicTag> list = dynamicTagNameService.getNameList(model);
		List<ContentBlockName> responseList = response.getContentBlockName();
		for (DynamicTag name : list) {
			ContentBlockName responseContentBlock = objectFactory.createListContentBlockNamesResponseContentBlockName();
			responseContentBlock.setNameID(name.getId());
			responseContentBlock.setName(name.getDynName());
			responseList.add(responseContentBlock);
		}
		
		return response;
	}

}
