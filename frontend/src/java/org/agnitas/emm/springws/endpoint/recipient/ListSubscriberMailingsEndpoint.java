/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListSubscriberMailingsRequest;
import org.agnitas.emm.springws.jaxb.ListSubscriberMailingsResponse;
import org.agnitas.emm.springws.jaxb.Map;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class ListSubscriberMailingsEndpoint extends AbstractMarshallingPayloadEndpoint {
    @Resource
    private ObjectFactory objectFactory;
    @Resource
    private RecipientService recipientService;

    @Override
    protected Object invokeInternal(Object arg0) throws Exception {
        ListSubscriberMailingsRequest request = (ListSubscriberMailingsRequest) arg0;
        ListSubscriberMailingsResponse response = objectFactory.createListSubscriberMailingsResponse();

        RecipientModel model = parseModel(request);

        List<java.util.Map<String, Object>> mailings = recipientService.getSubscriberMailings(model);
        populateResponse(response, mailings, objectFactory);
        return response;
    }

    static RecipientModel parseModel(ListSubscriberMailingsRequest request) {
        RecipientModel model = new RecipientModel();
        model.setCompanyId(Utils.getUserCompany());
        model.setCustomerId(request.getCustomerID());
        return model;
    }

    static void populateResponse(ListSubscriberMailingsResponse response, List<java.util.Map<String, Object>> mailings, ObjectFactory objectFactory) {
        ListSubscriberMailingsResponse.Items wrapper = new ListSubscriberMailingsResponse.Items();
        List<Map> items = wrapper.getItem();

        for (java.util.Map<String, Object> mailing : mailings) {
            items.add(Utils.toJaxbMap(mailing, objectFactory));
        }

        response.setItems(wrapper);
    }
}
