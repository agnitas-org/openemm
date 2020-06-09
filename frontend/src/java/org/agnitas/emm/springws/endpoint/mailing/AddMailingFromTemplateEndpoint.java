/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import javax.annotation.Resource;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddMailingFromTemplateRequest;
import org.agnitas.emm.springws.jaxb.AddMailingFromTemplateResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.mailing.service.MailingService;

public class AddMailingFromTemplateEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private MailingService mailingService;
	@Resource
	private ObjectFactory objectFactory;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		AddMailingFromTemplateRequest request = (AddMailingFromTemplateRequest) arg0;

		MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setTemplateId(request.getTemplateID());
		model.setShortname(request.getShortname());
		model.setDescription(request.getDescription());
		model.setAutoUpdate(request.isAutoUpdate());

		AddMailingFromTemplateResponse response = objectFactory.createAddMailingFromTemplateResponse();
		response.setMailingID(mailingService.addMailingFromTemplate(model));
		return response;
	}

}
