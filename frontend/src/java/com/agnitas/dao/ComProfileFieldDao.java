/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.LightProfileField;
import org.agnitas.dao.ProfileFieldDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.beans.ProfileField;

public interface ComProfileFieldDao extends ProfileFieldDao {
	int MAX_SORT_INDEX = 1000;

	boolean mayAdd(@VelocityCheck int companyID);

	boolean isNearLimit(int companyID);

	int getMaximumFieldCount(int companyID) throws Exception;

	int getCurrentFieldCount(int companyID) throws Exception;

    @Override
	ProfileField getProfileField(@VelocityCheck int companyID, String column) throws Exception;

    ProfileField getProfileField(@VelocityCheck int companyID, String column, int adminId) throws Exception;

    List<ProfileField> getComProfileFields(@VelocityCheck int companyID) throws Exception;

    List<ProfileField> getComProfileFields(@VelocityCheck int companyID, int adminID) throws Exception;

    List<ProfileField> getComProfileFields(@VelocityCheck int companyID, int adminID, boolean customSorting) throws Exception;

	List<LightProfileField> getLightProfileFields(@VelocityCheck int companyId) throws Exception;

	CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(@VelocityCheck int companyID) throws Exception;

	CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(@VelocityCheck int companyID, boolean determineDefaultValues) throws Exception;

    CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(@VelocityCheck int companyID, int adminID) throws Exception;

    @Override
	ProfileField getProfileFieldByShortname(@VelocityCheck int companyID, String shortName) throws Exception;

    boolean existWithExactShortname(int companyID, String shortName);

    ProfileField getProfileFieldByShortname(@VelocityCheck int companyID, String shortName, int adminID) throws Exception;

	List<ProfileField> getProfileFieldsWithIndividualSortOrder(@VelocityCheck int companyID, int adminID) throws Exception;

	List<ProfileField> getProfileFieldsWithInterest(@VelocityCheck int companyID, int adminID) throws Exception;

	List<ProfileField> getHistorizedProfileFields(@VelocityCheck int companyID) throws Exception;

	boolean checkAllowedDefaultValue(@VelocityCheck int companyID, String columnName, String fieldDefault) throws Exception;

	/**
	 * Returns the user-selected profile fields in history.
	 *
	 * @param companyID company ID
	 *
	 * @return list of user-selected profile fields in history.
	 */
	Set<String> listUserSelectedProfileFieldColumnsWithHistoryFlag(@VelocityCheck int companyID);

	boolean deleteByCompany(@VelocityCheck int companyID);

	/**
	 * Check whether or not a {@code column} exists (either a default (built-in) column or a custom profile field).
	 *
	 * @param column a profile field name (column name).
	 * @param companyId a company that is supposed to own the referenced profile field.
	 * @return whether ({@code true}) or not ({@code false}) a column exists.
	 */
	boolean exists(String column, @VelocityCheck int companyId);

	/**
	 * Check whether or not a {@code column} is either a default (built-in) column or a custom profile field having
	 * historization option enabled.
	 *
	 * @param column a profile field name (column name).
	 * @param companyId a company that is supposed to own the referenced profile field.
	 * @return whether ({@code true}) or not ({@code false}) a column changes can be tracked.
	 */
	boolean isTrackableColumn(String column, @VelocityCheck int companyId);

	int countCustomerEntries(@VelocityCheck final int companyID);

	boolean checkProfileFieldExists(@VelocityCheck final int companyID, final String fieldNameOnDatabase) throws Exception;

	int getMaximumCompanySpecificFieldCount(@VelocityCheck int companyID) throws Exception;

	int getCurrentCompanySpecificFieldCount(@VelocityCheck int companyID) throws Exception;

	DbColumnType getColumnType(@VelocityCheck int companyId, String columnName);

	boolean isColumnIndexed(@VelocityCheck int companyId, String column);

	boolean isReservedKeyWord(String fieldname);
}
