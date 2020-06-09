/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

public class ProfileFieldHashTagSupport {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ProfileFieldHashTagSupport.class);
	
	/** Factory creating new recipients. */
	private final RecipientFactory recipientFactory;
	
	public ProfileFieldHashTagSupport(final RecipientFactory recipientFactory) {
		this.recipientFactory = Objects.requireNonNull(recipientFactory, "Recipient factory is null");
	}
	
	public final String evaluateExpression(final HashTagContext context, final String expression) throws HashTagException {
		return evaluateExpression(context, expression, DateUtilities.YYYY_MM_DD_HH_MM_SS);
	}
	
	public final String evaluateExpression(final HashTagContext context, final String expression, final String dateFormatPattern) throws HashTagException {
		final String[] parts = expression.split("/");
		
		final DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
		
		for(final String s : parts) {
			final String value = evaluateSubExpression(context, s, dateFormat);
			
			if (StringUtils.isNotEmpty(value)) {
				return value;
			}
		}
		
		
		return "";
	}
	
	private final String evaluateSubExpression(final HashTagContext context, final String expression, final DateFormat dateFormat) throws HashTagException {
		// Return static value, if link is marked to support static values and if value is found in map
		if(context.getCurrentTrackableLink().isStaticValue()) {
			if(context.getStaticValueMap().containsKey(expression)) {
				return context.getStaticValueMap().get(expression).toString();
			}
		}
		
		// Use current value from DB if link is marked as dynamic or if link is marked as static, but value is not in map
		final Recipient cust = recipientFactory.newRecipient(context.getCompanyID());

		cust.setDateFormatForProfileFieldConversion(dateFormat);
		cust.setCustomerID(context.getCustomerId());
		cust.getCustomerDataFromDb();

		return doHandleAccess(expression, cust, context);
	}
	
	protected String doHandleAccess(final String expression, final Recipient recipient, final HashTagContext context) throws HashTagException {
		return handleProfileFieldAccess(expression, recipient);
	}
	
	/**
	 * Simply returns the content of the given profile field of current recipient.
	 * 
	 * @param expression name of profile field
	 * @param cust current recipient
	 * 
	 * @return content of profile field
	 * 
	 * @throws HashTagException on errors during processing
	 */
	protected final String handleProfileFieldAccess(final String expression, final Recipient cust) throws HashTagException {
		return cust.getCustParametersNotNull(expression);
	}

	/**
	 * Splits given String of form "key0=value0,key1=value1,..." into its key value pairs.
	 * If <code>string</code> is <code>null</code> an empty map is returned.
	 * 
	 * @param string String of comma-separated key-value pairs
	 * 
	 * @return parsed key value pairs
	 */
	public static final Map<String, Integer> keyValueParser(final String string) {
		final Map<String, Integer> map = new HashMap<>();
		
		if(string != null) {
			final String[] pairs = string.split(",");
			
			for(String pair : pairs) {
				final String[] keyValue = pair.split("=");
				
				if(keyValue.length == 2) {
					map.put(keyValue[0].trim(), Integer.parseInt(keyValue[1].trim()));
				}
			}
		}
		
		return map;
	}

}
