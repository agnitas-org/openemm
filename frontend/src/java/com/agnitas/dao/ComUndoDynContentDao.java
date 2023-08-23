/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;

import com.agnitas.beans.ComUndoDynContent;

/**
 * Interface for accessing undo steps for dynamic content of mailings.
 */
public interface ComUndoDynContentDao {
	/**
	 * Save undo data for the dynamic content of a given mailing.
	 * 
	 * @param mailingId mailing ID, undo steps for dynamic content is saved for
	 * @param undoId ID of the undo step
	 */
	void saveUndoData(int mailingId, int undoId);
	
	/**
	 * Get a specific undo step for the dynamic contents of a given mailing.
	 * 
	 * @param mailingId mailing ID
	 * @param undoId ID of undo step
	 * 
	 * @return undo step for dynamic content
	 */
	List<ComUndoDynContent> getAllUndoDataForMailing(int mailingId, int undoId);
	
	void deleteUndoData(int undoId);
	
	void deleteUndoDataForMailing(int mailingID);
	
	void deleteUndoDataOverLimit(int mailingId, int undoId);
	
	/**
	 * Deletes content existing in the current mailing but not in the given undo step.
	 * 
	 * @param mailingId mailing ID
	 * @param undoId ID of undo step
	 */
	void deleteAddedDynContent(int mailingId, int undoId);

	boolean deleteByCompany(int companyId);

	void deleteUndoData(List<Integer> undoIds);
}
