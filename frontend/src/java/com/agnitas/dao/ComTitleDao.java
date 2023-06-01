/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;

import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;

public interface ComTitleDao {
	/**
	 * Load a title from the database.
	 *
	 * @param titleID the unique id of the title to load.
	 * @param companyID the id of the company for the given title.
	 * @return the loaded title.
	 */
	Title getTitle(int titleID, int companyID);

	/**
	 * Delete a title in the database.
	 *
	 * @param titleID the unique id of the title to delete.
	 * @param companyID the id of the company for the given title.
	 * @return true on success.
	 */
	boolean delete(int titleID, int companyID);

    /**
     * Loads all titles of certain company and creates paginated list according to given criteria for sorting and pagination
     *
     * @param companyID
     *                The id of the company for titles
     * @param sort
     *                The name of the column to be sorted
     * @param direction
     *                The sort direction , 0 (for ascending) or 1 (for descending)
     * @param page
     *                The number of the page
     * @param rownums
     *                The number of rows to be shown on page
     * @return  PaginatedList of Title or empty list.
     */
	PaginatedListImpl<SalutationEntry> getSalutationList(int companyID, String sort, String direction, int page, int rownums);
    
    void save(Title title) throws Exception;
    
	boolean deleteTitlesByCompanyID(int companyID);

	List<Title> getTitles(int companyID);
}
