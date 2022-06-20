/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
