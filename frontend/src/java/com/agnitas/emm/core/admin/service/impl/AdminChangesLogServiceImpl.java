/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminService;

public class AdminChangesLogServiceImpl implements AdminChangesLogService {

    /** The logger. */
    private static final transient Logger logger = LogManager.getLogger(AdminChangesLogServiceImpl.class);

    /** Service handling admins. */
    private AdminService adminService;

    @Override
    public List<UserAction> getChangesAsUserActions(AdminForm newAdminData, Admin oldAdmin, AdminPreferences oldAdminPreferences) {
        List<UserAction> userActions = new ArrayList<>();

        collectAdminChanges(userActions, newAdminData, oldAdmin);
        collectAdminPreferencesChanges(userActions, newAdminData, oldAdmin, oldAdminPreferences);

        return userActions;
    }

    /**
     * Compare existed and new user preferences data and represent changes as {@link UserAction} entities.
     *
     * @param userActions a list of user actions to store one if any change found (for UAL).
     * @param newAdminData new admin data
     * @param oldAdmin who will be changed and saved
     */
    private void collectAdminChanges(List<UserAction> userActions, AdminForm newAdminData, Admin oldAdmin) {
        //Open EMM changes
        try {
            String userName = oldAdmin.getUsername();
            if (!oldAdmin.getFullname().equals(newAdminData.getFullname())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Name changed from " + oldAdmin.getFullname() + " to " + newAdminData.getFullname()));
            }
            if (!userName.equals(newAdminData.getUsername())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Login user name changed from " + userName + " to " + newAdminData.getUsername()));
            }

            if (passwordChanged(oldAdmin.getUsername(), newAdminData.getPassword())) {
                userActions.add(new UserAction("edit user", "Username: " + userName + ". Password changed"));
            }
            if (!oldAdmin.getAdminLang().equals(newAdminData.getAdminLocale().getLanguage())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Language changed from " + Locale.forLanguageTag(oldAdmin.getAdminLang()).getDisplayLanguage() +
                                " to " + Locale.forLanguageTag(newAdminData.getAdminLocale().getLanguage()).getDisplayLanguage()));
            }
            if (!oldAdmin.getAdminTimezone().equals(newAdminData.getAdminTimezone())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Timezone changed from " + oldAdmin.getAdminTimezone() + " to " + newAdminData.getAdminTimezone()));
            }
            
            Set<Integer> currentGroupIds = new HashSet<>(oldAdmin.getGroupIds());
            Set<Integer> newGroupIds = new HashSet<>();
            if (newAdminData.getGroupIDs() != null) {
            	newGroupIds.addAll(newAdminData.getGroupIDs());
            }
            if (!currentGroupIds.equals(newGroupIds)) {
                String oldGroupIdsList = StringUtils.join(currentGroupIds, ",");
                String newGroupIdsList = StringUtils.join(newGroupIds, ",");

                userActions.add(new UserAction("edit user", "Username: " + userName + ". User Group changed from " + oldGroupIdsList + " to " + newGroupIdsList));
            }

