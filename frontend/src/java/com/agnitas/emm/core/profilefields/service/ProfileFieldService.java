/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.LightProfileField;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public interface ProfileFieldService {

    String translateDatabaseNameToVisibleName(final int companyID, final String databaseName) throws ProfileFieldException;

    String translateVisibleNameToDatabaseName(final int companyID, final String visibleName) throws ProfileFieldException;

    List<ProfileField> getProfileFieldsWithInterest(Admin admin);

    boolean isAddingNearLimit(int companyId);

    PaginatedListImpl<ProfileField> getPaginatedFieldsList(int companyId, String sortColumn, String order, int page, int rowsCount) throws Exception;

    List<ProfileField> getSortedColumnInfo(int companyId);

    List<String> getAllExceptSpecifiedNames(List<String> excludedFields, int companyId);

    List<ProfileField> getFieldWithIndividualSortOrder(int companyId, int adminId);

    List<LightProfileField> getLightProfileFields(int companyId);

    int getCurrentSpecificFieldCount(int companyId);

    int getMaximumCompanySpecificFieldCount(int companyId);

    boolean exists(int companyId, String fieldName);

    ProfileField getProfileField(int companyId, String fieldName);

    ProfileField getProfileField(int companyId, String fieldName, int adminId);

    String getTrackingDependentWorkflows(int companyId, String fieldName);

    Set<String> getSelectedFieldsWithHistoryFlag(int companyId);

    void createMandatoryFieldsIfNotExist(Admin admin);

    boolean createNewField(ProfileField field, Admin admin);

    boolean updateField(ProfileField field, Admin admin);

    void removeProfileField(int companyId, String fieldName);

    UserAction getCreateFieldLog(String shortName);

    UserAction getDeleteFieldLog(String fieldName);

    UserAction getOpenEmmChangeLog(ProfileField field, ProfileFieldForm form);

    UserAction getEmmChangeLog(ProfileField field, ProfileFieldForm form);

    List<Dependent<ProfileFieldDependentType>> getDependents(int companyId, String fieldName);

    List<ProfileField> getProfileFields(int companyId) throws Exception;

    List<ProfileField> getVisibleProfileFields(int companyId);

    List<ProfileField> getVisibleProfileFields(int adminId, int companyId);

	boolean isReservedKeyWord(String fieldname);

	int getMaximumNumberOfCompanySpecificProfileFields() throws Exception;

    ProfileField getProfileFieldByShortname(int companyID, String shortname);

    ProfileField getProfileFieldByShortname(int companyID, String shortname, int adminId);

	List<ProfileField> getProfileFields(int companyId, int adminId) throws Exception;

	boolean isWithinGracefulLimit(int companyId);
}
