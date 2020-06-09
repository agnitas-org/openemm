/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.LinkProperty;
import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

public abstract class BaseTrackableLinkForm extends StrutsFormBase {

    private static final long serialVersionUID = -7395594021916269872L;
    
	public static final String PROPERTY_NAME_PREFIX = "propertyName_";
	public static final String PROPERTY_VALUE_PREFIX = "propertyValue_";

	/**
     * Holds value of property action.
     */
    protected int action;

    /**
     * Holds value of property linkID.
     */
    protected int linkID;

    /**
     * Holds value of property shortname.
     */
    protected String shortname;

    /**
     * Holds value of property shortname.
     */
    protected String description;
    
    /**
     * Holds value of property linkName.
     */
    protected String linkName;

    /**
     * Holds value of property linkAction.
     */
    protected int linkAction;

    /**
     * Holds value of property trackable.
     */
    protected int trackable;

    /**
     * Holds value of property linkUrl.
     */
    protected String linkUrl;

    /**
     * Holds value of property deepTracking.
     */
    protected int deepTracking;

    /**
     * Holds value of property relevance.
     */
    protected int relevance;
    
    /**
     * Holds value of property links.
     */
    protected Collection<? extends BaseTrackableLink> links;
    
    /**
     * Holds value of property links.
     */
    protected String altText;

    /**
     * Holds value of property commonLinkExtensions.
     */
    protected List<LinkProperty> commonLinkExtensions;
    
    protected boolean companyHasDefaultLinkExtension = false;

    protected Map<Integer, Integer> linkItemRelevance = new HashMap<>();

	protected Map<Integer, String> linkItemName = new HashMap<>();
	
    protected Set<Integer> bulkIDs = new HashSet<>();


    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        if(!errors.isEmpty()) {
            mapping.setInput(mapping.findForward("view").getPath());
        }
        return errors;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * Getter for property fontID.
     *
     * @return Value of property fontID.
     */
    public int getLinkID() {
        return this.linkID;
    }

    /**
     * Setter for property fontID.
     *
     * @param linkID
     */
    public void setLinkID(int linkID) {
        this.linkID = linkID;
    }

    /**
     * Getter for property fontName.
     *
     * @return Value of property fontName.
     */
    public String getLinkName() {
        return this.linkName;
    }

    /**
     * Setter for property fontName.
     *
     * @param linkName
     */
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    /**
     * Getter for property linkAction.
     *
     * @return Value of property linkAction.
     */
    public int getLinkAction() {
        return this.linkAction;
    }

    /**
     * Setter for property linkAction.
     *
     * @param linkAction New value of property linkAction.
     */
    public void setLinkAction(int linkAction) {
        this.linkAction = linkAction;
    }

    /**
     * Getter for property trackable.
     *
     * @return Value of property trackable.
     */
    public int getTrackable() {
        return this.trackable;
    }

    /**
     * Setter for property trackable.
     *
     * @param trackable New value of property trackable.
     */
    public void setTrackable(int trackable) {
        this.trackable = trackable;
    }

    /**
     * Getter for property linkUrl.
     *
     * @return Value of property linkUrl.
     */
    public String getLinkUrl() {
        return this.linkUrl;
    }

    /**
     * Setter for property linkUrl.
     *
     * @param linkUrl New value of property linkUrl.
     */
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return this.shortname;
    }
    
    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * Getter for property links.
     *
     * @return Value of property links.
     */
    public Collection<? extends BaseTrackableLink> getLinks() {
        return this.links;
    }

    /**
     * Setter for property links.
     *
     * @param links New value of property links.
     */
    public void setLinks(Collection<? extends BaseTrackableLink> links) {
        this.links = links;
    }

    /**
     * Getter for property deepTracking.
     * @return Value of property deepTracking.
     */
    public int getDeepTracking() {
        return this.deepTracking;
    }

    /**
     * Setter for property deepTracking.
     * @param deepTracking New value of property deepTracking.
     */
    public void setDeepTracking(int deepTracking) {
        this.deepTracking = deepTracking;
    }

    /**
     * Getter for property relevance.
     * @return Value of property relevance.
     */
    public int getRelevance() {
        return this.relevance;
    }

    /**
     * Setter for property relevance.
     * @param relevance New value of property relevance.
     */
    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Getter for property altText.
     * @return Value of property altText.
     */
    public String getAltText() {
        return altText;
    }
    
    /**
     * Setter for property altText.
     * @param altText New value of property altText.
     */
    public void setAltText(String altText) {
        this.altText = altText;
    }
    
    /**
     * Getter for property linkItemRelevance by link id.
     * @param id link identifier
     * @return Value of property link name for specific link.
     */
    public int getLinkItemRelevance(int id){
        return linkItemRelevance.getOrDefault(id, 0);
    }
    
    /**
     * Setter for property linkItemRelevance.
     * @param id link id
     * @param value new value of relevance
     */
    public void setLinkItemRelevance(int id, int value) {
        linkItemRelevance.put(id, value);
    }
    
    public void clearLinkItemRelevance() {
        this.linkItemRelevance.clear();
    }
    
    /**
     * Getter for property linkItemRelevance.
     * @return Value of property linkItemRelevance.
     */
    public Map<Integer, Integer> getLinkItemsRelevance() {
		return linkItemRelevance;
	}
	
	/**
     * Getter for property linkItemName by link id.
     * @return Value of property link name for specific link.
     */
	public String getLinkItemName(int id){
        return linkItemName.get(id);
    }
    
    /**
     * Setter for property linkItemName.
     * @param id link identifier
     * @param value new value of link name
     */
    public void setLinkItemName(int id, String value) {
        linkItemName.put(id, value);
    }

    public void clearLinkItemName() {
        this.linkItemName.clear();
    }

    /**
     * Getter for property linkItemName.
     * @return map of values properties names for each link
     */
	public Map<Integer, String> getLinkItemNames() {
		return linkItemName;
	}
    
    /**
     * Getter for property commonLinkExtensions.
     * @return Value of property commonLinkExtensions.
     */
	public List<LinkProperty> getCommonLinkExtensions() {
		return commonLinkExtensions;
	}

    /**
     * Setter for property commonLinkExtensions.
     * @param commonLinkExtensions new value of link commonLinkExtensions
     */
	public void setCommonLinkExtensions(List<LinkProperty> commonLinkExtensions) {
		this.commonLinkExtensions = commonLinkExtensions;
	}
    
    /**
     * Getter for property companyHasDefaultLinkExtension.
     * @return Value of property companyHasDefaultLinkExtension
     */
	public boolean getCompanyHasDefaultLinkExtension() {
		return companyHasDefaultLinkExtension;
	}
    
    /**
     * Setter for property commonLinkExtensions.
     * @param companyHasDefaultLinkExtension new value of link companyHasDefaultLinkExtension
     */
	public void setCompanyHasDefaultLinkExtension(boolean companyHasDefaultLinkExtension) {
		this.companyHasDefaultLinkExtension = companyHasDefaultLinkExtension;
	}
	
	public String getBulkID(int id) {
        return this.bulkIDs.contains(id) ? "on" : "";
    }

    public void setBulkID(int id, String value) {
        if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true")))
            this.bulkIDs.add(id);
    }

    public Set<Integer> getBulkIDs() {
        return bulkIDs;
    }

    public void setBulkIDs(Set<Integer> bulkIDs) {
        this.bulkIDs = bulkIDs;
    }

    public void clearBulkIDs() {
        this.bulkIDs.clear();
    }
}
