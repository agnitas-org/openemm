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
import java.util.stream.Collectors;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.endpoint.mailing.AddMailingFromTemplateEndpoint;
import org.agnitas.emm.springws.jaxb.UpdateContentBlockRequest;
import org.agnitas.emm.springws.jaxb.UpdateContentBlockResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.agnitas.emm.springws.util.UserActivityLogAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailingcontent.validator.impl.HtmlContentValidator;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;

@Endpoint
public class UpdateContentBlockEndpoint extends BaseEndpoint {

	private static final transient Logger LOGGER = LogManager.getLogger(AddMailingFromTemplateEndpoint.class);

	private final ThumbnailService thumbnailService;
	private final DynamicTagContentService dynamicTagContentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;
	private final UserActivityLogAccess userActivityLogAccess;
	private final HtmlContentValidator htmlContentValidator;
	
	public UpdateContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, final MailingEditableCheck mailingEditableCheck, final ThumbnailService thumbnailService, final SecurityContextAccess securityContextAccess, final UserActivityLogAccess userActivityLogAccess, final HtmlContentValidator htmlContentValidator) {
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService, "dynamicTagContentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.userActivityLogAccess = Objects.requireNonNull(userActivityLogAccess, "userActivityLogAccess");
		this.htmlContentValidator = Objects.requireNonNull(htmlContentValidator, "htmlContentValidator");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "UpdateContentBlockRequest")
	public @ResponsePayload UpdateContentBlockResponse updateContentBlock(@RequestPayload UpdateContentBlockRequest request) throws Exception {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		this.mailingEditableCheck.requireMailingForContentBlockEditable(request.getContentID(), companyID);
		
		final UpdateContentBlockResponse response = new UpdateContentBlockResponse();
		
		final ContentModel model = new ContentModel();
		model.setCompanyId(companyID);
		model.setContentId(request.getContentID());
		model.setTargetId(request.getTargetID());
		model.setOrder(request.getOrder());
		model.setContent(request.getContent());
		
		validateContent(model);

		List<UserAction> userActions = new ArrayList<>();
		dynamicTagContentService.updateContent(model, userActions);
		this.userActivityLogAccess.writeLog(userActions);

		try {
			final DynamicTagContent currentContent = this.dynamicTagContentService.getContent(model);
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, currentContent.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing containing content block", request.getContentID()), e);
		}

		return response;
	}
	
	private final void validateContent(final ContentModel model) throws InvalidMailingContentException {
		final List<String> invalidElements = this.htmlContentValidator.findInvalidTags(model.getContent());
		
		if(!invalidElements.isEmpty()) {
			throw new InvalidMailingContentException(
					String.format(
							"Content block contains invalid HTML tags: %s", 
							invalidElements.stream().collect(Collectors.joining(", "))));
		}
	}

}
