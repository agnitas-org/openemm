/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.dao;

import java.util.List;

import com.agnitas.beans.ExportPredef;

import com.agnitas.beans.Admin;

public interface ExportPredefDao {

    /**
     * Loads an ExportPredef identified by definition id and company id.
     *
     * @param id
     *          The id of the definition that should be loaded.
     * @param companyID
     *          The companyID for the definition.
     * @return The ExportPredef for given definition id and company id
     *  null if company id == 0
     *  new instance of ExportPredef if id == 0.
     */

    ExportPredef get(int id, int companyID);

    String findName(int id, int companyId);
    
    /**
     * Updates or create export definition.
     *
     * @param src
     *          The ExportPredef to be saved.
     * @return ID of updated definition
     * 0 if saving failed.
     */
    int save(ExportPredef src);

    /**
     * Deletes an export definition.
     *
     * @param src
     *          The export definition to delete.
     * @return true on success.
     */
    boolean delete(ExportPredef src);

    /**
     * Deletes an export definition by ID and company id
     *
     * @param id
     *          The ID of export definition to delete.
     * @param companyID
     *          The companyID of the definition.
     * @return true on success.
     */
    boolean delete(int id, int companyID);
    
    /**
     * Delete all export definitions for given companyID
     * 
     * @return true on success
     */
    boolean deleteAllByCompanyID(int companyID);

    /**
     * Loads all export definitions of certain company.
     *
     * @param companyId id of the company for export definitions.
     * @return  List of ExportPredef or empty list.
     */
    List<ExportPredef> getAllExports(int companyId);

    List<Integer> findTargetDependentExportProfiles(int targetGroupId, int companyId);

    /**
     * Loads all export definitions of certain company (except the entries using disabled mailing lists only).
     *
     * @param admin emm user to check ALML.
     * @return  List of ExportPredef or empty list.
     */
    List<ExportPredef> getAllExports(Admin admin);

    /**
     * Loads ids of all export definitions of certain company (except the entries using disabled mailing lists only).
     *
     * @param admin emm user to check ALML.
     * @return  List of ids or empty list.
     */
	List<Integer> getAllExportIds(Admin admin);

	List<Integer> getExportsContainingProfileField(int companyID, String profileFieldName);
}
