/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Date;

/**
 * Service to stop delivery / generation of mailings. 
 */
public interface MailingStopService {

	/**
	 * Stops generation / delivery of mailings.
	 * 
	 * If mailing is in generation or delivery stage, generation or delivery is paused and can be resumed.
	 * If mailing is scheduled, but has not reached generation stage, the mailing is aborted and can be
	 * resumed by scheduling it again.
	 * 
	 * Also deactivates all associates follow-up mailings.
	 * 
	 * @param companyID company ID of mailing
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mailing has been stopped
	 * 
	 * @throws MailingStopServiceException on errors processing stop request
	 */
	public boolean stopMailing(final int companyID, final int mailingID, final boolean includeUnscheduled) throws MailingStopServiceException;
	
	public boolean resumeMailing(final int companyID, final int mailingID) throws MailingStopServiceException;
	
	public int copyMailingForResume(final int companyID, final int mailingID, final String shortnameOfCopy, final String descriptionOfCopy) throws MailingStopServiceException;
	
	public boolean isStopped(final int mailingID);
	
	/**
	 * Checks if generation / delivery of mailing can be stopped.
	 * 
	 * Returns <code>false</code> if mailing cannot be stopped.
	 *   
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mailing can be stopped
	 */
	public boolean canStopMailing(final int companyID, final int mailingID);
	
	/**
	 * Checks if generation / delivery of mailing can be resumed.
	 * 
	 * Returns <code>false</code> if mailing cannot be resumed.
	 *   
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mailing can be resumed
	 */
	boolean isStopped(final int companyID, final int mailingID);

	Date getDeliveryPauseDate(final int companyId, final int mailingId);
}
