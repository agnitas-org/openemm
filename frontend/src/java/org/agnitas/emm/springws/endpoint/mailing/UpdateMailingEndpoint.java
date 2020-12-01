/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.UpdateMailingRequest;
import org.agnitas.emm.springws.jaxb.UpdateMailingRequest.TargetIDList;
import org.agnitas.emm.springws.jaxb.UpdateMailingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class UpdateMailingEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public UpdateMailingEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "UpdateMailingRequest")
	public @ResponsePayload UpdateMailingResponse updateMailing(@RequestPayload UpdateMailingRequest request) throws Exception {
		MailingModel model = new MailingModel();
		model.setMailingId(request.getMailingID());
		model.setCompanyId(Utils.getUserCompany());
		model.setShortname(request.getShortname());
		model.setDescription(request.getDescription());
		model.setMailinglistId(request.getMailinglistID());
		TargetIDList targetIDList = request.getTargetIDList();
		if (targetIDList != null) {
			model.setTargetIDList(targetIDList.getTargetID());
		}
		model.setTargetMode(request.getMatchTargetGroups());
		model.setMailingType(request.getMailingType());
		model.setSubject(request.getSubject());
		model.setSenderName(request.getSenderName());
		model.setSenderAddress(request.getSenderAddress());
		model.setReplyToName(request.getReplyToName());
		model.setReplyToAddress(request.getReplyToAddress());
		model.setCharset(request.getCharset());
		model.setLinefeed(request.getLinefeed());
		model.setFormatString(request.getFormat());
		model.setOnePixelString(request.getOnePixel());
//		model.setAutoUpdate(request.isAutoUpdate());

		List<UserAction> userActions = new ArrayList<>();
		mailingService.updateMailing(model, userActions);
		Utils.writeLog(userActivityLogService, userActions);

		return new UpdateMailingResponse();
	}
}
