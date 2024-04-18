/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.Tuple;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.AdminNameNotFoundException;
import com.agnitas.emm.core.admin.AdminNameNotUniqueException;
import com.agnitas.emm.core.news.enums.NewsType;

public interface AdminDao {
    List<Map<String, Object>> getAdminsNames(int companyID, List<Integer> adminsIds);

    List<AdminEntry> getAllAdminsByCompanyIdOnly(int companyID);

	Admin getAdminForReport(int companyID);

    List<AdminEntry> getAllAdminsByCompanyIdOnlyHasEmail(int companyID);

	/**
	 * <Admin ID, Username> list sorted by username
	 */
    List<Tuple<Integer, String>> getAdminsUsernames(int companyID);
    
    int getNumberOfGuiAdmins(int companyID);

    Map<Integer, String> getAdminsNamesMap(int companyId);

    PaginatedListImpl<AdminEntry> getAdminList(
    	int companyID,
    	String searchFirstName,
    	String searchLastName,
    	String searchEmail,
    	String searchCompanyName,
    	Integer filterCompanyId,
    	Integer filterAdminGroupId,
		Integer filterMailinglistId,
    	String filterLanguage,
		DateRange creationDate,
		DateRange lastLoginDate,
		String username,
    	String sort,
    	String direction,
    	int pageNumber,
    	int pageSize,
    	boolean showRestfulUsers);
	
	/**
	 * Read admin from DB.
	 * 
	 * @param username user name of admin
	 * 
	 * @return {@link Admin} for given user name
	 * @throws AdminNameNotFoundException
	 * @throws AdminNameNotUniqueException
	 */
	Admin getByNameAndActiveCompany(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException;
	
	int getNumberOfRestfulUsers(int companyID);

	boolean updateNewsDate(final int adminID, final Date newsDate, final NewsType type);
	
	boolean isAdminPassword(Admin admin, String password);
	
	Admin getAdmin(int adminID, int companyID);

	Admin getAdminByLogin(String name, String password);

	void save(Admin admin) throws Exception;

    /**
     * Deletes an admin and his permissions.
     *
     * @param admin
     *            The admin to be deleted.
     * @return true
     */
	boolean delete(Admin admin);
	
	boolean delete(final int adminID, final int companyID);

	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	List<AdminEntry> getAllAdminsByCompanyId(int companyID);
	List<AdminEntry> getAllAdminsByCompanyId(boolean restful, int companyID);

	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	List<AdminEntry> getAllAdmins();
	List<AdminEntry> getAllAdmins(boolean restful);

	List<AdminEntry> getAllWsAdminsByCompanyId( int companyID);			// TODO Move to webservice related class

	List<AdminEntry> getAllWsAdmins();													// TODO Move to webservice related class

    /**
     * Checks the existence of any admin with given username for certain company.
     *
     * @param username user name for the admin.
     * @return true if the admin exists, and false otherwise.
     */
	boolean adminExists(String username);

	boolean isEnabled(Admin admin);

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

    boolean isPermissionGranted(int adminId, Permission permission);
    void grantPermission(int adminId, Permission permission);

    void revokePermission(int adminId, Permission permission);

    Admin getAdmin(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException;
    String getAdminName(int adminID, int companyID);

	/**
	 * Get timezone id for an admin referenced by {@code adminId}.
	 *
	 * @param adminId an identifier of an admin to access.
	 * @param companyId an identifier of a company that a referenced admin belongs to.
	 * @return a timezone id or {@code null} if admin doesn't exist or timezone is not specified.
	 */
	String getAdminTimezone(int adminId, int companyId);

	Admin getOldestAdminOfCompany(int companyId);

	DataSource getDataSource();

	int getAdminWelcomeMailingId(String language);

	int getPasswordResetMailingId(String language);

	int getPasswordChangedMailingId(String language);

	List<Admin> getAdmins(int companyID, boolean restful);

	List<Integer> getAccessLimitingAdmins(int accessLimitingTargetGroupID);

	int getSecurityCodeMailingId(String language);

	int getOpenEmmDemoAccountWaitingMailingID(String language);

	int getOpenEmmDemoAccountDataMailingID(String language);

	void deleteAdminPermissionsForCompany(int companyID);

	List<Map<String, Object>> getAdminsLight(int companyID, boolean restful);

    void saveDashboardLayout(String layout, int adminId);

	String getDashboardLayout(int adminId);

    int getPasswordExpirationMailingId(String language);

    void setPasswordReminderState(int adminId, PasswordReminderState state);
}
