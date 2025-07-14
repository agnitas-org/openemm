/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.mail.internet.InternetAddress;

import com.agnitas.backend.Mailgun;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.util.AgnUtils;

/**
 * Builder class for options map that is used when invoking Mailgun.
 *
 * @see Mailgun#execute(Map)
 */
public final class MailgunOptions {

	/** Map holding the primitive properties. Complex properties (like Map-values) are stored in separate fields. */
	private final Map<String, Object> options;
	
	/** List holding allowed user status. */
	private final List<Integer> userStatusList;
	
	/** Map with values for &quot;overwrite&quot; property. */
	private final Map<String, String> overwriteMap;
	
	/** Map with values for &quot;static&quot; property. */
	private final Map<String, Object> staticMap;
	
	/**
	 * Creates a new instance with no property set.
	 */
	public MailgunOptions() {
		this.options = new HashMap<>();
		this.userStatusList = new ArrayList<>();
		this.overwriteMap = new HashMap<>();
		this.staticMap = new HashMap<>();
	}

	public static MailgunOptions empty() {
		return new MailgunOptions();
	}
	
	public static MailgunOptions from(Map<String, String> overwrite) {
		final MailgunOptions mo = MailgunOptions.empty();
		
		mo.options.putAll(overwrite);

		return mo;
	}

	/**
	 * Sets the &quot;force-sending&quot; property to given value.
	 * 
	 * @param forceSending value for property
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withForceSending(final boolean forceSending) {
		this.options.put("force-sending", forceSending);
		
		return this;
	}
	
	/**
	 * Sets the &quot;bcc&quot; property to given value. <i>bccEmails</i> is a list of
	 * email addresses separated by commas, semicolons or spaces.
	 * 
	 * @param bccEmails list of email addresses
	 * 
	 * @return this instance
	 * 
	 * @throws Exception on errors splitting list of email addresses
	 */
	public MailgunOptions withBccEmails(String bccEmails) {
		if(bccEmails != null) {
			final List<String> emails = Stream.of(AgnUtils.getEmailAddressesFromList(bccEmails))
					.map(InternetAddress::getAddress)
					.collect(Collectors.toList());
			
			this.options.put("bcc", emails);
		}
		
		return this;
	}
	
	/**
	 * Sets a differing recipient email address.
	 * 
	 * @param emailAddress differeing email address
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withDifferentRecipientEmailAddress(final String emailAddress) {
		if(emailAddress != null) {
			this.options.put("provider-email", emailAddress);
		}
		
		return this;
	}
	
	/**
	 * Sets the list of allowed user status.
	 * 
	 * @param statusList list of user status
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withAllowedUserStatus(final UserStatus... userStatuses) {
		if (userStatuses != null) {
			withAllowedUserStatus(Arrays.asList(userStatuses));
		} 
		
		return this;
	}
	
	/**
	 * Sets the list of allowed user status.
	 * 
	 * @param statusList list of user status
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withAllowedUserStatus(final List<UserStatus> userStatuses) {
		if (userStatuses != null) {
			if (userStatuses.contains(null)) {
				throw new IllegalArgumentException("User status list contains null value");
			}
			
			for (final UserStatus status : userStatuses) {
				if (!this.userStatusList.contains(status.getStatusCode())) {
					this.userStatusList.add(status.getStatusCode());
				}
			}
		}
			
		return this;
	}
	
	/**
	 * Overwrites the value of an existing profile field.
	 * 
	 * @param name name of profile field
	 * @param value value of profile field
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withProfileFieldValue(final String name, final String value) {
		this.overwriteMap.put(name, value);
		
		return this;
	}
	
	/**
	 * Overwrites the values of existing profile fields.
	 * 
	 * @param values map of profile fields to overwrite
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withProfileFieldValues(final Map<String, String> values) {
		if(values != null) {
			this.overwriteMap.putAll(values);
		}
		
		return this;
	}
	
	/**
	 * Defines a static value.
	 * 
	 * @param name name of value
	 * @param value value
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withStaticValue(final String name, final Object value) {
		this.staticMap.put(name, value);
		
		return this;
	}
	
	/**
	 * Defines static values.
	 * 
	 * @param staticValues map of static values
	 * 
	 * @return this instance
	 */
	public final MailgunOptions withStaticValues(final Map<String, Object> staticValues) {
		this.staticMap.putAll(staticValues);
		
		return this;
	}

	/**
	 * Fills options map with copy of complex properties. This is done to prevent someone from
	 * modifying the internal data from outside.
	 * 
	 * @param targetMap map to fill
	 */
	private final void fillCopyOfCollections(final Map<String, Object> targetMap) {
		if (this.userStatusList != null && !this.userStatusList.isEmpty()) {
			targetMap.put("user-status",  new ArrayList<>(this.userStatusList));
		}
		
		if (this.overwriteMap != null && !this.overwriteMap.isEmpty()) {
			targetMap.put("overwrite", new HashMap<>(this.overwriteMap));
		}
		
		if (this.staticMap != null && !this.staticMap.isEmpty()) {
			targetMap.put("static", new HashMap<>(this.staticMap));
		}
	}
	
	/**
	 * Returns the options as a {@link Map}.
	 * 
	 * @return options as {@link Map}
	 */
	public final Map<String, Object> asMap() {
		final Map<String, Object> map = new HashMap<>(this.options);
		
		fillCopyOfCollections(map);
		
		return map;
	}
	
	/**
	 * Returns the options as a {@link Hashtable}.
	 * 
	 * @return options as {@link Hashtable}
	 */
	public final Hashtable<String, Object> asHashtable() {
		final Hashtable<String, Object> table = new Hashtable<>(this.options);
		
		fillCopyOfCollections(table);
		
		return table;
	}
}
