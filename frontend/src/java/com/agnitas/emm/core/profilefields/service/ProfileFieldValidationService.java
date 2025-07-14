/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.ImportProfile;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.DbColumnType.SimpleDataType;

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

    boolean mayAddNewColumn(int companyId);

    boolean hasNotAllowedNumberOfEntries(int companyId);

    SimpleServiceResult isValidToDelete(String column, Admin admin);

    ServiceResult<Object> validateNewProfileFieldValue(Admin admin, String fieldName, SimpleDataType newSimpleDataType, String newValue, boolean clearThisField);

	boolean isInvalidFloatField(String fieldType, String fieldDefault, Locale locale);

	List<ImportProfile> getImportsContainingProfileField(int companyId, String fieldName);

	List<ExportPredef> getExportsContainingProfileField(int companyId, String fieldName);

    List<EmmAction> getDependentActions(String fieldName, int companyId);
}
