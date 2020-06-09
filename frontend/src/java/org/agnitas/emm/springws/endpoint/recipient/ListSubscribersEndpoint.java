/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.recipient;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import javax.annotation.Resource;

import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.RecipientsModel;
import org.agnitas.emm.core.recipient.service.impl.RecipientWrongRequestException;
import org.agnitas.emm.core.recipient.service.impl.RecipientsSizeLimitExceededExeption;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.Criteria;
import org.agnitas.emm.springws.jaxb.Equals;
import org.agnitas.emm.springws.jaxb.ListSubscribersRequest;
import org.agnitas.emm.springws.jaxb.ListSubscribersResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

public class ListSubscribersEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private RecipientService recipientService;
	@Resource
	private ObjectFactory objectFactory;
	@Resource
	private WebserviceUserService webserviceUserService;
	
	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		ListSubscribersRequest request = (ListSubscribersRequest) arg0;
		ListSubscribersResponse response = objectFactory.createListSubscribersResponse();

		RecipientsModel model = parseModel(request);
		
		final String username = Utils.getUserName();
		final int size = recipientService.getSubscribersSize(model);
        checkResultListSize(username, size);
	        
		List<Integer> recipientResultList = recipientService.getSubscribers(model);
		populateResponse(request, response, recipientResultList, objectFactory);
		return response;
	}
	
	static RecipientsModel parseModel(ListSubscribersRequest request) {
	    Criteria criteria = request.getCriteria();
	    if (criteria == null) {
            throw new RecipientWrongRequestException("Criteria are empty.");
        }
	    
        RecipientsModel model = new RecipientsModel();
        model.setCompanyId(Utils.getUserCompany());
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

	static void populateResponse(ListSubscribersRequest request, ListSubscribersResponse response, List<Integer> recipientResultList, ObjectFactory objectFactory) {
		if (recipientResultList != null && recipientResultList.size() > 0) {
		    List<Integer> customerIDs = response.getCustomerID();
		    customerIDs.addAll(recipientResultList);
		}
	}

	private final void checkResultListSize(final String username, final int listSize) throws RecipientsSizeLimitExceededExeption {
		final OptionalInt resultListSizeOpt = readMaxResultListSize(username);
		
        if(resultListSizeOpt.isPresent() && resultListSizeOpt.getAsInt() > 0 && resultListSizeOpt.getAsInt() < listSize) {
            throw new RecipientsSizeLimitExceededExeption("List too large, refine search criterion");
        }
	}
	
	private final OptionalInt readMaxResultListSize(final String username) {
		try {
			final WebserviceUserSettings settings = webserviceUserService.findSettingsForWebserviceUser(username);
			return settings.getMaxResultListSize();
		} catch(final Exception e) {
			return OptionalInt.empty();
		}
	}
}
