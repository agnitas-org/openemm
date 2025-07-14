/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.beans.LinkProperty;
import com.agnitas.util.AgnUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseTrackableLinkImpl implements BaseTrackableLink {

	private static final Logger logger = LogManager.getLogger(BaseTrackableLinkImpl.class);

	protected int id;
	protected String shortname;
	protected int companyID;
	protected int actionID;
	protected String fullUrl = null;
	protected int usage;
	protected int deepTracking;
	protected List<LinkProperty> linkProperties = new ArrayList<>();

	public BaseTrackableLinkImpl() {
		// Empty
	}
	
	public BaseTrackableLinkImpl(final BaseTrackableLink original) {
		this.setActionID(original.getActionID());
		this.setCompanyID(original.getCompanyID());
		this.setDeepTracking(original.getDeepTracking());
		this.setFullUrl(original.getFullUrl());
		this.setId(0);											// Do not use same link ID for copy
		this.setProperties(original.getProperties());
		this.setShortname(original.getShortname());
		this.setUsage(original.getUsage());
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String getShortname() {
		return shortname;
	}
	
	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	@Override
	public int getCompanyID() {
		return companyID;
	}
	
	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}
	
	@Override
	public void setActionID(int actionID) {
		this.actionID = actionID;
	}
	
	@Override
	public int getActionID() {
		return actionID;
	}
	
	@Override
	public void setFullUrl(String fullUrl) {
		this.fullUrl = StringUtils.defaultString(fullUrl);
	}
	
	@Override
	public String getFullUrl() {
		return StringUtils.defaultString(fullUrl);
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
	public int getDeepTracking() {
		return deepTracking;
	}

	@Override
	public void setDeepTracking(int deepTracking) {
		this.deepTracking = deepTracking;
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
     * use the methods of corresponding actions like "MailingContentController"
     *
     * Caution:
     * This is used by JSP-Files
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
	public String createDirectLinkWithOptionalExtensionsWithoutUserData() throws UnsupportedEncodingException {
		String linkString = getFullUrl();
		for (LinkProperty linkProperty : getProperties()) {
			if (linkProperty.getPropertyType() == LinkProperty.PropertyType.LinkExtension) {
				String propertyValue = linkProperty.getPropertyValue();
				if (propertyValue != null && propertyValue.contains("##")) {
					// Replace customer and form placeholders
                    propertyValue = AgnUtils.replaceHashTags(propertyValue);
				}
				// Extend link properly (watch out for html-anchors etc.)
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), propertyValue == null ? "" : propertyValue, "UTF-8");
			}
		}
		return linkString;
	}
	
	@Override
    public int getLinkExtensionCount() {
    	if(linkProperties == null || linkProperties.isEmpty()) {
    		return 0;
		}
		
		return (int) linkProperties.stream()
				.map(LinkProperty::getPropertyType)
				.filter(type -> type == LinkProperty.PropertyType.LinkExtension)
				.count();
	}
	
	@Override
	public String getFullUrlWithExtensions() {
    	try {
			String directLink = createDirectLinkWithOptionalExtensionsWithoutUserData();
			return StringEscapeUtils.escapeHtml4(directLink);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Error creation directory link with optional extension without user data, cause: " + e.getMessage());
			return "";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BaseTrackableLinkImpl that = (BaseTrackableLinkImpl) o;

		if (id != that.id) {
			return false;
		}

		return fullUrl.equals(that.fullUrl);
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + fullUrl.hashCode();
		return result;
	}
}
