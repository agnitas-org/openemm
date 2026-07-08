/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.service;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.importvalues.MailType;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MaildropService {		// TODO: Complete JavaDoc

	boolean stopWorldMailingBeforeGeneration(final int companyID, final int mailingID);
	
	boolean hasMaildropStatus(final int mailingID, final int companyID, final MaildropStatus... statusList);
	
	/**
	 * Returns <code>true</code> is world mailing is sent or scheduled to send or action-based/date-based mailing is activated.
	 */
	boolean isActiveMailing(final int mailingID, final int companyID);

	/**
	 * Store all {@code customerIds} as test recipients associated with referenced {@code maildropStatusId}.
	 *
	 * @param companyId an identifier of a company that the referenced maildrop entry belongs to.
	 * @param maildropStatusId an identifier of a maildrop entry.
	 * @param customerIds a list of customer ids to associated with referenced maildrop entry.
	 */
    void selectTestRecipients(int companyId, int maildropStatusId, List<Integer> customerIds);

	void writeMailingSendStatisticsEntry(int companyID, int mailingID, MaildropStatus maildropStatus, MediaTypes mediaType, MailType mailType, int amount, int dataSize, Date sendDate, String mailerHostname);

	List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded);

	Optional<MaildropEntry> findMaildrop(int mailingId, int companyId, MaildropStatus... statuses);

	int saveMaildropEntry(final MaildropEntry entry);

	void cleanupOldEntriesByMailingID(int mailingID, int maximumAgeInDays);

	Map<Integer, List<Integer>> cleanupFailedTestDeliveries();

	int cleanup(Collection<MaildropEntry> entries);

	MaildropEntry getEntryForStatus(int mailingID, int companyID, char status);

	MaildropEntry getMaildropEntry(int mailingId, int companyId, int statusId);

	int getLastMaildropEntryId(int mailingId, int companyId);

	List<MaildropEntry> getMaildropStatusEntriesForMailing(int companyID, int mailingID);

	void saveMaildropEntries(int companyId, int mailingId, Set<MaildropEntry> maildropStatusList);
}
