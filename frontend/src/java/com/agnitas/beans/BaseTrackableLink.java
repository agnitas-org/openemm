/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.agnitas.beans.LinkProperty;

public interface BaseTrackableLink {
	
	int KEEP_UNCHANGED = -1;
 
	/**
	 *  link should not be tracked
	 */
	/*
	 * TODO Check this constant.
	 * 
	 * This constant is used by BackTrackableLink.getUsage() and .setUsage(), but
	 * seems to be colliding with TrackableLink.NONE.
	 */
    int TRACKABLE_NO = 0;											
    
	/*
	 * TODO Check this constant.
	 * 
	 * This constant is used by BackTrackableLink.getUsage() and .setUsage(), but
	 * seems to be colliding with TrackableLink.TEXT.
	 */
    /**
	 * link should be tracked
	 */
    int TRACKABLE_YES = 1;
	
	/**
	 * link should be tracked with mailing information(when applicable)
	 */
	/*
	 * TODO Check this constant.
	 * 
	 * This constant is used by BackTrackableLink.getUsage() and .setUsage(), but
	 * seems to be colliding with TrackableLink.HTML.
	 */
    int TRACKABLE_YES_WITH_MAILING_INFO = 2;
	
	/**
	 * link should be tracked with mailing information(when applicable) and
	 * user(when applicable)
	 */
	/*
	 * TODO Check this constant.
	 * 
	 * This constant is used by BackTrackableLink.getUsage() and .setUsage(), but
	 * seems to be colliding with TrackableLink.TEXT_HTML.
	 */
    int TRACKABLE_YES_WITH_MAILING_AND_USER_INFO = 3;
    
    /**
	 * Getter for property urlID.
	 *
	 * @return Value of property urlID.
	 */
    int getId();
    
    /**
	 * Setter for property urlID.
	 *
	 * @@param New value of property urlID.
	 */
    void setId(int id);
    
    /**
	 * Getter for property shortname.
	 *
	 * @return Value of property shortname.
	 */
    String getShortname();
    
    /**
	 * Setter for property shortname.
	 *
	 * @param shortname New value of property shortname.
	 */
    void setShortname(String shortname);
    
    /**
	 * Getter for property companyID.
	 *
	 * @return Value of property companyID.
	 */
    int getCompanyID();
    
    /**
	 * Setter for property companyID.
	 *
	 * @param companyID New value of property companyID.
	 */
    void setCompanyID(int companyID);
    
    /**
     * Getter for property actionID.
     *
     * @return Value of property actionID.
     */
    int getActionID();
    
    /**
	 * Setter for property actionID.
	 *
	 * @param actionID New value of property actionID.
	 */
    void setActionID(int actionID);
    
    /**
	 * Getter for property fullUrl.
	 *
	 * @return Value of property fullUrl.
	 */
    String getFullUrl();

    /**
	 * Setter for property fullUrl.
	 *
	 * @param fullUrl New value of property fullUrl.
	 */
    void setFullUrl(String fullUrl);

	/**
	 * Setter for property usage.
	 *
	 * @param usage New value of property usage.
	 */
	void setUsage(int usage);

	/**
	 * Getter for property usage.
	 *
	 * @return Value of property usage.
	 */
	int getUsage();

	/**
     * Getter for property DeepTracking.
     *
     * @return Value of property DeepTracking.
     */
    int getDeepTracking();

    /**
     * Setter for property DeepTracking.
     *
     * @param deepTracking New value of property DeepTracking.
     */
    void setDeepTracking(int deepTracking);

    /**
	 * Getter for property properties.
	 *
	 * @return Value of property properties.
	 */
    List<LinkProperty> getProperties();

    /**
	 * Setter for property properties.
	 *
	 * @param linkProperties New value of property properties.
	 */
    void setProperties(List<LinkProperty> linkProperties);
    
    /**
     * This method extends the full url of this link with its link extensions for display purposes.
     * User or mailing data is not used, so hash-tags will be left empty.
     * For usage of user and mailing data in correct replacements of hash-tagsuse,
     * use the methods of corresponding actions like "MailingContentController"
     *
     * Caution:
     * This is used by JSP-Files
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    String createDirectLinkWithOptionalExtensionsWithoutUserData() throws UnsupportedEncodingException;
    
    /**
	 * Method that count number of links with type {@link LinkProperty.PropertyType#LinkExtension }
	 *
	 * @return int value of quantity
	 */
    int getLinkExtensionCount();
    
    /**
	 * Exception safe method of {@link #createDirectLinkWithOptionalExtensionsWithoutUserData()}
     *
     * Caution:
     * Is used by JSP-Files
	 *
	 * @return full url with escaped and empty string if method throws exception
	 */
    String getFullUrlWithExtensions();
}
