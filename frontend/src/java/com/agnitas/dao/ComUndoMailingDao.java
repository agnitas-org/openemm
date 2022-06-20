/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;

import com.agnitas.beans.ComUndoMailing;

/**
 * Interface to access undo steps of mailings.
 */
public interface ComUndoMailingDao {
	/**
	 * Save undo data for the given mailing.
	 * 
	 * @param mailingId mailing ID to save undo steps for
	 * @param undoId ID of the undo step
	 * @param undoCreationDate timestamp of creating undo step
	 * @param undoAdminId initiator of undo step
	 */
	void saveUndoData(int mailingId, int undoId, Date undoCreationDate, int undoAdminId);
	
	/**
	 * Get the last undo step for the given mailing.
	 * 
	 * @param mailingId mailing ID
	 * 
	 * @return last undo step 
	 */
	ComUndoMailing getLastUndoData(int mailingId);

	/**
	 * Get the given undo step for the given mailing.
	 * 
	 * @param mailingId mailing ID
	 * @param undoId undo step ID
	 * 
	 * @return last undo step 
	 */
	ComUndoMailing getUndoData(int mailingId, int undoId);
	
	void deleteUndoData(int undoId);

	int getYoungestOutdatedUndoId(int retentionTime);

	int getUndoIdOverLimit(int mailingId, int undoLimit);

	void deleteUndoDataOverLimit(int mailingId, int undoId);
	
	void deleteOutdatedUndoData(int lastUndoId);
	
	void deleteUndoDataForMailing(int mailingID);

	void deleteUndoForSentMailings();
}
