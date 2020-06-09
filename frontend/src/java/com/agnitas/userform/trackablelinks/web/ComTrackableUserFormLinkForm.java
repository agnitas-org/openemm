/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.agnitas.web.BaseTrackableLinkForm;

import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Struts form class for trackable user form links
 */
public class ComTrackableUserFormLinkForm extends BaseTrackableLinkForm {
	private static final long serialVersionUID = -3023759941861970201L;

	private int formID;

	private String linkExtension;
	
	private Map<Integer, Integer> linkItemUsage = new HashMap<>();

	private Map<Integer, Integer> extendLinkId = new HashMap<>();

	private Map<Integer, Boolean> extendLinkUrl = new HashMap<>();
	
	private ComTrackableUserFormLink linkToView;

    private boolean linkExtensionsContainerVisible;

	public int getFormID() {
		return formID;
	}

	public void setFormID(int formID) {
		this.formID = formID;
	}
	
	public int getLinkItemUsage(int id) {
		return this.linkItemUsage.getOrDefault(id, 0);
	}

	public void setLinkItemUsage(int id, int value) {
		this.linkItemUsage.put(id, value);
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
		return extendLinkId.getOrDefault(index, -1);
	}

	public boolean getExtendLinkUrl(int index) {
		return this.extendLinkUrl.getOrDefault(index, false);
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

    public boolean isLinkExtensionsContainerVisible() {
        return linkExtensionsContainerVisible;
    }

    public void setLinkExtensionsContainerVisible(boolean linkExtensionsContainerVisible) {
        this.linkExtensionsContainerVisible = linkExtensionsContainerVisible;
    }
}
