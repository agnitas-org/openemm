/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverprio.server;

import java.util.Date;

import com.agnitas.emm.core.mailing.service.MailingStopService;

/**
 * Lower-level methods to stop and resume mail generation and delivery.
 * 
 * This service does not stop mailings, that are in pre-generation stage.
 * The phase-independent implementation can be found in {@link MailingStopService}.
 * 
 * @see MailingStopService
 */
public interface ServerPrioService {

	/**
	 * Pauses mail generation and delivery.
	 * 
	 * Note: Nothing is done, if mailing is not in generation or delivery stage. 
	 * 
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mail generation has been paused
	 */
	public boolean pauseMailGenerationAndDelivery(final int mailingID);
	
	/**
	 * Resumes mail generation and delivery.
	 * 
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mail generation or delivery is resumed
	 */
	public boolean resumeMailGenerationAndDelivery(final int mailingID);
	
	/**
	 * Returns <code>true</code> if mail generation and delivery is paused.
	 * 
	 * @param mailingID mailing ID
	 * 
	 * @return <code>true</code> if mail generation and delivery is paused
	 */
	public boolean isMailGenerationAndDeliveryPaused(final int mailingID);

	Date getDeliveryPauseDate(int companyId, int mailingId);
}
