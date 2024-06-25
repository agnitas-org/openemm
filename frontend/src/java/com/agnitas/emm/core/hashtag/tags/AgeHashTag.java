/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.hashtag.AbstractColonHashTagWithParameters;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

public final class AgeHashTag extends AbstractColonHashTagWithParameters {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(AgeHashTag.class);
	
	private static final String COLUMN_PARAMETER = "column";
	
	private final RecipientFactory recipientFactory;
	private final RecipientService recipientService;
	
	public AgeHashTag(final RecipientFactory recipientFactory, final RecipientService recipientService) {
		this.recipientFactory = Objects.requireNonNull(recipientFactory, "recipient factory");
		this.recipientService = Objects.requireNonNull(recipientService, "recipient service");
	}

	@Override
	public final String handleWithParametersInternal(final HashTagContext context, final String tagName, final Map<String, String> parameters) throws HashTagException {
		if(!isTagName(tagName) || !parameters.containsKey(COLUMN_PARAMETER)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info("Invalid tag name or missing 'column' parameter");
			}
			
			return "";
		}
			
		final Period age = age(context, parameters);
		
		return age != null ? Long.toString(age.getYears()) : "";
	}
	
	private final Period age(final HashTagContext context, final Map<String, String> parameters) throws HashTagException {
		final String column = parameters.get(COLUMN_PARAMETER);
		final DateFormat dateFormat = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS); // SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
		
		final Recipient recipient = this.recipientFactory.newRecipient(context.getCompanyID());
		recipient.setCustomerID(context.getCustomerId());
		recipient.setCustParameters(recipientService.getCustomerDataFromDb(context.getCompanyID(), context.getCustomerId(), dateFormat));

		final String dateString = recipient.getCustParametersNotNull(column);
		
		if(dateString == null) {
			return null;
		}
		
		try {
			final Date date = dateFormat.parse(dateString);
			final ZonedDateTime zonedDate = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
			final ZonedDateTime now = ZonedDateTime.now();
			
			return Period.between(
					zonedDate.withZoneSameInstant(ZoneOffset.UTC).toLocalDate(),
					now.withZoneSameInstant(ZoneOffset.UTC).toLocalDate());
		} catch(final ParseException e) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Error parsing date string: %s", dateString), e);
			}
			
			return null;
		}
	}

	@Override
	public final boolean isSupportedTag(final String tagName, final boolean hasColon) {
		return isTagName(tagName);
	}
	
	private static final boolean isTagName(final String tagName) {
		return "age".equalsIgnoreCase(tagName);
	}

}
