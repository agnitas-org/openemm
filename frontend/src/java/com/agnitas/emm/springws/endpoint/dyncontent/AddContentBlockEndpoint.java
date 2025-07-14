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

import com.agnitas.emm.core.mailingcontent.validator.impl.HtmlContentValidator;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.AddContentBlockRequest;
import com.agnitas.emm.springws.jaxb.AddContentBlockResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.emm.springws.util.UserActivityLogAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddContentBlockEndpoint extends BaseEndpoint {

	private static final Logger LOGGER = LogManager.getLogger(AddContentBlockEndpoint.class);

	private final DynamicTagContentService dynamicTagContentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final ThumbnailService thumbnailService;
	private final SecurityContextAccess securityContextAccess;
	private final UserActivityLogAccess userActivityLogAccess;
	private final HtmlContentValidator htmlContentValidator;

	public AddContentBlockEndpoint(DynamicTagContentService dynamicTagContentService, MailingEditableCheck mailingEditableCheck,
								   ThumbnailService thumbnailService, SecurityContextAccess securityContextAccess, UserActivityLogAccess userActivityLogAccess,
								   HtmlContentValidator htmlContentValidator) {

		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentService, "dynamicTagContentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.userActivityLogAccess = Objects.requireNonNull(userActivityLogAccess, "userActivityLogAccess");
		this.htmlContentValidator = Objects.requireNonNull(htmlContentValidator, "htmlContentValidator");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddContentBlockRequest")
	public @ResponsePayload AddContentBlockResponse addContentBlock(@RequestPayload AddContentBlockRequest request) throws Exception {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		this.mailingEditableCheck.requireMailingEditable(request.getMailingID(), companyID);
		
		final AddContentBlockResponse response = new AddContentBlockResponse();
		
		final ContentModel model = new ContentModel();
		model.setCompanyId(companyID);
		model.setMailingId(request.getMailingID());
		model.setBlockName(request.getBlockName());
		model.setTargetId(request.getTargetID());
		model.setOrder(request.getOrder());
		model.setContent(request.getContent());
		
		validateContent(model);

		final List<UserAction> userActions = new ArrayList<>();
		response.setContentID(dynamicTagContentService.addContent(model, userActions));
		this.userActivityLogAccess.writeLog(userActions);
		
		try {
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, request.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing %d", request.getMailingID()), e);
		}

		return response;
	}
	
	private void validateContent(final ContentModel model) throws InvalidMailingContentException {
		final List<String> invalidElements = this.htmlContentValidator.findIllegalTags(model.getContent());
		
		if(!invalidElements.isEmpty()) {
			throw new InvalidMailingContentException(
					String.format(
							"Content block contains invalid HTML tags: %s",
                            String.join(", ", invalidElements)));
		}
	}
}
