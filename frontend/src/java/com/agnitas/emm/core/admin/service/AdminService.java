/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.Company;
import com.agnitas.beans.EmmLayoutBase;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.news.enums.NewsType;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.Tuple;

public interface AdminService {

    /**
     * Returns admin by it's user name.
     *
     * @param username user name
     * @param supervisorName supervisor name
     * @param password password of supervisor
     *
     * @return {@link Admin} for given user name
     */
	Admin getAdminByNameForSupervisor(String username, String supervisorName, String password) throws AdminException, SupervisorException;
	Optional<Admin> findAdminByCredentials(final String username, final String password);

	Map<String, String> mapIdToUsernameByCompanyAndEmail(int companyId);

    List<Map<String, Object>> getAdminsLight(int companyID, boolean restful);

    Map<Integer, String> getAdminsNamesMap(int companyId);

	ServiceResult<Admin> isPossibleToDeleteAdmin(int adminId, int companyId);

	ServiceResult<Admin> delete(Admin admin, int adminIdToDelete);
    boolean deleteAdmin(final int adminID, final int companyID);

    AdminSavingResult saveAdmin(AdminForm form, boolean restfulUser, Admin editorAdmin);

    /**
     * Save the permissions for an admin.
     *
     * Rules for changing admin rights:
     * - Rights granted by the admingroup cannot be changed in anyway (Change admin's group itself if needed to do so)
     * - Standard rights can be changed in anyway by any GUI user, who has the right to change admin rights
     * - Premium rights can only be changed, if the GUI user has the specific premium right himself and has the right to change admin rights
     * - "Others" rights and rights of unknown categories can only be changed by emm-master
     *
     * For information on rules for changing user rights, see also:
     *  http://wiki.agnitas.local/doku.php?id=abteilung:allgemein:premiumfeatures&s[]=rechtevergabe#rechtevergabe-moeglichkeiten_in_der_emm-gui
     *
     * @return tuple of added and removed permission tokens or {@code null} if something went wrong.
     */
    Tuple<List<String>, List<String>> saveAdminPermissions(int companyID, int savingAdminID, Collection<String> tokens, int editorAdminID);

    void grantPermission(Admin admin, Permission permission);

    void revokePermission(Admin admin, Permission permission);

    Admin getAdmin(int adminID, int companyID);

    boolean adminExists(String username);

    boolean isGuiAdminLimitReached(int companyID);

    List<AdminGroup> getAdminGroups(int companyID, Admin admin);

    List<Company> getCreatedCompanies(int companyID);

    boolean adminGroupExists(int companyId, String groupname);

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
            int pageSize);

    PaginatedListImpl<AdminEntry> getList(int companyId, String sort, String dir, int pageNumber, int pageSize);

    List<AdminEntry> listAdminsByCompanyID(final int companyID);
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    List<AdminEntry> getAdminEntriesForUserActivityLog(Admin admin);
    List<AdminEntry> getAdminEntriesForUserActivityLog(Admin admin, UserActivityLogService.UserType type);

    PasswordState getPasswordState(Admin admin);

    Date computePasswordExpireDate(Admin admin);

    boolean setPassword(int adminId, int companyId, String password);

	boolean checkBlacklistedAdminNames(String username);

    Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(Admin admin, Admin adminToEdit);

    AdminPreferences getAdminPreferences(int adminId);

    List<EmmLayoutBase> getEmmLayoutsBase(Admin admin);

    boolean isDarkmodeEnabled(Admin admin);

	Optional<Admin> getAdminByName(String username);
	boolean updateNewsDate(final int adminID, final Date newsDate, final NewsType type);
	Admin getOldestAdminOfCompany(int companyId);

	void save(Admin admin);

	boolean isAdminPassword(Admin admin, String password);
	boolean isEnabled(Admin admin);
	Admin getAdminByLogin(String name, String password);

    int getAccessLimitTargetId(Admin admin);
    
    boolean isExtendedAltgEnabled(Admin admin);
    
	int getAdminWelcomeMailingId(String language);
	
	int getPasswordResetMailingId(String language);
	
	int getPasswordChangedMailingId(String language);
	
	int getSecurityCodeMailingId(String language);
	
	int getOpenEmmDemoAccountWaitingMailingID(String language);
	
	int getOpenEmmDemoAccountDataMailingID(String language);
	
	PaginatedListImpl<AdminEntry> getRestfulUserList(
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
            int pageSize);
	
	List<Admin> getAdmins(int companyID, boolean restful);

	void setDefaultPreferencesSettings(AdminPreferences preferences);
	List<Integer> getAccessLimitingAdmins(int accessLimitingTargetGroupID);
	
	int getNumberOfRestfulUsers(int companyID);
	
	int getNumberOfGuiAdmins(int companyID);
	void deleteAdminPermissionsForCompany(int companyID);

    void saveDashboardLayout(String layout, Admin admin);

    String getDashboardLayout(Admin admin);

    void warnAdminsAboutPasswordExpiration(CompaniesConstraints constraints);

    int getPasswordExpirationMailingId(String language);

    void setPasswordReminderState(int adminId, PasswordReminderState state);

    int getEmailChangedMailingId(String language);

    List<AdminEntry> findAllByEmailPart(String email, int companyID);
    List<AdminEntry> findAllByEmailPart(String email);

    AdminEntry findByEmail(String email, int companyId);

    ServiceResult<List<Admin>> getAllowedForDeletion(Set<Integer> ids, int companyID);
    List<Admin> delete(Set<Integer> ids, int companyId);
    SimpleServiceResult delete(int id, int companyId);

    void updateEmail(String email, int id, int companyId);
}
