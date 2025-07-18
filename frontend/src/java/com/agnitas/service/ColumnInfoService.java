/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public interface ColumnInfoService {
	ProfileField getColumnInfo(int companyID, String column);
	
	List<ProfileField> getColumnInfos(int companyID);
	
	List<ProfileField> getColumnInfos(int companyID, int adminID);

	CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(int companyID);
	
	CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(int companyID, int adminID);
	
	ProfileField getColumnInfo(int companyID, String column, int adminID);
	
	List<ProfileField> getComColumnInfos(int companyID);

    List<ProfileField> getComColumnInfos(int companyID, int adminID);

    List<ProfileField> getComColumnInfos(int companyID, int adminID, boolean customSorting);

	List<ProfileField> getHistorizedComColumnInfos(int companyID);

	Map<Integer, ProfileFieldMode> getProfileFieldAdminPermissions(int companyID, String columnName) throws Exception;

	void storeProfileFieldAdminPermissions(int companyID, String column, Set<Integer> editableUsers, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) throws Exception;
}
