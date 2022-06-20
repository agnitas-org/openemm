/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.beans.TrackableLink;

public interface ComTrackableLink extends TrackableLink {
    /**
     * Getter for property altText.
     * 
     * @return Value of property altText.
     */
    String getAltText();

    /**
     * Setter for property altText.
     * 
     * @param altText New value of property altText.
     */
    void setAltText(String altText);

    /**
	 * Checks, if trackable link is not longer used by mailing.
	 *
	 * return true, if link is not longer used, otherwise true
	 */
    boolean isDeleted();

	/**
	 * Marks link as not longer used by mailing.
	 *
	 * @param deleted
	 *            true if not longer used
	 */
    void setDeleted(boolean deleted);
	
	boolean isExtendByMailingExtensions();
	
    void setExtendByMailingExtensions(boolean value);
    
    /**
     * Set original URL of the link, if current URL is modified.
     * 
     * @param url orignal URL
     */
    void setOriginalUrl(String url);
    
    /**
     * Returns the original URL of the link. If the return value is blank, empty or null, the URL of the
     * link is unmodified.
     * 
     * @return original URL of link
     */
    String getOriginalUrl();
    
    /**
     * Returns true, if link target is modified.
     * 
     * @return true, if link target is modified
     */
    boolean isUrlModified();

	void setStaticValue(final boolean flag);
	
	boolean isStaticValue();

    void setMeasureSeparately(boolean measureSeparately);

    boolean isMeasureSeparately();

	boolean isCreateSubstituteLinkForAgnDynMulti();
	
	void setCreateSubstituteLinkForAgnDynMulti(final boolean createSubstituteForAgnDynMulti);
}
