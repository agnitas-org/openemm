/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;

import com.agnitas.beans.Title;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;

public interface TitleDao {

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

    PaginatedList<Title> overview(SalutationOverviewFilter filter);

    void save(Title title);
    
	boolean deleteTitlesByCompanyID(int companyID);

	List<Title> getTitles(int companyID, boolean includeGenders);

}
