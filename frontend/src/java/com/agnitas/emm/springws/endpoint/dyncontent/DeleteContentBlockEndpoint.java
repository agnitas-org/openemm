/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.dyncontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.exception.MailingNotEditableException;
import com.agnitas.emm.springws.jaxb.DeleteContentBlockRequest;
import com.agnitas.emm.springws.jaxb.DeleteContentBlockResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.emm.springws.util.UserActivityLogAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.thumbnails.service.ThumbnailService;

@Endpoint
public class DeleteContentBlockEndpoint extends BaseEndpoint {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(DeleteContentBlockEndpoint.class);

	private final ThumbnailService thumbnailService;
	private final DynamicTagContentService dynamicTagContentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;
	private final UserActivityLogAccess userActivityLogAccess;

	public DeleteContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, final MailingEditableCheck mailingEditableCheck, final ThumbnailService thumbnailService, final SecurityContextAccess securityContextAccess, final UserActivityLogAccess userActivityLogAccess) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService, "dynamicTagContentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.userActivityLogAccess = Objects.requireNonNull(userActivityLogAccess, "userActivityLogAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "DeleteContentBlockRequest")
	public @ResponsePayload DeleteContentBlockResponse deleteContentBlock(@RequestPayload DeleteContentBlockRequest request) throws MailingNotEditableException {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		this.mailingEditableCheck.requireMailingForContentBlockEditable(request.getContentID(), companyID);
		

		final ContentModel model = new ContentModel();
		model.setCompanyId(companyID);
		model.setContentId(request.getContentID());
		

		final DeleteContentBlockResponse response = new DeleteContentBlockResponse();
		final List<UserAction> userActions = new ArrayList<>();
		response.setValue(dynamicTagContentService.deleteContent(model, userActions));
		this.userActivityLogAccess.writeLog(userActions);

		try {
			final DynamicTagContent currentContent = this.dynamicTagContentService.getContent(model);
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, currentContent.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing containing content block", request.getContentID()), e);
		}

		return response;
	}
}
