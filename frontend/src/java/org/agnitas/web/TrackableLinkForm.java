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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.TrackableLink;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

public class TrackableLinkForm extends StrutsFormBase {

    private static final long serialVersionUID = -7395594021916269872L;

	/**
     * Holds value of property action.
     */
    private int action;

    /**
     * Holds value of property linkID.
     */
    private int linkID;

    /**
     * Holds value of property linkName.
     */
    private String linkName;

    /**
     * Holds value of property linkAction.
     */
    private int linkAction;

    /**
     * Holds value of property trackable.
     */
    private int trackable;

    /**
     * Holds value of property linkUrl.
     */
    private String linkUrl;

    /**
     * Holds value of property mailingID.
     */
    private int mailingID;

    /**
     * Holds value of property deepTracking.
     */
    private int deepTracking;

    /**
     * Holds value of property relevance.
     */
    private int relevance;

    private boolean trackableContainerVisible;

    private boolean actionContainerVisible;

    private int clickActionID;

    private int openActionID;

    private String defaultActionType;

    private Set<Integer> bulkIDs = new HashSet<>();

    private Map<Integer, Integer> linkItemAction = new HashMap<>();

    private Map<Integer, Integer> linkItemTrackable = new HashMap<>();

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
	public ActionErrors formSpecificValidate(ActionMapping mapping,
    HttpServletRequest request) {

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
     * Getter for property mailingID.
     *
     * @return Value of property mailingID.
     */
    public int getMailingID() {
        return this.mailingID;
    }

    /**
     * Setter for property mailingID.
     *
     * @param mailingID New value of property mailingID.
     */
    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    /**
     * Holds value of property shortname.
     */
    private String shortname;


    private String description;
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
     * Holds value of property isTemplate.
     */
    private boolean isTemplate;

    /**
     * Getter for property isTemplate.
     *
     * @return Value of property isTemplate.
     */
    public boolean isIsTemplate() {
        return this.isTemplate;
    }

    /**
     * Setter for property isTemplate.
     *
     * @param isTemplate New value of property isTemplate.
     */
    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    /**
     * Holds value of property links.
     */
    private Collection<TrackableLink> links;

	private int globalUsage;

    /**
     * Getter for property links.
     *
     * @return Value of property links.
     */
    public Collection<TrackableLink> getLinks() {
        return this.links;
    }

    /**
     * Setter for property links.
     *
     * @param links New value of property links.
     */
    public void setLinks(Collection<TrackableLink> links) {
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

	public int getGlobalUsage() {
		return globalUsage;
	}

	public void setGlobalUsage(int globalUsage) {
		this.globalUsage = globalUsage;
	}

    public boolean isTrackableContainerVisible() {
        return trackableContainerVisible;
    }

    public void setTrackableContainerVisible(boolean trackableContainerVisible) {
        this.trackableContainerVisible = trackableContainerVisible;
    }

    public boolean isActionContainerVisible() {
        return actionContainerVisible;
    }

    public void setActionContainerVisible(boolean actionContainerVisible) {
        this.actionContainerVisible = actionContainerVisible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getClickActionID() {
        return clickActionID;
    }

    public void setClickActionID(int clickActionID) {
        this.clickActionID = clickActionID;
    }

    public int getOpenActionID() {
        return openActionID;
    }

    public void setOpenActionID(int openActionID) {
        this.openActionID = openActionID;
    }

    public String getDefaultActionType() {
        return defaultActionType;
    }

    public void setDefaultActionType(String defaultActionType) {
        this.defaultActionType = defaultActionType;
    }

    public int getLinkItemAction(int id){
        Integer linkItemVal = linkItemAction.get(id);
        return linkItemVal != null ? linkItemVal : 0;
    }

    public void setLinkItemAction(int id, int value) {
        linkItemAction.put(id, value);
    }

    public void clearLinkItemActions() {
        this.linkItemAction.clear();
    }

    public int getLinkItemTrackable(int id){
        Integer linkItemVal = linkItemTrackable.get(id);
        return linkItemVal != null ? linkItemVal : 0;
    }

    public void setLinkItemTrackable(int id, int value) {
        linkItemTrackable.put(id, value);
    }

    public void clearLinkItemTrackable() {
        this.linkItemTrackable.clear();
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
