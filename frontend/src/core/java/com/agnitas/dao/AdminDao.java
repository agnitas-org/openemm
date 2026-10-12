/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.admin.service.UserListOpts;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.news.enums.NewsType;
import org.apache.commons.lang3.StringUtils;

public interface AdminDao {

	String USERNAME_COL = "username";

	enum UserListAdditionalColumn {

		COMPANY("company_name"),
		USER_GROUP("user_group"),
		LANGUAGE("admin_lang");

		private static final Map<String, UserListAdditionalColumn> BY_COLUMN = Arrays.stream(values())
				.collect(Collectors.toUnmodifiableMap(UserListAdditionalColumn::name, Function.identity()));


		public final String dbColumn;

		UserListAdditionalColumn(String dbColumn) {
			this.dbColumn = dbColumn;
		}

		public String getDbColumn() {
			return dbColumn;
		}

		public static Optional<UserListAdditionalColumn> fromColumn(String column) {
			return Optional.ofNullable(BY_COLUMN.get(StringUtils.defaultString(column)));
		}
	}

    List<AdminEntry> getAllAdminsByCompanyIdOnly(int companyID);

    List<AdminEntry> getAllAdminsByCompanyIdOnlyHasEmail(int companyID);

    int getNumberOfGuiAdmins(int companyID);

    Map<Integer, String> getAdminsNamesMap(int companyId);

    PaginatedList<AdminEntry> getAdminList(UserListOpts opts, boolean showRestfulUsers);
	
	int getNumberOfRestfulUsers(int companyID);

	boolean updateNewsDate(final int adminID, final Date newsDate, final NewsType type);
	
	boolean isAdminPassword(Admin admin, String password);
	
	Admin getAdmin(int adminID, int companyID);

	Admin getAdminByLogin(String name, String password);

	void save(Admin admin);

    /**
     * Deletes an admin and his permissions.
     *
     * @param admin
     *            The admin to be deleted.
     * @return true
     */
	boolean delete(Admin admin);
	
	boolean delete(final int adminID, final int companyID);

	List<String> getUsernames(boolean restful);

	List<String> getUsernames(boolean restful, int companyId);

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
     */
    int saveAdminRights(int adminID, Set<String> userRights);

    Admin getAdmin(String username);

    Optional<Admin> findUiUser(String username, int companyId);

    Admin getByEmail(String email);

	Admin getOldestAdminOfCompany(int companyId);

	List<Admin> getAdmins(int companyID, boolean restful);

	List<Integer> getAccessLimitingAdmins(int accessLimitingTargetGroupID);

	int getOpenEmmDemoAccountWaitingMailingID(String language);

	void deleteAdminPermissionsForCompany(int companyID);

	List<Map<String, Object>> getAdminsLight(int companyID, boolean restful);

    void saveDashboardLayout(String layout, int adminId);

	String getDashboardLayout(int adminId);

    void setPasswordReminderState(int adminId, PasswordReminderState state);

	PaginatedList<AdminEntry> getList(int companyId, String sort, String dir, int pageNumber, int pageSize);

    List<AdminEntry> findAllByEmailPart(String email, int companyID);

    List<AdminEntry> findAllByEmailPart(String email);

    void updateEmail(String email, int id, int companyId);

    AdminEntry findByEmail(String email, int companyId);

	List<Admin> getAdmins(Set<String> emails, int companyId);

}
