/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.bean.impl;

import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import org.agnitas.beans.BaseTrackableLinkImpl;

/**
 * Bean class for trackable links within a user from
 */
public class ComTrackableUserFormLinkImpl extends BaseTrackableLinkImpl implements ComTrackableUserFormLink {
	
	protected int formID;
	protected int deepTracking = 0;
	protected int relevance;
	protected int usage;

	@Override
	public int getUsage() {
		return usage;
	}

	@Override
	public void setUsage(int usage) {
		this.usage = usage;
	}

	@Override
	public int getFormID() {
		return formID;
	}

	@Override
	public final boolean equals(final Object obj) {
		if(obj == null || !(obj instanceof ComTrackableUserFormLink)) {
			return false;
		} else {
			final ComTrackableUserFormLink link = (ComTrackableUserFormLink) obj;

			/*
			 * TODO: Replace comparing hash codes by comparing IDs?
			 * 
			 * By definition different String can be reduced to the same hash code.
			 * Isn't it better here to compare the IDs of the links?
			 */
			return link.hashCode() == this.hashCode();		
		}
	}

	@Override
	public int hashCode() {
		return getFullUrl().hashCode();
	}

	@Override
	public int getDeepTracking() {
		return deepTracking;
	}

	@Override
	public String getDeepTrackingSession() {
		// not implemented
		return null;
	}

	@Override
	public String getDeepTrackingUID() {
		// not implemented
		return null;
	}

	@Override
	public int getRelevance() {
		return relevance;
	}

	@Override
	public void setDeepTracking(int deepTracking) {
		this.deepTracking = deepTracking;
	}

	@Override
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}

	@Override
	public void setFormID(int aid) {
		formID = aid;
	}
}
