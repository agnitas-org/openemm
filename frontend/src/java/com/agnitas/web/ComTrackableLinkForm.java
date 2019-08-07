/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.web.TrackableLinkForm;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;

public class ComTrackableLinkForm extends TrackableLinkForm {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 3450884602706659095L;
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTrackableLinkForm.class);

	public static final String PROPERTY_NAME_PREFIX = "propertyName_";
	public static final String PROPERTY_VALUE_PREFIX = "propertyValue_";

    private String altText;

	private String linkExtension;

	private List<LinkProperty> commonLinkExtensions;

	private int globalRelevance;

	private boolean trackableContainerVisible;
	private boolean linkContainerVisible;
	private boolean linkExtensionsContainerVisible;
	private boolean actionContainerVisible;
	private boolean webshopContainerVisible;

	private Set<Integer> adminLinkIds = new HashSet<>();
	private boolean adminLink;

	private boolean everyPositionLink;

	private ComTrackableLink linkToView;
	private boolean companyHasDefaultLinkExtension = false;

    private boolean keepExtensionsUnchanged;
    
    /** Flag, if IntelliAd link tracking is enabled. */
    private boolean intelliAdEnabled;
    
    /** ID-String from IntelliAd. */
    private String intelliAdIdString;
    
    private boolean intelliAdShown;

    private Map<Integer, Integer> linkItemRelevance = new HashMap<>();

    private Map<Integer, Integer> linkItemDeepTracking = new HashMap<>();

    private Map<Integer, String> linkItemName = new HashMap<>();

	private int workflowId;

	private boolean isMailingGrid;

    private int scrollToLinkId;

	private boolean isMailingUndoAvailable;
	
	private boolean staticLink;

    public boolean isKeepExtensionsUnchanged() {
        return keepExtensionsUnchanged;
    }

    public void setKeepExtensionsUnchanged(boolean keepExtensionsUnchanged) {
        this.keepExtensionsUnchanged = keepExtensionsUnchanged;
    }

    public String getAltText() {
		return this.altText;
	}
	
	public void setAltText(String altText) {
		this.altText = altText;
	}

	/**
	 * this variable is used for the following issue: if you have two or more
	 * SAME Links in a Mailing, you can not them in the mailing statistic,
	 * because they get all the same ID. If the variable is set to false, we
	 * will make them unique and then you get a more detailed statistic. Default
	 * is true (no difference between the links)
	 */
	private boolean countLinkIsUnique = true;

	/**
	 * if "true", no difference is made between same links. if "false", same
	 * links are made different (by us).
	 * 
	 * @return
	 */
	public boolean isCountLinksUnique() {
		return countLinkIsUnique;
	}

	/**
	 * if set to "true", no difference is made between same links. if set to
	 * "false", same links are made different (by us).
	 * 
	 * @param countLinksUnique
	 */
	public void setCountLinksUnique(boolean countLinksUnique) {
		this.countLinkIsUnique = countLinksUnique;
	}

	public String getLinkExtension() {
		return linkExtension;
	}

	public void setLinkExtension(String linkExtension) {
		this.linkExtension = linkExtension;
	}

	public int getGlobalRelevance() {
		return globalRelevance;
	}

	public void setGlobalRelevance(int globalRelevance) {
		this.globalRelevance = globalRelevance;
	}

	@Override
	public boolean isTrackableContainerVisible() {
		return trackableContainerVisible;
	}

	@Override
	public void setTrackableContainerVisible(boolean trackableContainerVisible) {
		this.trackableContainerVisible = trackableContainerVisible;
	}

	public boolean isLinkContainerVisible() {
		return linkContainerVisible;
	}

	public void setLinkContainerVisible(boolean linkContainerVisible) {
		this.linkContainerVisible = linkContainerVisible;
	}

    public boolean isLinkExtensionsContainerVisible() {
        return linkExtensionsContainerVisible;
    }

    public void setLinkExtensionsContainerVisible(boolean linkExtensionsContainerVisible) {
        this.linkExtensionsContainerVisible = linkExtensionsContainerVisible;
    }

    @Override
	public boolean isActionContainerVisible() {
		return actionContainerVisible;
	}

	@Override
	public void setActionContainerVisible(boolean actionContainerVisible) {
		this.actionContainerVisible = actionContainerVisible;
	}

	public boolean isWebshopContainerVisible() {
		return webshopContainerVisible;
	}

	public void setWebshopContainerVisible(boolean webshopContainerVisible) {
		this.webshopContainerVisible = webshopContainerVisible;
	}

	public void setAdminLink(int id, boolean value) {
		if (value) {
			adminLinkIds.add(id);
		} else if (adminLinkIds.contains(id)) {
			adminLinkIds.remove(id);
		}
	}

	public boolean getAdminLink(int id) {
		return this.adminLinkIds.contains(id);
	}

	public Set<Integer> getAdminLinks() {
		return this.adminLinkIds;
	}

	public void clearAdminLinks() {
		this.adminLinkIds.clear();
	}

	public boolean isAdministrativeLink() {
		return adminLink;
	}

	public void setAdministrativeLink(boolean adminLink) {
		this.adminLink = adminLink;
	}

	public boolean isEveryPositionLink() {
		return everyPositionLink;
	}

	public void setEveryPositionLink(boolean everyPositionLink) {
		this.everyPositionLink = everyPositionLink;
	}

	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		String aCBox = null;
		String name = null;
		String value = null;
		this.intelliAdShown = false;
		this.staticLink = false;

		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			name = names.nextElement();
			if (name.startsWith(STRUTS_CHECKBOX) && name.length() > 18) {
				aCBox = name.substring(18);
				try {
					if ((value = request.getParameter(name)) != null) {
						BeanUtils.setProperty(this, aCBox, value);
					}
				} catch (Exception e) {
					logger.error("reset: " + e.getMessage());
				}
			}
		}
	}

	public void setLinkToView(ComTrackableLink linkToView) {
		this.linkToView = linkToView;
	}

	public ComTrackableLink getLinkToView() {
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
	
	/**
	 * Returns if IntelliAd link tracking is enabled.
	 * 
	 * @return true, if IntelliAd is enabled
	 */
	public boolean isIntelliAdEnabled() {
		return this.intelliAdEnabled;
	}
	
	/**
	 * Set if IntelliAd link tracking is enabled.
	 * 
	 * @param intelliAdEnabled true, if IntelliAd is enabled
	 */
	public void setIntelliAdEnabled( boolean intelliAdEnabled) {
		this.intelliAdEnabled = intelliAdEnabled;
	}
	
	/**
	 * Returns the ID string for IntelliAd link tracking.
	 * 
	 * @return ID string for IntelliAd link tracking
	 */
	public String getIntelliAdIdString() {
		return this.intelliAdIdString;
	}
	
	/**
	 * Sets the ID string for IntelliAd link tracking.
	 * 
	 * @param intelliAdIdString ID string for IntelliAd link tracking
	 */
	public void setIntelliAdIdString( String intelliAdIdString) {
		this.intelliAdIdString = intelliAdIdString;
	}
	
	/**
	 * Set to true, if IntelliAd form is shown.
	 * 
	 * @param shown true, if IntelliAd form is shown
	 */
	public void setIntelliAdShown( boolean shown) {
		this.intelliAdShown = shown;
	}
	
	/**
	 * Returns if the IntelliAd form was shown.
	 * 
	 * @return true, if IntelliAd form was shown
	 */
	public boolean isIntelliAdShown() {
		return this.intelliAdShown;
	}

    public int getLinkItemRelevance(int id){
        return linkItemRelevance.getOrDefault(id, 0);
    }

    public void setLinkItemRelevance(int id, int value) {
        linkItemRelevance.put(id, value);
    }

    public void clearLinkItemRelevance() {
        this.linkItemRelevance.clear();
    }
	
	public Map<Integer, Integer> getLinkItemsRelevance() {
		return linkItemRelevance;
	}
	
    public int getLinkItemDeepTracking(int id){
        return linkItemDeepTracking.getOrDefault(id, 0);
    }
	
	public void setLinkItemDeepTracking(int id, int value) {
        linkItemDeepTracking.put(id, value);
    }

    public void clearLinkItemDeepTracking() {
        this.linkItemDeepTracking.clear();
    }
	
	public Map<Integer, Integer> getLinkItemsDeepTracking() {
		return linkItemDeepTracking;
	}
	
	public String getLinkItemName(int id){
        return linkItemName.get(id);
    }

    public void setLinkItemName(int id, String value) {
        linkItemName.put(id, value);
    }

    public void clearLinkItemName() {
        this.linkItemName.clear();
    }
	
	public Map<Integer, String> getLinkItemNames() {
		return linkItemName;
	}
	
	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public boolean isIsMailingGrid() {
		return isMailingGrid;
	}

	public void setMailingGrid(boolean mailingGrid) {
		isMailingGrid = mailingGrid;
	}

    public int getScrollToLinkId() {
        return scrollToLinkId;
    }

    public void setScrollToLinkId(int scrollToLinkId) {
        this.scrollToLinkId = scrollToLinkId;
    }

	public boolean getIsMailingUndoAvailable() {
		return isMailingUndoAvailable;
	}

	public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
		this.isMailingUndoAvailable = isMailingUndoAvailable;
	}
	
	public final void setStaticLink(final boolean value) {
		this.staticLink = value;
	}
	
	public final boolean isStaticLink() {
		return this.staticLink;
	}
}
