/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import java.util.List;
import java.util.Set;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;

public interface ProfileFieldService {
    boolean checkDatabaseNameExists(@VelocityCheck final int companyID, final String fieldNameOnDatabase) throws ProfileFieldException;

    String translateDatabaseNameToVisibleName(@VelocityCheck final int companyID, final String databaseName) throws ProfileFieldException;
    String translateVisibleNameToDatabaseName(final int companyID, final String visibleName) throws ProfileFieldException;

    List<ProfileField> getProfileFieldsWithInterest(ComAdmin admin);

    boolean isAddingNearLimit(@VelocityCheck int companyId);

    List<ProfileField> getSortedColumnInfo(@VelocityCheck int companyId);

    List<ProfileField> getFieldWithIndividualSortOrder(@VelocityCheck int companyId, int adminId);

    int getCurrentSpecificFieldCount(@VelocityCheck int companyId);

    int getMaximumCompanySpecificFieldCount(@VelocityCheck int companyId);

    boolean exists(@VelocityCheck int companyId, String fieldName);

    ProfileField getProfileField(@VelocityCheck int companyId, String fieldName);

    List<String> getDependentWorkflows(@VelocityCheck int companyId, String fieldName);

    String getTrackingDependentWorkflows(@VelocityCheck int companyId, String fieldName);

    Set<String> getSelectedFieldsWithHistoryFlag(@VelocityCheck int companyId);

    boolean createNewField(ProfileField field, ComAdmin admin);

    boolean updateField(ProfileField field, ComAdmin admin);

    void removeProfileField(@VelocityCheck int companyId, String fieldName);

    UserAction getCreateFieldLog(String shortName);

    UserAction getDeleteFieldLog(String fieldName);

    UserAction getOpenEmmChangeLog(ProfileField field, ProfileFieldForm form);

    UserAction getEmmChangeLog(ProfileField field, ProfileFieldForm form);

    List<Dependent<ProfileFieldDependentType>> getDependents(@VelocityCheck int companyId, String fieldName);
    
    List<ProfileField> listVisibleProfileFields(final int companyID) throws Exception;

	boolean isReservedKeyWord(String fieldname);

	int getMaximumNumberOfCompanySpecificProfileFields() throws Exception;

	ProfileField getProfileFieldByShortname(int companyID, String shortname) throws Exception;
}
