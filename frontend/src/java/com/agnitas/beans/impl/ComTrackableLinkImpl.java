/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import org.agnitas.beans.BaseTrackableLinkImpl;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.ComTrackableLink;

public class ComTrackableLinkImpl extends BaseTrackableLinkImpl implements ComTrackableLink {

	protected int mailingID;
	protected boolean adminLink;
	protected boolean deleted;
	protected boolean extendByMailingExtensions;
	/** Original URL of link. Only set, if link URL is modified after sending mailing. */
	private String originalUrl;
	private boolean staticValue;
	private String altText;
	private boolean measureSeparately;
	private boolean createSubstituteForAgnDynMulti;
	
	public ComTrackableLinkImpl() {
		// Empty
	}
	
	public ComTrackableLinkImpl(final ComTrackableLink original) {
		super(original);
		
		this.setMailingID(original.getMailingID());
		this.setAdminLink(original.isAdminLink());
		this.setDeleted(original.isDeleted());
		this.setExtendByMailingExtensions(original.isExtendByMailingExtensions());
		this.setOriginalUrl(original.getOriginalUrl());
		this.setStaticValue(original.isStaticValue());
		this.setAltText(original.getAltText());
		this.setMeasureSeparately(original.isMeasureSeparately());
		this.setCreateSubstituteLinkForAgnDynMulti(original.isCreateSubstituteLinkForAgnDynMulti());
	}

	@Override
	public final boolean isStaticValue() {
		return staticValue;
	}

	@Override
	public void setMeasureSeparately(boolean measureSeparately) {
		this.measureSeparately = measureSeparately;
	}

	@Override
    public boolean isMeasureSeparately() {
        return this.measureSeparately;
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
	public int getMailingID() {
		return this.mailingID;
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
    
    @Override
    public final boolean isCreateSubstituteLinkForAgnDynMulti() {
    	return this.createSubstituteForAgnDynMulti;
    }
    
    @Override
    public final void setCreateSubstituteLinkForAgnDynMulti(final boolean createSubstituteForAgnDynMulti) {
    	this.createSubstituteForAgnDynMulti = createSubstituteForAgnDynMulti; 
    }
}
