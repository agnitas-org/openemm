/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.sql.SQLException;

import org.agnitas.backend.DBase;
import org.agnitas.util.Log;

/**
 * Update all backend log informations
 */
public class BackendLogDAO {
	private long statusID;
	private long mailingID;
	private boolean isWorldMailing;

	public BackendLogDAO(DBase dbase, long forStatusID, long forMailingID, boolean nIsWorldMailing) throws SQLException {
		statusID = forStatusID;
		mailingID = forMailingID;
		isWorldMailing = nIsWorldMailing;
		try (DBase.With with = dbase.with ()) {
			dbase.update (with.cursor (),
				      "INSERT INTO mailing_backend_log_tbl " +
				      "            (status_id, mailing_id, current_mails, total_mails, timestamp, creation_date) " +
				      "VALUES " +
				      "            (:statusID, :mailingID, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
				      "statusID", statusID,
				      "mailingID", mailingID
			);
			dbase.logging (Log.VERBOSE, "backendLog", "Setup of mailing backend log done");
		} catch (SQLException e) {
			dbase.logging(Log.ERROR, "backendLog", "Failed to setup mailing backend log", e);
			throw e;
		}
	}

	/**
	 * Update backend log entry with current mail count
	 *
	 * @param dbase      a reference to the database instance
	 * @param mailCount  current mail count
	 * @param totalCount expected number of total mails
	 */
	public void update (DBase dbase, long mailCount, long totalCount) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			dbase.update (with.cursor (),
				      "UPDATE mailing_backend_log_tbl " + 
				      "SET current_mails = :currentMails, total_mails = :totalMails, timestamp = CURRENT_TIMESTAMP " + 
				      "WHERE status_id = :statusID",
				      "currentMails", mailCount,
				      "totalMails", totalCount,
				      "statusID", statusID);
			dbase.logging (Log.DEBUG, "backendLog", "Updated backend log to " + mailCount + " of " + totalCount);
		} catch (SQLException e) {
			dbase.logging(Log.ERROR, "backendLog", "Failed to update mailing backend log to " + mailCount + " of " + totalCount, e);
			throw e;
		}
	}

	/**
	 * Finalize the created number of entries to database
	 *
	 * @param totalCount the final number of mails written
	 */
	public void freeze (DBase dbase, long totalCount) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			dbase.update (with.cursor (),
				      "UPDATE mailing_backend_log_tbl " +
				      "SET current_mails = :totalMails, total_mails = :totalMails, timestamp = CURRENT_TIMESTAMP " +
				      "WHERE status_id = :statusID",
				      "totalMails", totalCount,
				      "statusID", statusID);
			dbase.logging (Log.VERBOSE, "backendLog", "Freeze backend log to " + totalCount);
			if (isWorldMailing) {
				dbase.update (with.cursor (),
					      "INSERT INTO world_mailing_backend_log_tbl " +
					      "            (mailing_id, current_mails, total_mails, timestamp, creation_date) " +
					      "VALUES " +
					      "            (:mailingID, :totalMails, :totalMails, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
					      "mailingID", mailingID,
					      "totalMails", totalCount);
				dbase.logging (Log.VERBOSE, "backendLog", "Freeze world backend log to " + totalCount);
			}
		} catch (SQLException e) {
			dbase.logging(Log.ERROR, "backendLog", "Failed to freeze mailing backend log to " + totalCount, e);
			throw e;
		}
	}
}	
