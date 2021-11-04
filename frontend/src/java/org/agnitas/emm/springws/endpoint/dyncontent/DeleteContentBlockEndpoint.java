/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.exception.MailingNotEditableException;
import org.agnitas.emm.springws.jaxb.DeleteContentBlockRequest;
import org.agnitas.emm.springws.jaxb.DeleteContentBlockResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DeleteContentBlockEndpoint extends BaseEndpoint {

	private final DynamicTagContentService dynamicTagContentService;
	private final MailingEditableCheck mailingEditableCheck;

	public DeleteContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, final MailingEditableCheck mailingEditableCheck) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService);
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck);
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "DeleteContentBlockRequest")
	public @ResponsePayload DeleteContentBlockResponse deleteContentBlock(@RequestPayload DeleteContentBlockRequest request) throws MailingNotEditableException {
		final int companyId = Utils.getUserCompany();
		
		this.mailingEditableCheck.requireMailingForContentBlockEditable(request.getContentID(), companyId);
		
		final DeleteContentBlockResponse response = new DeleteContentBlockResponse();

		final ContentModel model = new ContentModel();
		model.setCompanyId(companyId);
		model.setContentId(request.getContentID());

		final List<UserAction> userActions = new ArrayList<>();
		response.setValue(dynamicTagContentService.deleteContent(model, userActions));
		Utils.writeLog(userActivityLogService, userActions);

		return response;
	}
}
