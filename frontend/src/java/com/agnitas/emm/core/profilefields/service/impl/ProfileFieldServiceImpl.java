/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.impl.ProfileFieldImpl;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.post.MandatoryField;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.beans.LightProfileField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public final class ProfileFieldServiceImpl implements ProfileFieldService {

    private static final Logger logger = LogManager.getLogger(ProfileFieldServiceImpl.class);

    private ProfileFieldDao profileFieldDao;
    private ColumnInfoService columnInfoService;

    public void setColumnInfoService(ColumnInfoService columnInfoService) {
        this.columnInfoService = columnInfoService;
    }

    public void setProfileFieldDao(ProfileFieldDao dao) {
        this.profileFieldDao = Objects.requireNonNull(dao, "Profile field DAO cannot be null");
    }

    @Override
    public String translateDatabaseNameToVisibleName(int companyID, String databaseName) {
        return this.profileFieldDao.getProfileField(companyID, databaseName).getShortname();
    }

	@Override
    public List<ProfileField> getProfileFieldsWithInterest(Admin admin) {
        return profileFieldDao.getProfileFieldsWithInterest(admin.getCompanyID(), admin.getAdminID());
    }

    @Override
    public List<ProfileField> getSortedColumnInfo(int companyId) {
        List<ProfileField> columnInfoList = columnInfoService.getComColumnInfos(companyId);
        columnInfoList.sort(this::compareColumn);

        return columnInfoList;
    }

    @Override
    public List<String> getAllExceptSpecifiedNames(List<String> excludedFields, int companyId) {
        // Get all available customer fields
        List<String> availableCustomerFields = getSortedColumnInfo(companyId)
                .stream()
                .map(LightProfileField::getColumn)
                .collect(Collectors.toList());

        availableCustomerFields.removeAll(excludedFields);

        return availableCustomerFields;
    }

    @Override
    public List<LightProfileField> getLightProfileFields(int companyId) {
        return profileFieldDao.getLightProfileFields(companyId);
    }

    @Override
    public boolean exists(int companyId, String fieldName) {
        return profileFieldDao.exists(fieldName, companyId);
    }

    @Override
    public void createMandatoryFieldsIfNotExist(Admin admin) {
        int companyID = admin.getCompanyID();

        if (!exists(companyID, MandatoryField.STRASSE.getName())) {
            createNewVarcharField(MandatoryField.STRASSE.getName(), 200, admin);
        }
        if (!exists(companyID, MandatoryField.PLZ.getName())) {
            createNewVarcharField(MandatoryField.PLZ.getName(), 5, admin);
        }
        if (!exists(companyID, MandatoryField.ORT.getName())) {
            createNewVarcharField(MandatoryField.ORT.getName(), 100, admin);
        }
    }

    private boolean createNewVarcharField(String column, int length, Admin admin) {
        ProfileField profileField = new ProfileFieldImpl();

        profileField.setCompanyID(admin.getCompanyID());
        profileField.setColumn(column);
        profileField.setDataType("VARCHAR");
        profileField.setDataTypeLength(length);

       return createNewField(profileField, admin);
    }

    private boolean createNewField(ProfileField field, Admin admin) {
        try {
            return profileFieldDao.saveProfileField(field, admin);
        } catch (Exception e) {
            logger.error("Something went wrong when tried to save new field", e);

            return false;
        }
    }

    private int compareColumn(ProfileField field1, ProfileField field2) {
        if (field1.isHiddenField() == field2.isHiddenField()) {
            return 0;
        }
        return field1.isHiddenField() ? -1 : 1;
    }

    @Override
    public List<ProfileField> getProfileFields(int companyId) {
        return profileFieldDao.getComProfileFields(companyId);
    }

    @Override
    public List<ProfileField> getProfileFields(int companyId, int adminId) {
        return profileFieldDao.getComProfileFields(companyId, adminId);
    }

    @Override
    public List<ProfileField> getVisibleProfileFields(int adminId, int companyId) {
        List<ProfileField> profileFields = getProfileFields(companyId, adminId);
        return filterVisibleFields(profileFields);
    }

    private List<ProfileField> filterVisibleFields(List<ProfileField> fields) {
        return fields
                .stream()
                .filter(pf -> !ProfileFieldMode.NotVisible.equals(pf.getModeEdit()))
                .collect(Collectors.toList());
    }

    @Override
    public ProfileField getProfileFieldByShortname(int companyID, String shortname, int adminId) {
        return profileFieldDao.getProfileFieldByShortname(companyID, shortname, adminId);
    }
}
