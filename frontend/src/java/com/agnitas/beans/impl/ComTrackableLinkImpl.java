/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import com.agnitas.beans.ComTrackableLink;
import org.agnitas.beans.BaseTrackableLinkImpl;
import org.apache.commons.lang3.StringUtils;

public class ComTrackableLinkImpl extends BaseTrackableLinkImpl implements ComTrackableLink {

	protected int mailingID;
	/** Holds value of property usage. */
	protected int usage;
	protected boolean adminLink;
	protected boolean deleted;
	protected boolean extendByMailingExtensions;
	/**
	 * Holds value of property deepTracking.
	 */
	protected int deepTracking = 0;
	/** Original URL of link. Only set, if link URL is modified after sending mailing. */
	private String originalUrl;
	private boolean staticValue;
	/**
	 * Holds value of property relevance.
	 */
	protected int relevance;
	public String altText;

	@Override
	public final boolean isStaticValue() {
		return staticValue;
	}

	@Override
	public final void setStaticValue(final boolean flag) {
		this.staticValue = flag;
	}

	@Override
	public void setMailingID(int aid) {
		mailingID = aid;
	}

	@Override
	public int getUsage() {
		return this.usage;
	}

	@Override
	public void setUsage(int usage) {
		this.usage = usage;
	}

	@Override
	public int getMailingID() {
		return this.mailingID;
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
	@Deprecated
	public int getRelevance() {
		return 0;
	}

	@Override
	@Deprecated
	public void setRelevance(int relevance) {
		this.relevance = 0;
	}

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

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
    public boolean isExtendByMailingExtensions() {
		return extendByMailingExtensions;
	}

	@Override
	public void setExtendByMailingExtensions(boolean extendByMailingExtensions) {
		this.extendByMailingExtensions = extendByMailingExtensions;
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
}
