/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.LinkProperty;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Struts form class for trackable user form links
 */
public class ComTrackableUserFormLinkForm extends StrutsFormBase {
	private static final long serialVersionUID = -3023759941861970201L;

	public static final String PROPERTY_NAME_PREFIX = "propertyName_";
	public static final String PROPERTY_VALUE_PREFIX = "propertyValue_";

	private int action;

	private int linkID;

	private String linkName;
	
	private String shortname;

	private int linkAction;

	private int trackable;

	private String linkUrl;

	private int formID;

	private int deepTracking;

	private int relevance;

	private String altText;

	private String linkExtension;
	
	private List<LinkProperty> commonLinkExtensions;

	private Collection<ComTrackableUserFormLink> links;

	private ComTrackableUserFormLink linkToView;

	private Map<Integer, Integer> extendLinkId = new HashMap<>();

	private Map<Integer, Boolean> extendLinkUrl = new HashMap<>();
	
	private boolean companyHasDefaultLinkExtension = false;
    private boolean linkExtensionsContainerVisible;

	/**
	 * Reset all properties to their default values.
	 * 
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
	}

	/**
	 * Validate the properties that have been set from this HTTP request, and
	 * return an <code>ActionErrors</code> object that encapsulates any
	 * validation errors that have been found. If no errors are found, return
	 * <code>null</code> or an <code>ActionErrors</code> object with no recorded
	 * error messages.
	 * 
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 * @return errors
	 */
	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (!errors.isEmpty()) {
			mapping.setInput(mapping.findForward("view").getPath());
		}
		
		return errors;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getLinkID() {
		return linkID;
	}

	public void setLinkID(int linkID) {
		this.linkID = linkID;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public int getLinkAction() {
		return linkAction;
	}

	public void setLinkAction(int linkAction) {
		this.linkAction = linkAction;
	}

	public int getTrackable() {
		return trackable;
	}

	public void setTrackable(int trackable) {
		this.trackable = trackable;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	public int getFormID() {
		return formID;
	}

	public void setFormID(int formID) {
		this.formID = formID;
	}
	
	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public Collection<ComTrackableUserFormLink> getLinks() {
		return links;
	}

	public void setLinks(Collection<ComTrackableUserFormLink> links) {
		this.links = links;
	}

	public int getDeepTracking() {
		return deepTracking;
	}

	public void setDeepTracking(int deepTracking) {
		this.deepTracking = deepTracking;
	}

	public int getRelevance() {
		return relevance;
	}

	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public String getLinkExtension() {
		return linkExtension;
	}

	public void setLinkExtension(String linkExtension) {
		this.linkExtension = linkExtension;
	}

	public void setExtendLinkId(int index, int linkId) {
		extendLinkId.put(index, linkId);
	}

	public void setExtendLinkUrl(int index, boolean extendUrl) {
		extendLinkUrl.put(index, extendUrl);
	}

	public int getExtendLinkId(int index) {
		if (extendLinkId.containsKey(index)) {
			return extendLinkId.get(index);
		} else {
			return -1; // Should never occur!
		}
	}

	public boolean getExtendLinkUrl(int index) {
		if (this.extendLinkUrl.containsKey(index)) {
			return this.extendLinkUrl.get(index);
		} else {
			return false;
		}
	}

	public Set<Integer> getLinkIndices() {
		return extendLinkId.keySet();
	}

	public void setLinkToView(ComTrackableUserFormLink linkToView) {
		this.linkToView = linkToView;
	}

	public ComTrackableUserFormLink getLinkToView() {
		return linkToView;
	}

	public boolean getCompanyHasDefaultLinkExtension() {
		return companyHasDefaultLinkExtension;
	}

	public void setCompanyHasDefaultLinkExtension(boolean companyHasDefaultLinkExtension) {
		this.companyHasDefaultLinkExtension = companyHasDefaultLinkExtension;
	}

	public List<LinkProperty> getCommonLinkExtensions() {
		return commonLinkExtensions;
	}

	public void setCommonLinkExtensions(List<LinkProperty> commonLinkExtensions) {
		this.commonLinkExtensions = commonLinkExtensions;
	}

    public boolean isLinkExtensionsContainerVisible() {
        return linkExtensionsContainerVisible;
    }

    public void setLinkExtensionsContainerVisible(boolean linkExtensionsContainerVisible) {
        this.linkExtensionsContainerVisible = linkExtensionsContainerVisible;
    }
}
