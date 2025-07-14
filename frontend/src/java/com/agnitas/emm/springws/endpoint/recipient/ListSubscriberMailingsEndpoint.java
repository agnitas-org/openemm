/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.recipient;

import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.endpoint.Utils;
import com.agnitas.emm.springws.jaxb.ListSubscriberMailingsRequest;
import com.agnitas.emm.springws.jaxb.ListSubscriberMailingsResponse;
import com.agnitas.emm.springws.jaxb.Map;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ListSubscriberMailingsEndpoint extends BaseEndpoint {

    private RecipientService recipientService;
    private SecurityContextAccess securityContextAccess;

    public ListSubscriberMailingsEndpoint(final RecipientService recipientService, final SecurityContextAccess securityContextAccess) {
        this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListSubscriberMailingsRequest")
    public @ResponsePayload ListSubscriberMailingsResponse listSubscriberMailings(@RequestPayload ListSubscriberMailingsRequest request) {
        ListSubscriberMailingsResponse response = new ListSubscriberMailingsResponse();

        RecipientModel model = parseModel(request);

        List<java.util.Map<String, Object>> mailings = recipientService.getSubscriberMailings(model);
        populateResponse(response, mailings);
        return response;
    }

    RecipientModel parseModel(ListSubscriberMailingsRequest request) {
        RecipientModel model = new RecipientModel();
        model.setCompanyId(this.securityContextAccess.getWebserviceUserCompanyId());
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
