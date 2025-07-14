/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Date;

import com.agnitas.beans.Admin;

/**
 * Service to stop delivery / generation of mailings. 
 */
public interface MailingStopService {

	/**
	 * Stops generation / delivery of mailings.
	 * <p>
	 * If mailing is in generation or delivery stage, generation or delivery is paused and can be resumed.
	 * If mailing is scheduled, but has not reached generation stage, the mailing is aborted and can be
	 * resumed by scheduling it again.
	 * <p>
	 * Also deactivates all associates follow-up mailings.
	 * 
	 * @param companyID company ID of mailing
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mailing has been stopped
	 * 
	 * @throws MailingStopServiceException on errors processing stop request
	 */
	boolean stopMailing(final int companyID, final int mailingID, final boolean includeUnscheduled) throws MailingStopServiceException;
	
	boolean resumeMailing(final int companyID, final int mailingID) throws MailingStopServiceException;
	
	/**
	 * Copies the stopped mailing for resume.
	 * <p>
	 * Requires enabled mailtracking for company.
	 * 
	 * @param admin admin
	 * @param mailingID mailing ID
	 * 
	 * @return mailing ID of copy
	 * 
	 * @throws MailingStopServiceException on errors during processing
	 * @throws MailtrackingNotEnabledException if mailtracking is not enabled for company
	 */
	int copyMailingForResume(final Admin admin, final int mailingID) throws MailingStopServiceException;
	
	boolean isStopped(final int mailingID);
	
	/**
	 * Checks if generation / delivery of mailing can be stopped.
	 * <p>
	 * Returns <code>false</code> if mailing cannot be stopped.
	 *   
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mailing can be stopped
	 */
	boolean canStopMailing(final int companyID, final int mailingID);
	
	/**
	 * Checks if generation / delivery of mailing can be resumed.
	 * <p>
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
