/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.dao;

import java.util.List;

import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;


public interface ImportProfileDao {

    /**
     * Inserts new import profile to database.
     * Also creates corresponding column and gender mappings.
     *
     * @param item
     *          ImportProfile entry to insert.
     * @return ID of inserted import profile.
     */
	int insertImportProfile(ImportProfile item);

    /**
     * Updates import profile and its column and gender mappings.
     *
     * @param item
     *          ImportProfile entry to update.
     */
	void updateImportProfile(ImportProfile item);

    /**
     * Loads an import profile identified by ID.
     *
     * @param id
     *          The ID of import profile.
     * @return The ImportProfile or null on failure.
     */
	ImportProfile getImportProfileById(int id);

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

	List<ImportProfile> findAllByEmailPart(String email, int companyID);

	List<ImportProfile> findAllByEmailPart(String email);

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

    void updateEmails(String emailForError, String emailForReport, int id);

}
