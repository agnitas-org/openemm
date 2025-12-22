/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.UndoMailing;

/**
 * Interface to access undo steps of mailings.
 */
public interface UndoMailingDao {

	/**
	 * Get the last undo step for the given mailing.
	 * 
	 * @param mailingId mailing ID
	 * 
	 * @return last undo step
	 */
	UndoMailing getLastUndoData(int mailingId);

	void deleteUndoData(int undoId);

	List<Integer> findUndoIdsToCleanup(final int retentionTime);

	int getUndoIdOverLimit(int mailingId, int undoLimit);

	void deleteUndoDataOverLimit(int mailingId, int undoId);
	
	void deleteUndoDataForMailing(int mailingID);

	List<Integer> findAllUndoIdsForMailings(List<Integer> mailings);

	void deleteUndoData(List<Integer> undoIds);

	int saveUndoData(int mailingId, Date date, int adminId);

}
