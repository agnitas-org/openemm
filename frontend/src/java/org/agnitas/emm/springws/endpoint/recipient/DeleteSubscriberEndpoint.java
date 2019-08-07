/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberRequest;
import org.agnitas.emm.springws.jaxb.DeleteSubscriberResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.service.UserActivityLogService;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class DeleteSubscriberEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private RecipientService recipientService;
	@Resource
	private ObjectFactory objectFactory;
	@Resource
	private UserActivityLogService userActivityLogService;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		DeleteSubscriberRequest request = (DeleteSubscriberRequest) arg0;
		DeleteSubscriberResponse response = objectFactory.createDeleteSubscriberResponse();

		RecipientModel model = parseModel(request);

		List<UserAction> userActions = new ArrayList<>();
		recipientService.deleteSubscriber(model, userActions);
		Utils.writeLog(userActivityLogService, userActions);

		return response;
	}

	static RecipientModel parseModel(DeleteSubscriberRequest request) {
		RecipientModel model = new RecipientModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setCustomerId(request.getCustomerID());
		return model;
	}

}