            if (logger.isInfoEnabled()) {
                logger.info("save user: save user " + newAdminData.getAdminID());
            }
        } catch (Exception e) {
            logger.error("User changes error: " + e.getMessage());
        }

        //EMM changes
        try {
            String userName = oldAdmin.getUsername();
            //Log changes of gender (Salutation)
            if (oldAdmin.getGender() != newAdminData.getGender()) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Gender changed from " + AdminChangesLogService.getGenderText(oldAdmin.getGender()) +
                                " to " + AdminChangesLogService.getGenderText(newAdminData.getGender())));
            }
            
            //log first name changes
            if (!StringUtils.equals(oldAdmin.getFirstName(), newAdminData.getFirstname())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". First Name changed from " + oldAdmin.getFirstName() + " to " + newAdminData.getFirstname()));
            }
            
            //log employeeid changes
            if (!StringUtils.equals(oldAdmin.getEmployeeID(), newAdminData.getEmployeeID())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". EmployeeID changed from " + oldAdmin.getEmployeeID() + " to " + newAdminData.getEmployeeID()));
            }
            
            //log email changes
            if (!StringUtils.equals(oldAdmin.getEmail(), newAdminData.getEmail())) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Email changed from " + oldAdmin.getEmail() + " to " + newAdminData.getEmail()));
            }

            //log statistic email address changes
            String existingStatEmail = AgnUtils.emptyToNull(oldAdmin.getStatEmail());
            String newStatEmail = AgnUtils.emptyToNull(newAdminData.getStatEmail());

            if (!StringUtils.equalsIgnoreCase(existingStatEmail, newStatEmail)) {
                if (existingStatEmail == null) {
                    userActions.add(new UserAction("edit user", "Username: " + userName + ". Statistic email " + newStatEmail + " added"));
                } else if (newStatEmail == null) {
                    userActions.add(new UserAction("edit user", "Username: " + userName + ". Statistic email " + existingStatEmail + " removed"));
                } else {
                    userActions.add(new UserAction("edit user", "Username: " + userName + ". Statistic email changed from " + existingStatEmail + " to " + newStatEmail));
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: save user " + newAdminData.getAdminID());
            }
        } catch (Exception e) {
            logger.error("Log EMM User changes error " + e, e);
        }
    }

    /**
     * Compare existed and new user preferences data and represent changes as {@link UserAction} entities.
     *
     * @param userActions a list of user actions to store one if any change found (for UAL).
     * @param newAdminData - new data for front-end
     * @param oldAdmin - current data
     * @param oldAdminPreferences - current admin preferences
     */
    private void collectAdminPreferencesChanges(List<UserAction> userActions, AdminForm newAdminData, Admin oldAdmin, AdminPreferences oldAdminPreferences) {
        //EMM preferences
        try {
            String userName = oldAdmin.getUsername();
            AdminPreferences newAdminPreferences = newAdminData.getAdminPreferences();

            // Log changes of default mailing content view
            int oldMailingContentView = oldAdminPreferences.getMailingContentView();
            int newMailingContentView = newAdminPreferences.getMailingContentView();

            if (oldMailingContentView != newMailingContentView) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". User mailing content view type changed from " + AdminChangesLogService.getMailingContentViewName(oldMailingContentView) +
                                " to " + AdminChangesLogService.getMailingContentViewName(newMailingContentView)));
            }

            //Log changes of default dashboard mailings view
            int oldDashboardMailingsView = oldAdminPreferences.getDashboardMailingsView();
            int newDashboardMailingsView = newAdminPreferences.getDashboardMailingsView();

            if (oldDashboardMailingsView != newDashboardMailingsView) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Dashboard mailings view type changed from " + AdminChangesLogService.getDashboardMailingsView(oldDashboardMailingsView) +
                                " to " + AdminChangesLogService.getDashboardMailingsView(newDashboardMailingsView)));
            }

            // Log changes of default mailing settings view (expanded ot collapsed)
            int oldMailingSettingsView = oldAdminPreferences.getMailingSettingsView();
            int newMailingSettingsView = newAdminPreferences.getMailingSettingsView();

            if (oldMailingSettingsView != newMailingSettingsView) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Default mailing settings view changed from " + AdminChangesLogService.getMailingSettingsViewName(oldMailingSettingsView) +
                                " to " + AdminChangesLogService.getMailingSettingsViewName(newMailingSettingsView)));
            }

            // Log changes of default position of the mailing content live preview (right/bottom/deactivated)
            int oldLivePreviewPosition = oldAdminPreferences.getLivePreviewPosition();
            int newLivePreviewPosition = newAdminPreferences.getLivePreviewPosition();

            if (oldLivePreviewPosition != newLivePreviewPosition) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Mailing content live preview position changed from " + AdminChangesLogService.getMailingLivePreviewPosition(oldLivePreviewPosition) +
                                " to " + AdminChangesLogService.getMailingLivePreviewPosition(newLivePreviewPosition)));
            }

            //Log changes of Statistic-Summary load type
            int oldStatisticLoadType = oldAdminPreferences.getStatisticLoadType();
            int newStatisticLoadType = newAdminPreferences.getStatisticLoadType();

            if (oldStatisticLoadType != newStatisticLoadType) {
                userActions.add(new UserAction("edit user",
                        "Username: " + userName + ". Statistic-Summary load type changed from " + AdminChangesLogService.getStatisticLoadType(oldStatisticLoadType) +
                                " to " + AdminChangesLogService.getStatisticLoadType(newStatisticLoadType)));
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: edit save user preferences " + newAdminData.getAdminID());
            }
        } catch (Exception e) {
            logger.error("Log EMM User preferences changes error" + e);
        }
    }

    private boolean passwordChanged(String username, String password) {
        final Optional<Admin> adminOptional = adminService.findAdminByCredentials(username, password);

        return !(StringUtils.isEmpty(password) || (adminOptional.isPresent() && adminOptional.get().getAdminID() > 0));
    }

    public void setAdminService(AdminService service) {
        this.adminService = Objects.requireNonNull(service, "Admin service is null");
    }
}
