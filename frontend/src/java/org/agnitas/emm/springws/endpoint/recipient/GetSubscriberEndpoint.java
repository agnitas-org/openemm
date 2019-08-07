/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.Map;

import javax.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetSubscriberRequest;
import org.agnitas.emm.springws.jaxb.GetSubscriberResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.util.CaseInsensitiveSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class GetSubscriberEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private RecipientService recipientService;
	@Resource
	private ObjectFactory objectFactory;
	
	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		GetSubscriberRequest request = (GetSubscriberRequest) arg0;
		GetSubscriberResponse response = objectFactory.createGetSubscriberResponse();

		RecipientModel model = parseModel(request);

		// Validate requested profile fields
		recipientService.checkColumnsAvailable(model);

		Map<String, Object> parameters = recipientService.getSubscriber(model);
		populateResponse(request, response, parameters, objectFactory);
		return response;
	}
	
	static RecipientModel parseModel(GetSubscriberRequest request) {
		RecipientModel model = new RecipientModel();

		model.setCompanyId(Utils.getUserCompany());
		model.setCustomerId(request.getCustomerID());

		GetSubscriberRequest.Profilefields profileFields = request.getProfilefields();
		if (profileFields != null && CollectionUtils.isNotEmpty(profileFields.getName())) {
			CaseInsensitiveSet columns = new CaseInsensitiveSet();
			for (String columnName : profileFields.getName()) {
				if (StringUtils.isNotEmpty(columnName)) {
					columns.add(columnName);
				}
			}
			model.setColumns(columns);
		}

		return model;
	}

	static void populateResponse(GetSubscriberRequest request, GetSubscriberResponse response, Map<String, Object> parameters, ObjectFactory objectFactory) {
		if (parameters != null && parameters.size() > 0) {
			response.setParameters(Utils.toJaxbMap(parameters, objectFactory));
			response.setCustomerID(request.getCustomerID());
		} else {
			response.setCustomerID(0);
		}
	}

}
