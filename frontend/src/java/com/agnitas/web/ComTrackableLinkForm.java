/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import static com.agnitas.beans.LinkProperty.PropertyType.LinkExtension;

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

	private boolean linkContainerVisible;
	
	private boolean linkExtensionsContainerVisible;
	
	private boolean webshopContainerVisible;

	private Set<Integer> adminLinkIds = new HashSet<>();
	
	private boolean adminLink;

	private boolean everyPositionLink;

	private ComTrackableLink linkToView;

    private boolean bulkModifyLinkExtensions;
    
    private boolean modifyExtensionsForAllLinks;

    private Map<Integer, LinkProperty> commonExtensions = new HashMap<>();

    /** Flag, if IntelliAd link tracking is enabled. */
    private boolean intelliAdEnabled;
    
    /** ID-String from IntelliAd. */
    private String intelliAdIdString;
    
    private boolean intelliAdShown;

    private Map<Integer, Integer> linkItemDeepTracking = new HashMap<>();

	private int workflowId;

	private boolean isMailingGrid;

    private int scrollToLinkId;

	private boolean isMailingUndoAvailable;
	
	private boolean staticLink;
	
	private int bulkLinkStatic;
	
    private String bulkDescription;
	
    private boolean modifyBulkDescription;
    
	/**
	 * this variable is used for the following issue: if you have two or more
	 * SAME Links in a Mailing, you can not them in the mailing statistic,
	 * because they get all the same ID. If the variable is set to false, we
	 * will make them unique and then you get a more detailed statistic. Default
	 * is true (no difference between the links)
	 */
	private boolean countLinkIsUnique = true;
	
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		String aCBox;
		String name;
		String value;
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

	public void setLinkToView(ComTrackableLink linkToView) {
		this.linkToView = linkToView;
	}

	public ComTrackableLink getLinkToView() {
		return linkToView;
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

    public void setLinkItems(List<ComTrackableLink> trackableLinks) {
        clearLinkItemActions();
        clearLinkItemDeepTracking();
        clearLinkItemTrackable();
        clearLinkItemName();
        clearAdminLinks();
        clearBulkIDs();

        for (ComTrackableLink link : trackableLinks) {
            int id = link.getId();
            setLinkItemAction(id, link.getActionID());
            setLinkItemDeepTracking(id, link.getDeepTracking());
            setLinkItemTrackable(id, link.getUsage());
            setLinkItemName(id, link.getShortname());
            setAdminLink(id, link.isAdminLink());
        }
    }

    public LinkProperty getCommonExtension(int index) {
	    return commonExtensions.computeIfAbsent(index, i -> new LinkProperty(LinkExtension, "", ""));
    }

    public Map<Integer, LinkProperty> getCommonExtensions() {
        return commonExtensions;
    }

    public void setCommonExtensions(Map<Integer, LinkProperty> commonExtensions) {
        this.commonExtensions = commonExtensions;
    }

    public int getBulkLinkStatic() {
        return bulkLinkStatic;
    }

    public void setBulkLinkStatic(int bulkLinkStatic) {
        this.bulkLinkStatic = bulkLinkStatic;
    }

    public String getBulkDescription() {
        return bulkDescription;
    }

    public void setBulkDescription(String bulkDescription) {
        this.bulkDescription = bulkDescription;
    }

    public boolean isModifyBulkDescription() {
        return modifyBulkDescription;
    }

    public void setModifyBulkDescription(boolean modifyBulkDescription) {
        this.modifyBulkDescription = modifyBulkDescription;
    }
    
    public boolean isBulkModifyLinkExtensions() {
        return bulkModifyLinkExtensions;
    }

    public void setBulkModifyLinkExtensions(boolean bulkModifyLinkExtensions) {
        this.bulkModifyLinkExtensions = bulkModifyLinkExtensions;
    }

    public boolean isModifyExtensionsForAllLinks() {
        return modifyExtensionsForAllLinks;
    }

    public void setModifyExtensionsForAllLinks(boolean modifyExtensionsForAllLinks) {
        this.modifyExtensionsForAllLinks = modifyExtensionsForAllLinks;
    }
}
