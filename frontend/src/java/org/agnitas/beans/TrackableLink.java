/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

public interface TrackableLink extends BaseTrackableLink {
    
    int TRACKABLE_NONE = 0;
    int TRACKABLE_ONLY_TEXT = 1;
    int TRACKABLE_ONLY_HTML = 2;
    int TRACKABLE_TEXT_HTML = 3;
    
    int DEEPTRACKING_NONE = 0;
    int DEEPTRACKING_ONLY_COOKIE = 1;
    int DEEPTRACKING_ONLY_URL = 2;
    int DEEPTRACKING_BOTH = 3;

    
    /**
     * Getter for property mailingID.
     * 
     * @return Value of property mailingID.
     */
    int getMailingID();
    
    /**
     * Setter for property mailingID.
     *
     * @param id New value of property mailingID.
     */
    void setMailingID(int id);
    
    /**
     * Getter for property usage.
     *
     * @return Value of property usage.
     */
    int getUsage();

    /**
     * Setter for property usage.
     * 
     * @param usage New value of property usage.
     */
    void setUsage(int usage);

    /**
     * Getter for property deepTracking.
     *
     * @return Value of property deepTracking.
     */
    int getDeepTracking();

    /**
     * Setter for property deepTracking.
     *
     * @param deepTracking New value of property deepTracking.
     */
    void setDeepTracking(int deepTracking);
    
    /**
     * Getter for property relevance.
     *
     * @return Value of property relevance.
     */
    @Deprecated
    int getRelevance();

    /**
     * Setter for property relevance.
     *
     * @param relevance New value of property relevance.
     */
    @Deprecated
    void setRelevance(int relevance);

    /**
     * Setter for property adminLink.
     *
     * @param adminLink New value of property adminLink.
     */
	void setAdminLink(boolean adminLink);
	
	 /**
     * Getter for property adminLink.
     *
     * @return Value of property adminLink.
     */
	boolean isAdminLink();
}
