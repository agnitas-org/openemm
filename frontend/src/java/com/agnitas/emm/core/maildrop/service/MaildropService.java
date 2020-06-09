/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.service;

import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.maildrop.MaildropException;
import com.agnitas.emm.core.maildrop.MaildropStatus;

public interface MaildropService {		// TODO: Complete JavaDoc

	public boolean stopWorldMailingBeforeGeneration(final int companyID, final int mailingID);
	
	// TODO: Modify code to use this method
	/**
	 * Schedules mailing for immediate test delivery to admin recipients.
	 */
	int scheduleAdminMailing(final int mailingID, final int companyID, final int adminTargetID) throws MaildropException;

	// TODO: Modify code to use this method
	/**
	 * Schedules mailing for immediate test delivery to test recipients.
	 * @return 
	 */
	int scheduleTestMailing(final int mailingID, final int companyID, final int testTargetID) throws MaildropException;
	
	// TODO: Modify code to use this method
	/**
	 * Schedules mailing for worlds delivery.
	 */
	void scheduleWorldMailing(final int mailingID, final int companyID, final Date sendDate, final int stepping, final int blocksize) throws MaildropException;
	
	// TODO: Modify code to use this method
	/**
	 * Schedules mailing for worlds delivery.
	 */
	void scheduleWorldMailing(final int mailingID, final int companyID, final Date sendDate, final int mailsPerHour) throws MaildropException;

	// TODO: Modify code to use this method
	/**
	 * Activates date-based mailing.
	 */
	void activateDatebasedMailing(final int mailingID, final int companyID, final int hour, final int stepping, final int blocksize) throws MaildropException;

	// TODO: Modify code to use this method
	/**
	 * Activates date-based mailing.
	 */
	void activateDatebasedMailing(final int mailingID, final int companyID, final int hour, final int mailsPerHour) throws MaildropException;

	// TODO: Modify code to use this method
	/**
	 * Deactivates date-based mailing.
	 */
	void deactivateDatebasedMailing(final int mailingID, final int companyID) throws MaildropException;
	
	// TODO: Modify code to use this method
	/**
	 * Activates action-based mailing.
	 */
	void activateActionbasedMailing(final int mailingID, final int companyID) throws MaildropException;
	
	// TODO: Modify code to use this method
	/**
	 * Deactivates action-based mailing.
	 */
	void deactivateActionbasedMailing(final int mailingID, final int companyID) throws MaildropException;
	
	// TODO: Modify code to use this method
	boolean hasMaildropStatus(final int mailingID, final int companyID, final MaildropStatus... statusList);
	
	/**
	 * Returns <code>true</code> is world mailing is sent or scheduled to send or action-based/date-based mailing is activated.
	 * @return 
	 */
	boolean isActiveMailing(final int mailingID, final int companyID);

	/**
	 * Store all {@code customerIds} as test recipients associated with referenced {@code maildropStatusId}.
	 *
	 * @param companyId an identifier of a company that the referenced maildrop entry belongs to.
	 * @param maildropStatusId an identifier of a maildrop entry.
	 * @param customerIds a list of customer ids to associated with referenced maildrop entry.
	 */
    void selectTestRecipients(@VelocityCheck int companyId, int maildropStatusId, List<Integer> customerIds);

}
