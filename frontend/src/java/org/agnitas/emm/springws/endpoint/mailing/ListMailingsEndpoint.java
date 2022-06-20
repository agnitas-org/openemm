/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.exception.InvalidFilterSettingsException;
import org.agnitas.emm.springws.jaxb.ListMailingsRequest;
import org.agnitas.emm.springws.jaxb.ListMailingsRequest.Filter;
import org.agnitas.emm.springws.jaxb.ListMailingsRequest.Filter.SentAfter;
import org.agnitas.emm.springws.jaxb.ListMailingsRequest.Filter.SentBefore;
import org.agnitas.emm.springws.jaxb.ListMailingsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.service.ListMailingCondition;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.service.MailingService;

@Endpoint
public class ListMailingsEndpoint extends BaseEndpoint {

	private MailingService mailingService;

	public ListMailingsEndpoint(@Qualifier("MailingService") MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListMailingsRequest")
	public @ResponsePayload ListMailingsResponse listMailings(@RequestPayload ListMailingsRequest request) {
		final ListMailingsResponse response = new ListMailingsResponse();
		
		final List<Mailing> mailings = request.getFilter() == null 
				? listAllMailings(Utils.getUserCompany())
			    : listFilteredMailings(Utils.getUserCompany(), request.getFilter());
		
		for (Mailing mailing : mailings) {
			response.getItem().add(new MailingResponseBuilder().createResponse(mailing));
		}
			
		return response;
	}
	
	private final List<Mailing> listAllMailings(final int companyId) {
		final MailingModel model = new MailingModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setTemplate(false);
		
		return this.mailingService.getMailings(model);
	}
	
	private final List<Mailing> listFilteredMailings(final int companyId, final Filter filter) {
		assert filter != null; // Ensured by caller
		
		final ListMailingFilter listFilter = toListMailingFilter(filter);
		
		return listFilter != null 
				? this.mailingService.listMailings(companyId, listFilter)
				: listAllMailings(companyId);
	}
	
	private final ListMailingFilter toListMailingFilter(final Filter filter) {
		assert filter != null; // Ensured by caller
		
		final List<ListMailingCondition> conditions = new ArrayList<>();
		
		// "sentBefore"
		if(filter.getSentBefore() != null) {
			final SentBefore sb = filter.getSentBefore();
			final ZonedDateTime timestamp = ZonedDateTime.ofInstant(sb.getTimestamp().toInstant(), ZoneOffset.UTC);	// WS interface uses UTC timezone
			
			conditions.add(ListMailingCondition.sentBefore(
					timestamp,
					sb.isInclusive() != null && sb.isInclusive()		// inclusive, if property is specified and set to "true" (defaults to: not inclusive)
					));
		}
		
		// "sentAfter"
		if(filter.getSentAfter() != null) {
			final SentAfter sa = filter.getSentAfter();
			final ZonedDateTime timestamp = ZonedDateTime.ofInstant(sa.getTimestamp().toInstant(), ZoneOffset.UTC);	// WS interface uses UTC timezone
			
			conditions.add(ListMailingCondition.sentAfter(
					timestamp,
					sa.isInclusive() == null || sa.isInclusive()		// inclusive, if property is not set or set to "true" (defaults to: inclusive)
					));
		}
		
		// Status filter
		if(filter.getMailingStatus() != null && filter.getMailingStatusList() != null) {
			throw new InvalidFilterSettingsException("Specify either 'mailingStatus' or 'mailingStatusList'");
		}
		
		if(filter.getMailingStatus() != null) {
			conditions.add(ListMailingCondition.mailingStatus(convertMailingStatus(filter.getMailingStatus())));
		}
		if(filter.getMailingStatusList() != null) {
			conditions.add(ListMailingCondition.mailingStatusList(convertMailingStatus(filter.getMailingStatusList().getMailingStatus())));
		}
		
		return conditions.isEmpty()
				? null
				: new ListMailingFilter(conditions);
	}
	
	private static final MailingStatus convertMailingStatus(final String name) throws InvalidFilterSettingsException {
		try {
			return MailingStatus.fromDbKey(name);
		} catch(final NoSuchElementException e) {
			throw new InvalidFilterSettingsException(String.format("Unknown mailing status '%s'", name));
		}
	}
	
	private static final List<MailingStatus> convertMailingStatus(final List<String> names) throws InvalidFilterSettingsException {
		final List<MailingStatus> list = new ArrayList<>();
		
		for(final String name : names) {
			list.add(convertMailingStatus(name));
		}
		
		return list;
	}
}
