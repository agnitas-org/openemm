/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.service.ColumnInfoService;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public class ColumnInfoServiceImpl implements ColumnInfoService {
	private ProfileFieldDao profileFieldDao;

	public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}
	
	@Override
    public ProfileField getColumnInfo(int companyID, String column) throws Exception {
		return profileFieldDao.getProfileField(companyID, column);
    }
	
	@Override
    public ProfileField getColumnInfo(int companyID, String column, int adminID) throws Exception {
		return profileFieldDao.getProfileField(companyID, column, adminID);
    }
	
	@Override
    public List<ProfileField> getColumnInfos(int companyID) throws Exception {
		return profileFieldDao.getProfileFields(companyID);
    }
	
	@Override
    public List<ProfileField> getColumnInfos(int companyID, int adminID) throws Exception {
		return profileFieldDao.getProfileFields(companyID, adminID);
    }
	
	@Override
    public List<ProfileField> getComColumnInfos(int companyID) throws Exception {
		return profileFieldDao.getComProfileFields(companyID);
    }
	
	@Override
    public List<ProfileField> getComColumnInfos(int companyID, int adminID) throws Exception {
		return getComColumnInfos(companyID, adminID, false);
    }

    @Override
    public List<ProfileField> getComColumnInfos(int companyID, int adminID, boolean customSorting) throws Exception {
		return profileFieldDao.getComProfileFields(companyID, adminID, customSorting);
    }

    @Override
	public List<ProfileField> getHistorizedComColumnInfos(int companyID) throws Exception {
		return profileFieldDao.getHistorizedProfileFields(companyID);
	}
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(int companyID) throws Exception {
		CaseInsensitiveMap<String, ProfileField> comProfileFieldMap = profileFieldDao.getComProfileFieldsMap(companyID);
		CaseInsensitiveMap<String, ProfileField> profileFieldMap = new CaseInsensitiveMap<>();
		for (ProfileField comProfileField : comProfileFieldMap.values()) {
			profileFieldMap.put(comProfileField.getColumn(), comProfileField);
		}
		return profileFieldMap;
	}
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(int companyID, int adminID) throws Exception {
		CaseInsensitiveMap<String, ProfileField> comProfileFieldMap = profileFieldDao.getComProfileFieldsMap(companyID, adminID);
		CaseInsensitiveMap<String, ProfileField> profileFieldMap = new CaseInsensitiveMap<>();
		for (ProfileField comProfileField : comProfileFieldMap.values()) {
			profileFieldMap.put(comProfileField.getColumn(), comProfileField);
		}
		return profileFieldMap;
	}
	
	@Override
    public CaseInsensitiveMap<String, ProfileField> getComColumnInfoMap(int companyID) throws Exception {
		return profileFieldDao.getComProfileFieldsMap(companyID);
    }
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getComColumnInfoMap(int companyID, int adminID) throws Exception {
		return profileFieldDao.getComProfileFieldsMap(companyID, adminID);
	}

	@Override
	public Map<Integer, ProfileFieldMode> getProfileFieldAdminPermissions(int companyID, String columnName) throws Exception {
		return profileFieldDao.getProfileFieldAdminPermissions(companyID, columnName);
	}

	@Override
	public void storeProfileFieldAdminPermissions(int companyID, String columnName, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) {
		profileFieldDao.storeProfileFieldAdminPermissions(companyID, columnName, readOnlyUsers, notVisibleUsers);
	}
}
