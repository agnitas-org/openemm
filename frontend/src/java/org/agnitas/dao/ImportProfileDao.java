/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;

import java.util.List;


public interface ImportProfileDao {

    /**
     * Inserts new import profile to database.
     * Also creates corresponding column and gender mappings.
     *
     * @param item
     *          ImportProfile entry to insert.
     * @return ID of inserted import profile.
     * @throws Exception
     */
	int insertImportProfile(ImportProfile item) throws Exception;

    /**
     * Updates import profile and its column and gender mappings.
     *
     * @param item
     *          ImportProfile entry to update.
     * @throws Exception
     */
	void updateImportProfile(ImportProfile item) throws Exception;

    /**
     * Loads an import profile identified by ID.
     *
     * @param id
     *          The ID of import profile.
     * @return The ImportProfile or null on failure.
     */
	ImportProfile getImportProfileById(int id);

    /**
     * Loads an import profile identified by shortname.
     *
     * @param shortname
     *          The shortname of import profile.
     * @return The ImportProfile or null on failure.
     */
	ImportProfile getImportProfileByShortname(String shortname);

    /**
     * Loads list of import profiles identified by company id where deleted != 1.
     *
     * @param companyId
     *          The company id for import profiles.
     * @return The list of ImportProfiles or empty list.
     */
	List<ImportProfile> getImportProfilesByCompanyId( int companyId);
	
	 /**
     * Loads list of import profiles identified by company id.
     *
     * @param companyId
     *          The company id for import profiles.
     * @return The list of ImportProfiles or empty list.
     */
	List<ImportProfile> getAllImportProfilesByCompanyId( int companyId);

    /**
     * Deletes import profile by ID with column and gender mappings.
     *
     * @param id
     *          The ID of import profile to be deleted.
     */
	boolean deleteImportProfileById(int id);

	void insertColumnMappings(List<ColumnMapping> columnMappings);

	void updateColumnMappings(List<ColumnMapping> columnMappings);

	void deleteColumnMappings(List<Integer> ids);

	List<Integer> getSelectedMailingListIds(int id, int companyId);

	int findImportProfileIdByName(String name, int companyId);

	boolean isColumnWasImported(String columnName, int id);

	List<Integer> getImportsContainingProfileField(int companyID, String profileFieldName);
}
