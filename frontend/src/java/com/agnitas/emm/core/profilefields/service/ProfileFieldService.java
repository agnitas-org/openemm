/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import org.agnitas.beans.LightProfileField;

import java.util.List;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public interface ProfileFieldService {

    String translateDatabaseNameToVisibleName(final int companyID, final String databaseName) throws ProfileFieldException;

    List<ProfileField> getProfileFieldsWithInterest(Admin admin);


    List<ProfileField> getSortedColumnInfo(int companyId);

    List<String> getAllExceptSpecifiedNames(List<String> excludedFields, int companyId);

    List<LightProfileField> getLightProfileFields(int companyId);

    int getCurrentSpecificFieldCount(int companyId);

    boolean exists(int companyId, String fieldName);

    void createMandatoryFieldsIfNotExist(Admin admin);

    List<ProfileField> getProfileFields(int companyId) throws Exception;

    List<ProfileField> getVisibleProfileFields(int adminId, int companyId);

	List<ProfileField> getProfileFields(int companyId, int adminId) throws Exception;

    ProfileField getProfileFieldByShortname(int companyID, String shortname, int adminId);

}
