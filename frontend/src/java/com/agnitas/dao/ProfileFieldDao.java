/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.LightProfileField;
import com.agnitas.util.DbColumnType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public interface ProfileFieldDao {
	int MAX_SORT_INDEX = 1000;

    ProfileField getProfileField(int companyID, String column, int adminId);

    List<ProfileField> getComProfileFields(int companyID);

    List<ProfileField> getComProfileFields(int companyID, int adminID);

    List<ProfileField> getComProfileFields(int companyID, int adminID, boolean customSorting);

	List<LightProfileField> getLightProfileFields(int companyId);

	CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(int companyID);

	@Deprecated	// Due to cachine, default values must always be determined. See EMM-9446 for details.
	CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(int companyID, @Deprecated boolean determineDefaultValues);

    CaseInsensitiveMap<String, ProfileField> getComProfileFieldsMap(int companyID, int adminID);

    boolean existWithExactShortname(int companyID, String shortName);

    ProfileField getProfileFieldByShortname(int companyID, String shortName, int adminID);

	List<ProfileField> getProfileFieldsWithInterest(int companyID, int adminID);

	List<ProfileField> getHistorizedProfileFields(int companyID);

	boolean checkAllowedDefaultValue(int companyID, String columnName, String fieldDefault);

	/**
	 * Check whether or not a {@code column} exists (either a default (built-in) column or a custom profile field).
	 *
	 * @param column a profile field name (column name).
	 * @param companyId a company that is supposed to own the referenced profile field.
	 * @return whether ({@code true}) or not ({@code false}) a column exists.
	 */
	boolean exists(String column, int companyId);

	/**
	 * Check whether or not a {@code column} is either a default (built-in) column or a custom profile field having
	 * historization option enabled.
	 *
	 * @param column a profile field name (column name).
	 * @param companyId a company that is supposed to own the referenced profile field.
	 * @return whether ({@code true}) or not ({@code false}) a column changes can be tracked.
	 */
	boolean isTrackableColumn(String column, int companyId);

	int countCustomerEntries(final int companyID);

	boolean checkProfileFieldExists(final int companyID, final String fieldNameOnDatabase);

	int getMaximumCompanySpecificFieldCount(int companyID);

	DbColumnType getColumnType(int companyId, String columnName);

	boolean isColumnIndexed(int companyId, String column);

	void clearProfileStructureCache(int companyID);
	
	boolean isOracleDB();
	
    /**
     * Loads profile field by company id and column name.
     *
     * @param companyID The companyID for the profile field.
     * @param column    The column name for profile field.
     * @return The ProfileField or null on failure or if companyID is 0.
     */
    ProfileField getProfileField(int companyID, String column);

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    List<ProfileField> getProfileFields(int companyID);

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    List<ProfileField> getProfileFields(int companyID, int adminID);

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    CaseInsensitiveMap<String, ProfileField> getProfileFieldsMap(int companyID);

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    CaseInsensitiveMap<String, ProfileField> getProfileFieldsMap(int companyID, int adminID);

    /**
     * Saves or updates the profile field.
     *
     * @param field The profile field to save.
     */
    boolean saveProfileField(ProfileField field, Admin admin) throws Exception;

    /**
     * Loads profile field by company id and short name.
     *
     * @param companyID The companyID for the profile field.
     * @param shortName The shortname for the profile field.
     * @return The ProfileField or null on failure or if companyID is 0.
     */
    ProfileField getProfileFieldByShortname(int companyID, String shortName);

    /**
     * Creates a new custom column in customer_tbl for given company_id.
     *
     * @param companyID    The company id for new column.
     * @param fieldname    Column name in database.
     * @param fieldType    Column type in database.
     * @param length       Column size. 0 for numeric fields means default length of 32 bit.
     * @param fieldDefault Default column value.
     * @param notNull      Column NOT NULL constraint.
     * @return true on success.
     */
    boolean addColumnToDbTable(int companyID, String fieldname, String fieldType, long length, String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean notNull) throws Exception;

    /**
     * Changes a custom column in customer_tbl for given company_id to a new type and/or default value.
     */
    boolean alterColumnTypeInDbTable(int companyID, String fieldname, String fieldType, long length, String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean notNull) throws Exception;

	Map<Integer, ProfileFieldMode> getProfileFieldAdminPermissions(int companyID, String columnName) throws Exception;

	void storeProfileFieldAdminPermissions(int companyID, String columnName, Set<Integer> editableUsers, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) throws Exception;

	Set<String> getCustomerColumns(int companyId);
}
