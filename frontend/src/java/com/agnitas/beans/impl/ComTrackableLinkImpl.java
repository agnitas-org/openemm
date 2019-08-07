/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;

public class ComTrackableLinkImpl implements ComTrackableLink {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComTrackableLinkImpl.class);

	protected int companyID;
		
	protected int id;
	protected int mailingID;
	protected int actionID;
	protected String fullUrl = null;

	/** Holds value of property shortname. */
	protected String shortname;

	/** Holds value of property usage. */
	protected int usage;

	protected boolean adminLink;
	protected boolean deleted;
	protected boolean extendByMailingExtensions;
	
	protected List<LinkProperty> linkProperties = new ArrayList<>();

	/** Original URL of link. Only set, if link URL is modified after sending mailing. */
	private String originalUrl;
	
	private boolean staticValue;

	@Override
	public final boolean isStaticValue() {
		return staticValue;
	}

	@Override
	public final void setStaticValue(final boolean flag) {
		this.staticValue = flag;
	}

	@Override
	public void setCompanyID( @VelocityCheck int aid) {
		companyID = aid;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setMailingID(int aid) {
		mailingID = aid;
	}

	@Override
	public void setActionID(int aid) {
		actionID = aid;
	}

	@Override
	public void setFullUrl(String url) {
		if (url == null) {
			url = "";
		}

		fullUrl = url;
	}

	@Override
	public String getFullUrl() {
		if (fullUrl == null) {
			return "";
		}

		return fullUrl;
	}

	/**
	 * Getter for property shortname.
	 * 
	 * @return Value of property shortname.
	 */
	@Override
	public String getShortname() {
		return this.shortname;
	}

	/**
	 * Setter for property shortname.
	 * 
	 * @param shortname
	 *            New value of property shortname.
	 */
	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	/**
	 * Getter for property usage.
	 * 
	 * @return Value of property usage.
	 */
	@Override
	public int getUsage() {
		return this.usage;
	}

	/**
	 * Setter for property usage.
	 * 
	 * @param usage
	 *            New value of property usage.
	 */
	@Override
	public void setUsage(int usage) {
		this.usage = usage;
	}

	/**
	 * Getter for property urlID.
	 * 
	 * @return Value of property urlID.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * Getter for property actionID.
	 * 
	 * @return Value of property actionID.
	 */
	@Override
	public int getActionID() {
		return actionID;
	}

	/**
	 * Getter for property companyID.
	 * 
	 * @return Value of property companyID.
	 */
	@Override
	public int getCompanyID() {
		return companyID;
	}

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	@Override
	public int getMailingID() {
		return this.mailingID;
	}

	/**
	 * Holds value of property relevance.
	 */
	protected int deepTracking = 0;

	/**
	 * Getter for property relevance.
	 * 
	 * @return Value of property relevance.
	 */
	@Override
	public int getDeepTracking() {
		return deepTracking;
	}

	/**
	 * Setter for property deepTracking.
	 * 
	 * @param deepTracking
	 *            New value of property deepTracking.
	 */
	@Override
	public void setDeepTracking(int deepTracking) {
		this.deepTracking = deepTracking;
	}

	/**
	 * Holds value of property relevance.
	 */
	protected int relevance;

	/**
	 * Getter for property relevance.
	 * 
	 * @return Value of property relevance.
	 */
	@Override
	public int getRelevance() {
		return this.relevance;
	}

	/**
	 * Setter for property relevance.
	 * 
	 * @param relevance
	 *            New value of property relevance.
	 */
	@Override
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}

	public String altText;

	@Override
	public String getAltText() {
		return altText;
	}

	@Override
	public void setAltText(String altText) {
		this.altText = altText;
	}

	@Override
	public void setAdminLink(boolean adminLink) {
		this.adminLink = adminLink;
	}

	@Override
	public boolean isAdminLink() {
		return adminLink;
	}

	/**
	 * Checks, if trackable link is not longer used by mailing.
	 * 
	 * return true, if link is not longer used, otherwise true
	 */
	@Override
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Marks link as not longer used by mailing.
	 * 
	 * @param deleted
	 *            true if not longer used
	 */
	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public void setProperties(List<LinkProperty> linkProperties) {
		this.linkProperties = linkProperties;
	}

	@Override
	public List<LinkProperty> getProperties() {
		return linkProperties;
	}

	@Override
    public boolean isExtendByMailingExtensions() {
		return extendByMailingExtensions;
	}

	@Override
	public void setExtendByMailingExtensions(boolean extendByMailingExtensions) {
		this.extendByMailingExtensions = extendByMailingExtensions;
	}

	/**
     * This method extends the full url of this link with its link extensions for display purposes.
     * User or mailing data is not used, so hash-tags will be left empty.
     * For usage of user and mailing data in correct replacements of hash-tagsuse,
     * use the methods of corresponding actions like "ComMailingContentAction"
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
    
    @Override
    public void setOriginalUrl(String url) {
    	this.originalUrl = url;
    }
    
    @Override
    public String getOriginalUrl() {
    	return this.originalUrl;
    }
    
    @Override
    public boolean isUrlModified() {
    	return StringUtils.isNotEmpty(getOriginalUrl());
    }
    
    @Override
    public int getLinkExtensionCount() {
    	if(linkProperties == null || linkProperties.isEmpty()) {
    		return 0;
		}
		
		return (int) linkProperties.stream()
				.map(LinkProperty::getPropertyType)
				.filter(type -> type == PropertyType.LinkExtension)
				.count();
	}
	
	@Override
	public String getFullUrlWithExtensions() {
    	try {
			String directLink = createDirectLinkWithOptionalExtensionsWithoutUserData();
			return StringEscapeUtils.escapeHtml(directLink);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Error creation directory link with optional extension without user data, cause: " + e.getMessage());
			return "";
		}
	}
}
