/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.bean;

import org.agnitas.beans.BaseTrackableLink;

/**
 * Bean interface for trackable links within a user form
 */
public interface ComTrackableUserFormLink extends BaseTrackableLink {

	
	
	
    /**
     * Getter for property formID.
     * 
     * @return Value of property formID.
     */
    int getFormID();

    /**
     * Getter for property usage.
     * 
     * @return Value of property usage.
     */
    int getUsage();

     /**
     * Setter for property FormID.
     * 
     * @param id New value of property FormID.
     */
    void setFormID(int id);

    /**
     * Setter for property usage.
     * 
     * @param usage New value of property usage.
     */
    void setUsage(int usage);

    /**
     * Getter for property relevance.
     *
     * @return Value of property relevance.
     */
    String getDeepTrackingUID();

    /**
     * Getter for property relevance.
     *
     * @return Value of property relevance.
     */
    String getDeepTrackingSession();

    /**
     * Getter for property relevance.
     *
     * @return Value of property relevance.
     */
    int getDeepTracking();

    /**
     * Setter for property relevance.
     *
     * @param deepTracking New value of property relevance.
     */
    void setDeepTracking(int deepTracking);
    
    /**
     * Getter for property relevance.
     *
     * @return Value of property relevance.
     */
    int getRelevance();

    /**
     * Setter for property relevance.
     *
     * @param relevance New value of property relevance.
     */
    void setRelevance(int relevance);
}

