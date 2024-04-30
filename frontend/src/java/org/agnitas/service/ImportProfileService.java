/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;

public interface ImportProfileService {

    void saveImportProfile(ImportProfile profile);

    ColumnMapping findColumnMappingByDbColumn(String column, List<ColumnMapping> mappings);

    void saveColumnsMappings(List<ColumnMapping> columnMappings, int profileId, Admin admin);

    void saveImportProfileWithoutColumnMappings(ImportProfile profile);

    ImportProfile getImportProfileById(int id);

    boolean isDuplicatedName(String name, int id, int companyId);

    boolean isColumnWasImported(String columnName, int id);

    void deleteImportProfileById(int id);

    List<ImportProfile> getImportProfilesByCompanyId(int companyId);

    List<Integer> getSelectedMailingListIds(int id, int companyId);

	Map<String, Integer> getImportProfileGenderMapping(int id);

	void saveImportProfileGenderMapping(int id, Map<String, Integer> genderMapping);

	boolean addImportProfileGenderMapping(int profileId, String addedGender, int addedGenderInt);

	boolean isKeyColumnsIndexed(ImportProfile profile);
}
