/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.admin.AdminNameNotFoundException;
import com.agnitas.emm.core.admin.AdminNameNotUniqueException;
import com.agnitas.emm.core.news.enums.NewsType;

public interface ComAdminDao {
    List<Map<String, Object>> getAdminsNames(@VelocityCheck int companyID, List<Integer> adminsIds);

    List<AdminEntry> getAllAdminsByCompanyIdOnly(@VelocityCheck int companyID);

	ComAdmin getAdminForReport(@VelocityCheck int companyID);

    List<AdminEntry> getAllAdminsByCompanyIdOnlyHasEmail(@VelocityCheck int companyID);

	/**
	 * <Admin ID, Username> list sorted by username
	 */
    List<Tuple<Integer, String>> getAdminsUsernames(int companyID);

    Map<Integer, String> getAdminsNamesMap(@VelocityCheck int companyId);

    PaginatedListImpl<AdminEntry> getAdminList(
    	@VelocityCheck int companyID,
    	String searchFirstName,
    	String searchLastName,
    	String searchEmail,
    	String searchCompanyName,
    	Integer filterCompanyId,
    	Integer filterAdminGroupId,
		Integer filterMailinglistId,
    	String filterLanguage,
    	String sort,
    	String direction,
    	int pageNumber,
    	int pageSize);
	
	/**
	 * Read admin from DB.
	 * 
	 * @param username user name of admin
	 * 
	 * @return {@link ComAdmin} for given user name
	 * @throws AdminNameNotFoundException
	 * @throws AdminNameNotUniqueException
	 */
	ComAdmin getByNameAndActiveCompany(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException;
	
	/**
	 * Get number of active and not deleted admins
	 */
	int getNumberOfAdmins();
	
	int getNumberOfAdmins(@VelocityCheck int companyID);

	void deleteFeaturePermissions(Set<String> unAllowedPremiumFeatures);

	boolean updateNewsDate(final int adminID, final Date newsDate, final NewsType type);
	
	boolean isAdminPassword(ComAdmin admin, String password);
	
	ComAdmin getAdmin(int adminID, @VelocityCheck int companyID);

	ComAdmin getAdminByLogin(String name, String password);

	void save(ComAdmin admin) throws Exception;

    /**
     * Deletes an admin and his permissions.
     *
     * @param admin
     *            The admin to be deleted.
     * @return true
     */
	boolean delete(ComAdmin admin);
	
	boolean delete(final int adminID, final int companyID);

	List<AdminEntry> getAllAdminsByCompanyId( @VelocityCheck int companyID);

	List<AdminEntry> getAllAdmins();

	List<AdminEntry> getAllWsAdminsByCompanyId( @VelocityCheck int companyID);			// TODO Move to webservice related class

	List<AdminEntry> getAllWsAdmins();													// TODO Move to webservice related class

    /**
     * Checks the existence of any admin with given username for certain company.
     *
     * @param username user name for the admin.
     * @return true if the admin exists, and false otherwise.
     */
	boolean adminExists(String username);

	boolean isEnabled(ComAdmin admin);

	boolean checkBlacklistedAdminNames(String username);

    /**
     * Saves permission set for given admin
     *
     * @param adminID
     *              The id of the admin whose right are to be stored
     * @param userRights
     *               Set of permissions
     * @return
     */
    int saveAdminRights(int adminID, Set<String> userRights);

    ComAdmin getAdmin(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException;
    String getAdminName(int adminID, @VelocityCheck int companyID);

	/**
	 * Get timezone id for an admin referenced by {@code adminId}.
	 *
	 * @param adminId an identifier of an admin to access.
	 * @param companyId an identifier of a company that a referenced admin belongs to.
	 * @return a timezone id or {@code null} if admin doesn't exist or timezone is not specified.
	 */
	String getAdminTimezone(int adminId, @VelocityCheck int companyId);

	ComAdmin getOldestAdminOfCompany(int companyId);

	DataSource getDataSource();

	int getAdminWelcomeMailingId(String language);

	int getPasswordResetMailingId(String language);

	int getPasswordChangedMailingId(String language);

	void updateLoginDate(int adminID, Date date);
}
