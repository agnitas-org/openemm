/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import java.util.Collections;
import java.util.Map;

import com.agnitas.beans.ComTrackableLink;

/**
 * Context providing common data used for processing hash tags.
 */
public final class HashTagContext {

	/** Customer ID, for that the tag is processed. */
	private final int customerID;
	
	/** The current trackable link. */
	private final ComTrackableLink link;

	// TODO: Not sure, what this is, but this data is provided by the method head and used by some tags.
	/** Original UID. */
	private final String originalUID;
	
	/** Selector for a record in reference table with 1:n relations. */
	private final String referenceTableRecordSelector;
	
	/** Map containing static values used by hashtag substitution. */
	private final Map<String, Object> staticValueMap;
	
	/**
	 * Creates a new hash tag context.
	 * 
	 * @param link the current trackable link
	 * @param customerID customer ID, for that the tag is processed
	 * @param originalUID original UID (?)
	 * @param referenceTableRecordSelector selector for a record in reference table with 1:n relations
	 * @param staticValueMapOrNull map containing static values used by hashtag substitution
	 */
	public HashTagContext(final ComTrackableLink link, final int customerID, final String originalUID, final String referenceTableRecordSelector, final Map<String, Object> staticValueMapOrNull) {
		this.customerID = customerID;
		this.originalUID = originalUID;
		this.referenceTableRecordSelector = referenceTableRecordSelector;
		this.link = link;
		this.staticValueMap = staticValueMapOrNull != null ? staticValueMapOrNull : Collections.emptyMap();
	}
	
	/**
	 * Returns the company ID of the trackable link.
	 * 
	 * @return company ID of trackable link
	 */
	public final int getCompanyID() {
		return this.link.getCompanyID();
	}
	
	/**
	 * Returns the current trackable link.
	 * 
	 * @return current trackable link
	 */
	public final ComTrackableLink getCurrentTrackableLink() {
		return this.link;
	}

	/**
	 * Returns the customer ID, for that the hash tag is processed.
	 * 
	 * @return customer ID
	 */
	public final int getCustomerId() {
		return this.customerID;
	}
	
	/**
	 * Returns the original UID.
	 * 
	 * @return original UID
	 */
	public final String getOriginalUID() {
		return this.originalUID;
	}

	/**
	 * Returns the selector for a record in reference table with 1:n relations.
	 * 
	 * @return selector for a record in reference table with 1:n relations
	 */
	public final String getReferenceTableRecordSelector() {
		return this.referenceTableRecordSelector;
	}
	
	/**
	 * Returns the map containing static values used by hashtag substitution.
	 * 
	 * @return map containing static values used by hashtag substitution
	 */
	public final Map<String, Object> getStaticValueMap() {
		return this.staticValueMap;
	}
}
