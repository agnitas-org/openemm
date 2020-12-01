/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;

import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.service.ComColumnInfoService;

public class ComColumnInfoServiceImpl implements ComColumnInfoService {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComColumnInfoServiceImpl.class);
	
	private ComProfileFieldDao profileFieldDao;
	
	@Override
    public ProfileField getColumnInfo(@VelocityCheck int companyID, String column) throws Exception {
		return profileFieldDao.getProfileField(companyID, column);
    }
	
	@Override
    public ProfileField getColumnInfo(@VelocityCheck int companyID, String column, int adminID) throws Exception {
		return profileFieldDao.getProfileField(companyID, column, adminID);
    }
	
	@Override
    public List<ProfileField> getColumnInfos(@VelocityCheck int companyID) throws Exception {
		return profileFieldDao.getProfileFields(companyID);
    }
	
	@Override
    public List<ProfileField> getColumnInfos(@VelocityCheck int companyID, int adminID) throws Exception {
		return profileFieldDao.getProfileFields(companyID, adminID);
    }
	
	@Override
    public List<ProfileField> getComColumnInfos(@VelocityCheck int companyID) throws Exception {
		return profileFieldDao.getComProfileFields(companyID);
    }
	
	@Override
    public List<ProfileField> getComColumnInfos(@VelocityCheck int companyID, int adminID) throws Exception {
		return getComColumnInfos(companyID, adminID, false);
    }

    @Override
    public List<ProfileField> getComColumnInfos(@VelocityCheck int companyID, int adminID, boolean customSorting) throws Exception {
		return profileFieldDao.getComProfileFields(companyID, adminID, customSorting);
    }

    @Override
	public List<ProfileField> getHistorizedComColumnInfos(@VelocityCheck int companyID) throws Exception {
		return profileFieldDao.getHistorizedProfileFields(companyID);
	}
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(@VelocityCheck int companyID) throws Exception {
		CaseInsensitiveMap<String, ProfileField> comProfileFieldMap = profileFieldDao.getComProfileFieldsMap(companyID);
		CaseInsensitiveMap<String, ProfileField> profileFieldMap = new CaseInsensitiveMap<>();
		for (ProfileField comProfileField : comProfileFieldMap.values()) {
			profileFieldMap.put(comProfileField.getColumn(), comProfileField);
		}
		return profileFieldMap;
	}
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getColumnInfoMap(@VelocityCheck int companyID, int adminID) throws Exception {
		CaseInsensitiveMap<String, ProfileField> comProfileFieldMap = profileFieldDao.getComProfileFieldsMap(companyID, adminID);
		CaseInsensitiveMap<String, ProfileField> profileFieldMap = new CaseInsensitiveMap<>();
		for (ProfileField comProfileField : comProfileFieldMap.values()) {
			profileFieldMap.put(comProfileField.getColumn(), comProfileField);
		}
		return profileFieldMap;
	}
	
	@Override
    public CaseInsensitiveMap<String, ProfileField> getComColumnInfoMap(@VelocityCheck int companyID) throws Exception {
		return profileFieldDao.getComProfileFieldsMap(companyID);
    }
	
	@Override
	public CaseInsensitiveMap<String, ProfileField> getComColumnInfoMap(@VelocityCheck int companyID, int adminID) throws Exception {
		return profileFieldDao.getComProfileFieldsMap(companyID, adminID);
	}

	public void setProfileFieldDao(ComProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}
}
