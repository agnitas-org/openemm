/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.List;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListSubscriberMailingsRequest;
import org.agnitas.emm.springws.jaxb.ListSubscriberMailingsResponse;
import org.agnitas.emm.springws.jaxb.Map;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListSubscriberMailingsEndpoint extends BaseEndpoint {

    private RecipientService recipientService;

    public ListSubscriberMailingsEndpoint(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListSubscriberMailingsRequest")
    public @ResponsePayload ListSubscriberMailingsResponse listSubscriberMailings(@RequestPayload ListSubscriberMailingsRequest request) {
        ListSubscriberMailingsResponse response = new ListSubscriberMailingsResponse();

        RecipientModel model = parseModel(request);

        List<java.util.Map<String, Object>> mailings = recipientService.getSubscriberMailings(model);
        populateResponse(response, mailings);
        return response;
    }

    static RecipientModel parseModel(ListSubscriberMailingsRequest request) {
        RecipientModel model = new RecipientModel();
        model.setCompanyId(Utils.getUserCompany());
        model.setCustomerId(request.getCustomerID());
        return model;
    }

    static void populateResponse(ListSubscriberMailingsResponse response, List<java.util.Map<String, Object>> mailings) {
        ListSubscriberMailingsResponse.Items wrapper = new ListSubscriberMailingsResponse.Items();
        List<Map> items = wrapper.getItem();

        for (java.util.Map<String, Object> mailing : mailings) {
            items.add(Utils.toJaxbMap(mailing));
        }

        response.setItems(wrapper);
    }
}
