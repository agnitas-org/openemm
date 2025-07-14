/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;


import com.agnitas.beans.UndoMailingComponent;

/**
 * Interface to access undo steps of components of mailings.
 * Currently only the template components (<i>agnText</i> and <i>agnHtml</i>) is accessed.
 */
public interface UndoMailingComponentDao {
	/**
	 * Save undo data for the components of a given mailing.
	 * 
	 * @param mailingId mailing ID, undo steps for components is saved for
	 * @param undoId ID of the undo step
	 */
	void saveUndoData(int mailingId, int undoId);
	
	/**
	 * Get a specific undo step for the components of a given mailing.
	 * 
	 * @param mailingId mailing ID
	 * @param componentId component ID
	 * @param undoId ID of undo step
	 * @return undo step for component
	 */
	UndoMailingComponent getUndoData(int mailingId, int componentId, int undoId);
	
	/**
	 * Get undo data for all mailing components for a given undo step.
	 * 
	 * @param mailingId mailing ID
	 * @param undoId ID of undo step
	 * 
	 * @return List containing all mailing components of the given mailing for the given undo step
	 */
	List<UndoMailingComponent> getAllUndoDataForMailing(int mailingId, int undoId);
	
	void deleteUndoData(int undoId);
	
	void deleteUndoDataForMailing(int mailingID);
	
	void deleteUndoDataOverLimit(int mailingId, int undoId);

	boolean deleteByCompany(int companyId);

	void deleteUndoData(List<Integer> undoIds);
}
