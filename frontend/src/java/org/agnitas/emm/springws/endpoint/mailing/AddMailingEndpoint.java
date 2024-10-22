/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import java.util.Objects;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.AddMailingRequest;
import org.agnitas.emm.springws.jaxb.AddMailingRequest.TargetIDList;
import org.agnitas.emm.springws.jaxb.AddMailingResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;

@Endpoint
public class AddMailingEndpoint extends BaseEndpoint {

	private static final Logger LOGGER = LogManager.getLogger(AddMailingEndpoint.class);

	private final ThumbnailService thumbnailService;
	private final MailingService mailingService;
	private final SecurityContextAccess securityContextAccess;

	public AddMailingEndpoint(@Qualifier("MailingService") MailingService mailingService, final ThumbnailService thumbnailService, final SecurityContextAccess securityContextAccess) {
		this.mailingService = Objects.requireNonNull(mailingService, "mailingService");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddMailingRequest")
	public @ResponsePayload AddMailingResponse addMailing(@RequestPayload AddMailingRequest request) throws Exception {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
		// Check for unallowed html tags
		try {
			HtmlChecker.checkForNoHtmlTags(request.getShortname());
		} catch(HtmlCheckerException e) {
			throw new RestfulClientException("Mailing name contains unallowed HTML tags");
		}
		try {
			HtmlChecker.checkForUnallowedHtmlTags(request.getDescription(), false);
		} catch(HtmlCheckerException e) {
			throw new RestfulClientException("Mailing description contains unallowed HTML tags");
		}
		
		final AddMailingResponse response = new AddMailingResponse();

		final MailingModel model = new MailingModel();
		model.setCompanyId(companyID);
		model.setShortname(request.getShortname());
		model.setDescription(request.getDescription());
		model.setMailinglistId(request.getMailinglistID());
		final TargetIDList targetIDList = request.getTargetIDList();
		if (targetIDList != null) {
			model.setTargetIDList(targetIDList.getTargetID());
		}
		model.setTargetMode(request.getMatchTargetGroups());
		model.setMailingType(MailingType.fromWebserviceCode(request.getMailingType()));
		model.setSubject(request.getSubject());
		model.setSenderName(request.getSenderName());
		model.setSenderAddress(request.getSenderAddress());
		model.setReplyToName(request.getReplyToName());
		model.setReplyToAddress(request.getReplyToAddress());
		model.setCharset(request.getCharset());
		model.setLinefeed(request.getLinefeed());
		model.setFormat(request.getFormat());
		model.setOnePixel(request.getOnePixel());
		model.setPlannedDate(request.getPlannedDate());
//		model.setAutoUpdate(request.isAutoUpdate());

		final int mailingID = mailingService.addMailing(model);
		response.setMailingID(mailingID);
		
		try {
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, mailingID);
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing %d", mailingID), e);
		}
		
		return response;
	}
}
