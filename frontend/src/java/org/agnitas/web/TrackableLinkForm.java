/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;


import java.util.HashMap;
import java.util.Map;

public class TrackableLinkForm extends BaseTrackableLinkForm {

    private static final long serialVersionUID = -7395594021916269872L;

    /**
     * Holds value of property mailingID.
     */
    protected int mailingID;

    protected boolean trackableContainerVisible;

    protected boolean actionContainerVisible;

    protected int clickActionID;

    protected int openActionID;

    protected String defaultActionType;

    protected Map<Integer, Integer> linkItemAction = new HashMap<>();

    protected Map<Integer, Integer> linkItemTrackable = new HashMap<>();
    
    /**
     * Holds value of property isTemplate.
     */
    protected boolean isTemplate;
    
    /**
     * Holds value of property globalUsage.
     */
    protected int globalUsage;

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

    @Override
	public void setDir(final String orderDirection) {
        setOrder(orderDirection);
    }
}
