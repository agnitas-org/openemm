/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface TrackableLink {
    
    int TRACKABLE_NONE = 0;
    int TRACKABLE_ONLY_HTML = 2;
    int TRACKABLE_ONLY_TEXT = 1;
    int TRACKABLE_TEXT_HTML = 3;
    
    int DEEPTRACKING_NONE = 0;
    int DEEPTRACKING_ONLY_COOKIE = 1;
    int DEEPTRACKING_ONLY_URL = 2;
    int DEEPTRACKING_BOTH = 3;

    /**
     * Getter for property actionID.
     * 
     * @return Value of property actionID.
     */
    int getActionID();

    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    int getCompanyID();

    /**
     * Getter for property fullUrl.
     * 
     * @return Value of property fullUrl.
     */
    String getFullUrl();

    /**
     * Getter for property mailingID.
     * 
     * @return Value of property mailingID.
     */
    int getMailingID();

    /**
     * Getter for property shortname.
     * 
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property urlID.
     * 
     * @return Value of property urlID.
     */
    int getId();

    /**
     * Getter for property usage.
     * 
     * @return Value of property usage.
     */
    int getUsage();
    
     /**
     * Setter for property actionID.
     * 
     * @param id New value of property actionID.
     */
    void setActionID(int id);

     /**
     * Setter for property companysID.
     * 
     * @param id New value of property companyID.
     */
    void setCompanyID( @VelocityCheck int id);

     /**
     * Setter for property fullUrl.
     * 
     * @param url New value of property fullUrl.
     */
    void setFullUrl(String url);

     /**
     * Setter for property mailingID.
     * 
     * @param id New value of property mailingID.
     */
    void setMailingID(int id);

    /**
     * Setter for property shortname.
     * 
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    void setId(int id);

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

	void setAdminLink(boolean adminLink);
	
	boolean isAdminLink();
}
