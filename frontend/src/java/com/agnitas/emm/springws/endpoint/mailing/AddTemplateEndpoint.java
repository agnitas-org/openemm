/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.mailing;

import java.util.Objects;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.AddTemplateRequest;
import com.agnitas.emm.springws.jaxb.AddTemplateRequest.TargetIDList;
import com.agnitas.emm.springws.jaxb.AddTemplateResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddTemplateEndpoint extends BaseEndpoint {

	private final ThumbnailService thumbnailService;
	private final MailingService mailingService;
	private final SecurityContextAccess securityContextAccess;

	public AddTemplateEndpoint(
			@Qualifier("MailingService") MailingService mailingService,
			ThumbnailService thumbnailService,
			SecurityContextAccess securityContextAccess
	) {
		this.mailingService = Objects.requireNonNull(mailingService, "mailingService");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddTemplateRequest")
	public @ResponsePayload AddTemplateResponse addTemplate(@RequestPayload AddTemplateRequest request) {
		final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
		
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
//		model.setAutoUpdate(request.isAutoUpdate());
		model.setTemplate(true);

		final int mailingID = mailingService.addMailing(model);
		
		thumbnailService.tryUpdateMailingThumbnailByWebservice(companyID, mailingID);

		final AddTemplateResponse response = new AddTemplateResponse();
		response.setTemplateID(mailingID);
		
		return response;
	}
}
