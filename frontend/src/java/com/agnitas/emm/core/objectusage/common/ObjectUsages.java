/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.agnitas.emm.core.objectusage.web.ObjectUsagesToMessages;
import com.agnitas.messages.Message;

/**
 * Container for all object usages found.
 */
public final class ObjectUsages implements Iterable<ObjectUsage> {

	/** List of object usages. */
	private final Collection<ObjectUsage> usages;
	
	/**
	 * Creates a new instance.
	 * If given collection of usages is <code>null</code> a collection
	 * with no object usage data is created.
	 * 
	 * @param usages object usages
	 */
	public ObjectUsages(final Collection<ObjectUsage> usages) {
		this.usages = usages != null ? usages : Collections.emptyList();
	}
	
	/**
	 * Returns an instance without usage data.
	 * 
	 * @return instance without usage data
	 */
	public static ObjectUsages empty() {
		return new ObjectUsages(Collections.emptyList());
	}
	
	/**
	 * Returns all object usages for given user type.
	 * 
	 * @param type user type
	 * 
	 * @return list of object usages for given user types
	 */
	public List<ObjectUsage> getUsagesByUserType(final ObjectUserType type) {
		return this.usages.stream()
				.filter(u -> u.getObjectUserType() == type)
				.toList();
	}

	/**
	 * Returns <code>true</code> if this collection contains no usage data.
	 * 
	 * @return <code>true</code> if collection contains no usage data
	 */
	public boolean isEmpty() {
		return usages.isEmpty();
	}
	
	/**
	 * Returns the number of usage data contained in this collection.
	 * 
	 * @return number of usage data contained in this collection
	 */
	public int size() {
		return this.usages.size();
	}

	@Override
	public Iterator<ObjectUsage> iterator() {
		return usages.iterator();
	}

	public Message toMessage(String msgKey, Locale locale) {
        return ObjectUsagesToMessages.objectUsagesToMessage(msgKey, this, locale);
    }

	public Message toMessage(String msgKey, String errorMsgWithMoreKey, Locale locale) {
		return ObjectUsagesToMessages.objectUsagesToMessage(msgKey, errorMsgWithMoreKey, this, locale);
	}
}
