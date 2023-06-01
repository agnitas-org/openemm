/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dynname;

import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.dynname.service.DynamicTagNameService;
import org.agnitas.emm.core.dynname.service.NameModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesRequest;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse;
import org.agnitas.emm.springws.jaxb.ListContentBlockNamesResponse.ContentBlockName;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.DynamicTag;

@Endpoint
public class ListContentBlockNamesEndpoint extends BaseEndpoint {

	private final DynamicTagNameService dynamicTagNameService;
	private final SecurityContextAccess securityContextAccess;

	public ListContentBlockNamesEndpoint(DynamicTagNameService dynamicTagNameService, final SecurityContextAccess securityContextAccess) {
		this.dynamicTagNameService = Objects.requireNonNull(dynamicTagNameService, "dynamicTagNameService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListContentBlockNamesRequest")
	public  @ResponsePayload ListContentBlockNamesResponse listContentBlockNames(@RequestPayload ListContentBlockNamesRequest request) {
		final NameModel model = new NameModel();
		model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
		model.setMailingId(request.getMailingID());
		
		final ListContentBlockNamesResponse response = new ListContentBlockNamesResponse();
		final List<DynamicTag> list = dynamicTagNameService.getNameList(model);
		final List<ContentBlockName> responseList = response.getContentBlockName();
		for (final DynamicTag name : list) {
			ContentBlockName responseContentBlock = new ListContentBlockNamesResponse.ContentBlockName();
			responseContentBlock.setNameID(name.getId());
			responseContentBlock.setName(name.getDynName());
			responseList.add(responseContentBlock);
		}
		
		return response;
	}
}
