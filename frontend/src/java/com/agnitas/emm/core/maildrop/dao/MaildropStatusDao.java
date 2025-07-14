/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.dao;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.importvalues.MailType;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MaildropStatusDao {
    /**
     * Deletes maildrop status identified by ID.
     *
     * @param companyId
     *          The ID of a company that a maildrop status belongs to.
     * @param id
     *          The ID of maildrop status to delete.
     * @return true on success.
     */
    boolean delete(int companyId, int id);
    
    boolean delete(final int companyId, final int mailingId, final MaildropStatus status, final MaildropGenerationStatus generationStatus);

    /**
     * Deletes unsent maildrop entries for given mailing id.
     *
     * @param mailingID
     *          The id of mailing.
     * @return count of deleted entities.
     */
    int deleteUnsentEntries(int mailingID);

    /**
     * Delete unsent entries for world mailings and all entries for other delivery types.
     * Remove corresponding entries from the {@code entries} collection and from DB.
     *
     * @return an actual count of entries deleted from database (not from collection).
     */
    int cleanup(Collection<MaildropEntry> entries);

	Collection<MaildropEntry> listMaildropStatus(final int mailingID, final int companyID);

	/**
	 * Returns maildrop entry for given ID or <code>null</code> if no such entry exists.
	 *
	 * @param mailingID mailing ID
	 * @param companyID company ID
	 * @param statusID maildrop status ID
	 *
	 * @return maildrop entry or <code>null</code>
	 */
	MaildropEntry getMaildropEntry(final int mailingID, final int companyID, final int statusID);

	List<Integer> getMaildropEntryIds(int mailingID, int companyID);
	
	/**
	 * Returns maildrop entry for given status field or <code>null</code> if no such entry exists.
	 *
	 * @param mailingID mailing ID
	 * @param companyID company ID
	 * @param status maildrop status field.
	 *
	 * @return maildrop entry or <code>null</code>
	 */
	MaildropEntry getEntryForStatus(final int mailingID, final int companyID, final char status);

	List<MaildropEntry> getMaildropStatusEntriesForMailing(int companyID, int mailingID);

	/**
	 * Update referenced maildrop entry and set/unset a boolean flag indicating that a special test recipients table
	 * must be used to select recipients for this delivery. Use {@link #setTestRecipients(int, java.util.List)} to save
	 * customer ids (or cleanup previously saved) into special test recipients table mentioned above.
	 *
	 * @param companyId an identifier of a company that the referenced maildrop entry belongs to.
	 * @param maildropStatusId an identifier of a maildrop entry to be updated.
	 * @param isSelected whether a flag should be set ({@code true}) or unset ({@code false}).
	 * @return whether a referenced maildrop entry exists, has proper type and successfully updated.
	 */
	boolean setSelectedTestRecipients(int companyId, int maildropStatusId, boolean isSelected);

	/**
	 * Save given customer ids into special test recipients table and associate them with maildrop entry referenced
	 * by {@code maildropStatusId}. Make sure to validate ids before use.
	 *
	 * @param maildropStatusId an identifier of a given maildrop entry.
	 * @param customerIds a list of recipients who are supposed to receive a test mailing.
	 */
    void setTestRecipients(int maildropStatusId, List<Integer> customerIds);
    
	void batchInsertMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries);

	void batchUpdateMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries);

	void removeOutdatedFindLastNewsletterEntries(final int companyID, final ZonedDateTime olderThan);

	void writeMailingSendStatisticsEntry(int companyID, int mailingID, MaildropStatus maildropStatus, MediaTypes mediaType, MailType mailType, int amount, int dataSize, Date sendDate, String mailerHostname);

	List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded);

	Map<Integer, List<Integer>> cleanupFailedTestDeliveries();

	void cleanupOldEntriesByMailingID(int mailingID, int maximumAgeInDays);

    void deleteByMailingId(int mailingID);

	int insertMaildropEntry(MaildropEntry entry);

	void updateMaildropEntry(MaildropEntry entry);
}
