/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.RecipientsModel;
import org.agnitas.emm.core.recipient.service.impl.RecipientWrongRequestException;
import org.agnitas.emm.core.recipient.service.impl.RecipientsSizeLimitExceededExeption;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.Criteria;
import org.agnitas.emm.springws.jaxb.Equals;
import org.agnitas.emm.springws.jaxb.ListSubscribersRequest;
import org.agnitas.emm.springws.jaxb.ListSubscribersResponse;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

@Endpoint
public class ListSubscribersEndpoint extends BaseEndpoint {

	private final RecipientService recipientService;
	private final WebserviceUserService webserviceUserService;
	private final SecurityContextAccess securityContextAccess;

	public ListSubscribersEndpoint(RecipientService recipientService, WebserviceUserService webserviceUserService, final SecurityContextAccess securityContextAccess) {
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService");
		this.webserviceUserService = Objects.requireNonNull(webserviceUserService, "webserviceUserService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
	}

	@PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListSubscribersRequest")
	public @ResponsePayload ListSubscribersResponse listSubscribers(@RequestPayload ListSubscribersRequest request) {
		ListSubscribersResponse response = new ListSubscribersResponse();

		RecipientsModel model = parseModel(request);
		
		final String username = this.securityContextAccess.getWebserviceUserName();
		final int size = recipientService.getSubscribersSize(model);
        checkResultListSize(username, size);
	        
		List<Integer> recipientResultList = recipientService.getSubscribers(model);
		populateResponse(response, recipientResultList);
		return response;
	}
	
	RecipientsModel parseModel(ListSubscribersRequest request) {
	    Criteria criteria = request.getCriteria();
	    if (criteria == null) {
            throw new RecipientWrongRequestException("Criteria are empty.");
        }
	    
        RecipientsModel model = new RecipientsModel();
        model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
        List<RecipientsModel.CriteriaEquals> criteriaEqualsList = new ArrayList<>();
        model.setCriteriaEquals(criteriaEqualsList);
        model.setMatchAll(criteria.isMatchAll());

        List<Equals> equalsList = criteria.getEquals();
        for (Equals equals : equalsList) {
            criteriaEqualsList
                .add(new RecipientsModel.CriteriaEquals(
                        equals.getProfilefield(),
                        equals.getValue(),
                        equals.getDateformat())
                );
        }
		
		return model;
	}

	static void populateResponse(ListSubscribersResponse response, List<Integer> recipientResultList) {
		if (recipientResultList != null && recipientResultList.size() > 0) {
		    List<Integer> customerIDs = response.getCustomerID();
		    customerIDs.addAll(recipientResultList);
		}
	}

	private void checkResultListSize(final String username, final int listSize) throws RecipientsSizeLimitExceededExeption {
		final OptionalInt resultListSizeOpt = readMaxResultListSize(username);
		
        if(resultListSizeOpt.isPresent() && resultListSizeOpt.getAsInt() > 0 && resultListSizeOpt.getAsInt() < listSize) {
            throw new RecipientsSizeLimitExceededExeption("List too large, refine search criterion");
        }
	}
	
	private OptionalInt readMaxResultListSize(final String username) {
		try {
			final WebserviceUserSettings settings = webserviceUserService.findSettingsForWebserviceUser(username);
			return settings.getMaxResultListSize();
		} catch(final Exception e) {
			return OptionalInt.empty();
		}
	}
}
