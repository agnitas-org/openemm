/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.emmapi;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.agnitas.beans.DynamicTag;

public interface VelocityMailing {

	/**
	 * Returns the mailing ID.
	 * 
	 * <p>
	 *   <b>Note:</b> This method was found in the Velocity documentation of EMM, but was never
	 *   used. Real Velocity scripts use {@link #getMailingID()} instead.
	 * </p>
	 * 
	 * @return ID of mailing
	 */
	int getId();
	
	/**
	 * Sets the mailing ID.
	 * 
	 * <p>
	 *   <b>Note:</b> This method was found in the Velocity documentation of EMM, but was never
	 *   used. Real Velocity scripts use {@link #setMailingID(int)} instead.
	 * </p>
	 * 
	 * @param id mailing ID
	 */
	void setId(final int id);
	
	/**
	 * Returns the mailing ID.
	 * 
	 * <p>
	 *   <b>Note:</b> This method is used by real Velocity scripts use {@link #getMailingID()}, but is not documented.
	 * </p>
	 * 
	 * @return ID of mailing
	 */
	int getMailingID();
	
	/**
	 * Sets the mailing ID.
	 * 
	 * <p>
	 *   <b>Note:</b> This method is used by real Velocity scripts use {@link #setId(int)}, but is not documented.
	 * </p>
	 * 
	 * @param id mailing ID
	 */
	void setMailingID(final int id);

	int getMailinglistID();
	void setMailinglistID(final int id);

	String getShortname();
	void setShortname(final String shortname);
	
    int getMailingType();
    void setMailingType(final int mailingType);
    
    DynamicTag getDynamicTagById(final int dynId);
    Map<String, DynamicTag> getDynTags();
    
    boolean sendEventMailing(final int customerID, final int delayMinutes, final String userStatus, final Map<String, String> overwrite, final ApplicationContext con);
    boolean sendEventMailing(final int customerID, final int delayMinutes, final List<Integer> userStatusList, final Map<String, String> overwrite, final ApplicationContext con);
}

