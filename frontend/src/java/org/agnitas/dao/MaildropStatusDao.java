/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.MaildropEntry;

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
    boolean delete(@VelocityCheck int companyId, int id);

    /**
     * Deletes unsent world maildrop entries for given mailing id.
     *
     * @param mailingID
     *          The id of mailing.
     * @return count of deleted entities.
     */
	int deleteUnsentWorldMailingEntries(int mailingID);

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

	/**
	 * Saves given maildrop entry. If entry ID is 0 or no entry with given ID exists, a new entry is created.
	 * Otherwise, existing entry is updated.
	 *  
	 * @param entry entry to save
	 * 
	 * @return ID of maildrop entry
	 */
	int saveMaildropEntry(final MaildropEntry entry);

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
	boolean setSelectedTestRecipients(@VelocityCheck int companyId, int maildropStatusId, boolean isSelected);

	/**
	 * Save given customer ids into special test recipients table and associate them with maildrop entry referenced
	 * by {@code maildropStatusId}. Make sure to validate ids before use.
	 *
	 * @param maildropStatusId an identifier of a given maildrop entry.
	 * @param customerIds a list of recipients who are supposed to receive a test mailing.
	 */
    void setTestRecipients(int maildropStatusId, List<Integer> customerIds);
    
	void saveMaildropEntries(int companyId, int mailingId, Set<MaildropEntry> maildropStatusList);
}
