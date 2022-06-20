/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.endpoint.mailing.AddMailingFromTemplateEndpoint;
import org.agnitas.emm.springws.jaxb.UpdateContentBlockRequest;
import org.agnitas.emm.springws.jaxb.UpdateContentBlockResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.thumbnails.service.ThumbnailService;

@Endpoint
public class UpdateContentBlockEndpoint extends BaseEndpoint {

	private static final transient Logger LOGGER = LogManager.getLogger(AddMailingFromTemplateEndpoint.class);

	private final ThumbnailService thumbnailService;
	private final DynamicTagContentService dynamicTagContentService;
	private final MailingEditableCheck mailingEditableCheck;

	public UpdateContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, final MailingEditableCheck mailingEditableCheck, final ThumbnailService thumbnailService) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService);
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck);
		this.thumbnailService = Objects.requireNonNull(thumbnailService);
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "UpdateContentBlockRequest")
	public @ResponsePayload UpdateContentBlockResponse updateContentBlock(@RequestPayload UpdateContentBlockRequest request) throws Exception {
		final int companyID = Utils.getUserCompany();
		
		this.mailingEditableCheck.requireMailingForContentBlockEditable(request.getContentID(), companyID);
		
		final UpdateContentBlockResponse response = new UpdateContentBlockResponse();
		
		final ContentModel model = new ContentModel();
		model.setCompanyId(companyID);
		model.setContentId(request.getContentID());
		model.setTargetId(request.getTargetID());
		model.setOrder(request.getOrder());
		model.setContent(request.getContent());

		List<UserAction> userActions = new ArrayList<>();
		dynamicTagContentService.updateContent(model, userActions);
		Utils.writeLog(userActivityLogService, userActions);

		try {
			final DynamicTagContent currentContent = this.dynamicTagContentService.getContent(model);
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, currentContent.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing containing content block", request.getContentID()), e);
		}

		return response;
	}
}
