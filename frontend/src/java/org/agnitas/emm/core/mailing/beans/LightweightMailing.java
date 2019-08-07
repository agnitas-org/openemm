/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.beans;

import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Light-weight mailing containing only data used in lists, tables, etc.
 */
public interface LightweightMailing {
	
	/**
	 * Set ID of mailing.
	 * 
	 * @param mailingID ID of mailing
	 */
	public void setMailingID(Integer mailingID);
	
	/**
	 * Returns ID of mailing.
	 * 
	 * @return ID of mailing
	 */
	public Integer getMailingID();
	
	/**
	 * Set company ID of mailing.
	 * 
	 * @param companyID company ID of mailing
	 */
	public void setCompanyID(@VelocityCheck Integer companyID);
	
	/**
	 * Returns company ID of mailing.
	 * 
	 * @return company ID of mailing
	 */
	public Integer getCompanyID();
	
	/**
	 * Set description of mailing.
	 * 
	 * @param mailingDescription description of mailing
	 */
	public void setMailingDescription(String mailingDescription);
	
	/**
	 * Return description of mailing.
	 * 
	 * @return description of mailing
	 */
	public String getMailingDescription();	
	
	/**
	 * Set name of mailing.
	 * 
	 * @param shortname name of mailing.
	 */
	public void setShortname(String shortname);
	
	/**
	 * Return name of mailing.
	 * 
	 * @return name of mailing
	 */
	public String getShortname();

	/**
	 * Transfer data from heavy-weight mailing object to the light-weight mailing
	 * object.
	 * 
	 * @param tmpMailing heavy-weight mailing object
	 */
	public void compressMailingInfo( Mailing tmpMailing);	

}
