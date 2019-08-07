/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.bean.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Bean class for trackable links within a user from
 */
public class ComTrackableUserFormLinkImpl implements ComTrackableUserFormLink {
	protected int companyID;
	protected int id;
	protected int formID;
	protected int actionID;
	protected String fullUrl = null;
	protected int deepTracking = 0;
	protected int relevance;
	protected String shortname;
	protected int usage;
	protected List<LinkProperty> linkProperties = new ArrayList<>();

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public int getUsage() {
		return usage;
	}

	@Override
	public void setUsage(int usage) {
		this.usage = usage;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getActionID() {
		return actionID;
	}

	@Override
	public int getCompanyID() {
		return companyID;
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
	public void setCompanyID(@VelocityCheck int aid) {
		companyID = aid;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setFormID(int aid) {
		formID = aid;
	}

	@Override
	public void setActionID(int aid) {
		actionID = aid;
	}

	@Override
	public void setFullUrl(String url) {
		if (url == null) {
			fullUrl = "";
		} else {
			fullUrl = url;
		}
	}

	@Override
	public String getFullUrl() {
		if (fullUrl == null) {
			return "";
		} else {
			return fullUrl;
		}
	}

    @Override
	public void setProperties(List<LinkProperty> linkProperties) {
		this.linkProperties = linkProperties;
	}

    @Override
	public List<LinkProperty> getProperties() {
		return linkProperties;
	}
    
    /**
     * This method extends the full url of this link with its link extensions for display purposes.
     * User or mailing data is not used, so hash-tags will be left empty.
     * For usage of user and mailing data in correct replacements of hash-tagsuse,
     * use the methods of corresponding actions like "ComUserFormExecuteAction"
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
	public String createDirectLinkWithOptionalExtensionsWithoutUserData() throws UnsupportedEncodingException {
		String linkString = getFullUrl();
		for (LinkProperty linkProperty : getProperties()) {
			if (linkProperty.getPropertyType() == PropertyType.LinkExtension) {
				String propertyValue = linkProperty.getPropertyValue();
				if (propertyValue != null && propertyValue.contains("##")) {
					// Replace customer and form placeholders
					@SuppressWarnings("unchecked")
					String replacedPropertyValue = AgnUtils.replaceHashTags(propertyValue);
					propertyValue = replacedPropertyValue;
				}
				// Extend link properly (watch out for html-anchors etc.)
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), propertyValue == null ? "" : propertyValue, "UTF-8");
			}
		}
		return linkString;
	}
}
