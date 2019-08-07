/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.bean;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.LinkProperty;

/**
 * Bean interface for trackable links within a user form
 */
public interface ComTrackableUserFormLink {
	/**
	 *  link should not be tracked
	 */
	static final int TRACKABLE_NO = 0;
	
	/**
	 * link should be tracked
	 */
	static final int TRACKABLE_YES = 1;
	
	/**
	 * link should be tracked with mailing information(when applicable) 
	 */
	static final int TRACKABLE_YES_WITH_MAILING_INFO = 2;
	
	/**
	 * link should be tracked with mailing information(when applicable) and
	 * user(when applicable)
	 */
	static final int TRACKABLE_YES_WITH_MAILING_AND_USER_INFO = 3;
	

    static final int DEEPTRACKING_NONE = 0;
    static final int DEEPTRACKING_ONLY_COOKIE = 1;
    static final int DEEPTRACKING_ONLY_URL = 2;
    static final int DEEPTRACKING_BOTH = 3;

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
     * Getter for property formID.
     * 
     * @return Value of property formID.
     */
    int getFormID();

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
    void setCompanyID(@VelocityCheck int id);

     /**
     * Setter for property fullUrl.
     * 
     * @param url New value of property fullUrl.
     */
    void setFullUrl(String url);

     /**
     * Setter for property FormID.
     * 
     * @param id New value of property FormID.
     */
    void setFormID(int id);

    /**
     * Setter for property shortname.
     * 
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * set id of link
     * @param id - id of link
     */
    void setId(int id);

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
     * @param relevance New value of property relevance.
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
    	
	void setProperties(List<LinkProperty> linkProperties);
	
	List<LinkProperty> getProperties();
	
	String createDirectLinkWithOptionalExtensionsWithoutUserData() throws UnsupportedEncodingException;
}

