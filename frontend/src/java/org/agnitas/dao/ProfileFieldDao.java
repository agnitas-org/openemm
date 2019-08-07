/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;

import org.agnitas.beans.ProfileField;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.profilefields.ProfileFieldException;

public interface ProfileFieldDao {
    /**
     * Loads profile field by company id and column name.
     *
     * @param companyID The companyID for the profile field.
     * @param column    The column name for profile field.
     * @return The ProfileField or null on failure or if companyID is 0.
     * @throws Exception
     */
    ProfileField getProfileField(@VelocityCheck int companyID, String column) throws Exception;

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    List<ProfileField> getProfileFields(@VelocityCheck int companyID) throws Exception;

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    List<ProfileField> getProfileFields(@VelocityCheck int companyID, int adminID) throws Exception;

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    CaseInsensitiveMap<String, ProfileField> getProfileFieldsMap(@VelocityCheck int companyID) throws Exception;

    /**
     * Loads all profile fields of certain company.
     *
     * @param companyID The companyID for the profile fields.
     * @return List of ProfileFields or empty list.
     */
    CaseInsensitiveMap<String, ProfileField> getProfileFieldsMap(@VelocityCheck int companyID, int adminID) throws Exception;

    /**
     * Saves or updates the profile field.
     *
     * @param field The profile field to save.
     * @throws Exception
     */
    boolean saveProfileField(ProfileField field, ComAdmin admin) throws Exception;

    /**
     * Loads profile field by company id and short name.
     *
     * @param companyID The companyID for the profile field.
     * @param shortName The shortname for the profile field.
     * @return The ProfileField or null on failure or if companyID is 0.
     */
    ProfileField getProfileFieldByShortname(@VelocityCheck int companyID, String shortName) throws Exception;

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
     * @throws Exception
     */
    boolean addColumnToDbTable(@VelocityCheck int companyID, String fieldname, String fieldType, int length, String fieldDefault, boolean notNull) throws Exception;

    /**
     * Changes a custom column in customer_tbl for given company_id to a new type and/or default value.
     */
    boolean alterColumnTypeInDbTable(@VelocityCheck int companyID, String fieldname, String fieldType, int length, String fieldDefault, boolean notNull) throws Exception;

    /**
     * Removes custom column in customer_tbl for given company_id.
     *
     * @param companyID Table of customers for this company will be altered.
     * @param fieldname Database column name to remove.
     * @throws Exception
     */
    void removeProfileField(@VelocityCheck int companyID, String fieldname) throws ProfileFieldException;
}
