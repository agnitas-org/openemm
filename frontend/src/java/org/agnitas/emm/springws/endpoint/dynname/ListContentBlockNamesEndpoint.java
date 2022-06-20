/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dynname;

import java.util.List;

import org.agnitas.emm.core.dynname.service.DynamicTagNameService;
import org.agnitas.emm.core.dynname.service.NameModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesRequest;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse.ContentBlockName;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.DynamicTag;

@Endpoint
public class ListContentBlockNamesEndpoint extends BaseEndpoint {

	private DynamicTagNameService dynamicTagNameService;

	public ListContentBlockNamesEndpoint(DynamicTagNameService dynamicTagNameService) {
		this.dynamicTagNameService = dynamicTagNameService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListContentBlockNamesRequest")
	public  @ResponsePayload ListContentBlockNamesResponse listContentBlockNames(@RequestPayload ListContentBlockNamesRequest request) {
		ListContentBlockNamesResponse response = new ListContentBlockNamesResponse();
		
		NameModel model = new NameModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailingId(request.getMailingID());
		
		List<DynamicTag> list = dynamicTagNameService.getNameList(model);
		List<ContentBlockName> responseList = response.getContentBlockName();
		for (DynamicTag name : list) {
			ContentBlockName responseContentBlock = new ListContentBlockNamesResponse.ContentBlockName();
			responseContentBlock.setNameID(name.getId());
			responseContentBlock.setName(name.getDynName());
			responseList.add(responseContentBlock);
		}
		
		return response;
	}
}
