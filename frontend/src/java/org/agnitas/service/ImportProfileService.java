/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import com.agnitas.beans.Admin;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.importvalues.ImportMode;

import java.util.List;
import java.util.Set;

public interface ImportProfileService {

    ColumnMapping findColumnMappingByDbColumn(String column, List<ColumnMapping> mappings);

    void saveColumnsMappings(List<ColumnMapping> columnMappings, int profileId, Admin admin);

    void saveImportProfileWithoutColumnMappings(ImportProfile profile, Admin admin);

    ImportProfile getImportProfileById(int id);

    boolean isDuplicatedName(String name, int id, int companyId);

    boolean isColumnWasImported(String columnName, int id);

    void deleteImportProfileById(int id);

    List<ImportProfile> getAvailableImportProfiles(Admin admin);

    List<Integer> getSelectedMailingListIds(int id, int companyId);

	boolean isKeyColumnsIndexed(ImportProfile profile);

    boolean isCheckForDuplicatesAllowed(Admin admin);

    boolean isUpdateDuplicatesChangeAllowed(Admin admin);

    boolean isPreprocessingAllowed(Admin admin);

    boolean isAllMailinglistsAllowed(Admin admin);

    boolean isCustomerIdImportAllowed(Admin admin);

    boolean isAllowedToShowMailinglists(Admin admin);

    boolean isImportModeAllowed(int mode, Admin admin);

    boolean isEcryptedImportAllowed(Admin admin);

    Set<ImportMode> getAvailableImportModes(Admin admin);

    boolean isManageAllowed(ImportProfile profile, Admin admin);

}
