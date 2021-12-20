/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import java.text.SimpleDateFormat;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.service.ServiceResult;

public interface ProfileFieldValidationService {
    boolean isValidDbFieldName(String fieldName);

    boolean isInvalidLengthForDbFieldName(String fieldName);

    boolean isValidShortname(@VelocityCheck int companyId, String shortName, String fieldName);

    boolean isShortnameInDB(@VelocityCheck int companyId, String shortName);

    boolean isInvalidIntegerField(String fieldType, String fieldDefault);

    boolean isAllowedDefaultValue(String fieldType, String defaultValue, SimpleDateFormat dateFormat);

    boolean isDefaultValueAllowedInDb(@VelocityCheck int companyId, String fieldName, String defaultValue);

    boolean isInvalidVarcharField(String fieldType, long fieldLength);

    boolean mayAddNewColumn(@VelocityCheck int companyId);

    boolean notContainsInDb(@VelocityCheck int companyId, String fieldName);

    boolean hasNotAllowedNumberOfEntries(@VelocityCheck int companyId);

    boolean hasTargetGroups(@VelocityCheck int companyId, String fieldName);

    boolean isStandardColumn(String fieldName);
    
    ServiceResult<Object> validateNewProfileFieldValue(ComAdmin admin, RecipientFieldDto field);

	boolean isInvalidFloatField(String fieldType, String fieldDefault);
}
