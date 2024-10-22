/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import com.agnitas.beans.Admin;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.beans.ExportPredef;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.DbColumnType.SimpleDataType;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public interface ProfileFieldValidationService {

    boolean isDbFieldNameContainsSpaces(String fieldName);

    boolean isValidDbFieldName(String fieldName);

    boolean isInvalidLengthForDbFieldName(String fieldName);

    boolean isValidShortname(int companyId, String shortName, String fieldName);

    boolean isShortnameInDB(int companyId, String shortName);

    boolean isInvalidIntegerField(String fieldType, String fieldDefault);

    boolean isAllowedDefaultValue(String fieldType, String defaultValue, SimpleDateFormat dateFormat);

    boolean isDefaultValueAllowedInDb(int companyId, String fieldName, String defaultValue);

    boolean isInvalidVarcharField(String fieldType, long fieldLength);

    boolean mayAddNewColumn(int companyId) throws Exception;

    boolean notContainsInDb(int companyId, String fieldName);

    boolean hasNotAllowedNumberOfEntries(int companyId);

    boolean hasTargetGroups(int companyId, String fieldName);

    SimpleServiceResult isValidToDelete(String column, Admin admin);

    boolean isStandardColumn(String fieldName);

    ServiceResult<Object> validateNewProfileFieldValue(Admin admin, String fieldName, SimpleDataType newSimpleDataType, String newValue, boolean clearThisField);

	boolean isInvalidFloatField(String fieldType, String fieldDefault, Locale locale);

	List<Integer> getImportsContainingProfileField(int companyId, String fieldName);

	List<Integer> getExportsContainingProfileField(int companyId, String fieldName);

	ImportProfile getImportProfile(int importProfileID);

	ExportPredef getExportProfile(int companyID, int exportProfileID);
}
