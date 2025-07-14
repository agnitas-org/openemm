/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.EmmLayoutBaseDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.user.form.UserSelfForm;
import com.agnitas.emm.core.user.service.UserSelfService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.AdminGroup;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import org.agnitas.emm.core.commons.password.util.PasswordUtil;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getDashboardMailingsView;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getGenderText;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingContentViewName;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingLivePreviewPosition;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingSettingsViewName;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getStatisticLoadType;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

public class UserSelfController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(UserSelfController.class);

    private static final String EDIT_USER_STR = "edit user";

    protected final ConfigService configService;
    private final WebStorage webStorage;
    private final CompanyDao companyDao;
    private final AdminPreferencesDao adminPreferencesDao;
    private final AdminGroupDao adminGroupDao;
    private final AdminService adminService;
    private final UserActivityLogService userActivityLogService;
    private final PasswordCheck passwordCheck;
    private final EmmLayoutBaseDao layoutBaseDao;
    private final LogonService logonService;
    private final UserSelfService userSelfService;

    public UserSelfController(WebStorage webStorage, CompanyDao companyDao, AdminPreferencesDao adminPreferencesDao, AdminService adminService,
                              AdminGroupDao adminGroupDao, ConfigService configService, UserActivityLogService userActivityLogService, PasswordCheck passwordCheck,
                              EmmLayoutBaseDao layoutBaseDao, LogonService logonService, UserSelfService userSelfService) {

        this.webStorage = webStorage;
        this.companyDao = companyDao;
        this.adminPreferencesDao = adminPreferencesDao;
        this.adminService = adminService;
        this.adminGroupDao = adminGroupDao;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.passwordCheck = passwordCheck;
        this.layoutBaseDao = layoutBaseDao;
        this.logonService = logonService;
        this.userSelfService = userSelfService;
    }

    @RequestMapping("/view.action")
    public String view(Admin admin, @ModelAttribute("selfForm") UserSelfForm form, Model model) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.ADMIN_LOGIN_LOG_OVERVIEW, form);

        fillForm(form, admin);
        prepareModelAttributesForViewPage(model, admin);

        return "user_selfservice";
    }

    protected void prepareModelAttributesForViewPage(Model model, Admin admin) {
        int companyID = admin.getCompanyID();

        List<LoginData> loginTrackingList = userSelfService.getLoginTrackingList(admin, UserSelfService.DEFAULT_LOGIN_MIN_PERIOD_DAYS, admin.getDateTimeFormatWithSeconds());

        model.addAttribute("loginTrackingList", loginTrackingList);
        model.addAttribute("availableAdminGroups", adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyID, admin.getGroupIds()));
        model.addAttribute("availableTimezones", TimeZone.getAvailableIDs());
        model.addAttribute("availableLayouts", adminService.getEmmLayoutsBase(admin));
        model.addAttribute("passwordPolicy", PasswordPolicyUtil.loadCompanyPasswordPolicy(companyID, configService).getPolicyName());
    }

    @PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute UserSelfForm form, Popups popups, HttpSession session) {
        AdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());

        writeUserChangesLog(admin, form);
        writeUserPreferencesChangesLog(admin, adminPreferences, form);

        if (!validate(form, popups, admin)) {
            return MESSAGES_VIEW;
        }

        saveData(admin, form, adminPreferences);
        popups.success(CHANGES_SAVED_MSG);

        updateSessionAttributes(session, admin, adminPreferences);

        return "redirect:/user/self/view.action";
    }

    private void saveData(Admin admin, UserSelfForm form, AdminPreferences adminPreferences) {
        final String adminLangBefore = admin.getAdminLang();
        final String adminTimeZoneBefore = admin.getAdminTimezone();

        admin.setFullname(form.getFullname());

        if (StringUtils.isNotBlank(form.getFirstname())) {
            admin.setFirstName(form.getFirstname());
        }

        admin.setTitle(form.getTitle());

        if (StringUtils.isNotBlank(form.getEmployeeID())) {
            admin.setEmployeeID(form.getEmployeeID());
        }

        admin.setCompanyName(form.getCompanyName());
        admin.setEmail(form.getEmail());
        admin.setStatEmail(form.getStatEmail());

        if (StringUtils.isNotEmpty(form.getPassword())) {
            admin.setPasswordForStorage(form.getPassword());
            writeUserActivityLog(admin, "change password", String.format("%s (%d)", form.getUsername(), form.getId()));
        }

        admin.setAdminLang(form.getAdminLocale().getLanguage());
        admin.setAdminCountry(form.getAdminLocale().getCountry());
        admin.setLayoutBaseID(form.getLayoutBaseId());
        if (admin.isRedesignedUiUsed()) {
            admin.setLayoutType(form.getUiLayoutType());
        }
        admin.setAdminTimezone(form.getAdminTimezone());
        admin.setGender(form.getGender());

        if (form.getGroupIDs() != null && admin.permissionAllowed(Permission.ADMIN_SETGROUP)) {
            List<AdminGroup> adminGroups = new ArrayList<>();

            for (String adminGroupId : form.getGroupIDs()) {
                adminGroups.add(adminGroupDao.getAdminGroup(Integer.parseInt(adminGroupId), form.getCompanyID()));
            }

            admin.setGroups(adminGroups);
        }

        adminService.save(admin);
        if (!adminLangBefore.equals(admin.getAdminLang())
            || !adminTimeZoneBefore.equals(admin.getAdminTimezone())) {
            AgnUtils.updateBrowserCacheMarker();
        }

        saveNewPreferences(adminPreferences, form);
    }

    private boolean validate(UserSelfForm form, Popups popups, Admin admin) {
        if (StringUtils.isBlank(form.getFullname())) {
            popups.alert("error.invalid.username");
        }

        if (StringUtils.isBlank(form.getCompanyName())) {
            popups.alert("error.name.is.empty");
        }

        if (!AgnUtils.isEmailValid(form.getEmail())) {
            popups.alert("error.invalid.email");
        }

        if (!StringUtils.isEmpty(form.getStatEmail()) && !AgnUtils.isEmailValid(form.getStatEmail())) {
            popups.alert("error.invalid.email");
        }

        if (StringUtils.isNotEmpty(form.getPassword())) {
            if (adminService.isAdminPassword(admin, form.getCurrentPassword())) {
                PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups, "password");

                if (form.getCurrentPassword().equals(form.getPassword())) {
                    popups.alert("error.password_must_differ");
                } else if (!form.getPassword().equals(form.getPasswordConfirm())) {
                    popups.alert("error.password.mismatch");
                } else {
                    passwordCheck.checkAdminPassword(form.getPassword(), admin, handler);
                }
            } else {
                popups.alert("error.password.wrong");
            }
        }

        return !popups.hasAlertPopups();
    }

    private void saveNewPreferences(AdminPreferences adminPreferences, UserSelfForm form) {
        if (adminPreferences != null) {
            adminPreferences.setAdminID(form.getId());
            adminPreferences.setDashboardMailingsView(form.getDashboardMailingsView());
            adminPreferences.setLivePreviewPosition(form.getLivePreviewPosition());
            adminPreferences.setMailingContentView(form.getMailingContentView());
            adminPreferences.setMailingSettingsView(form.getMailingSettingsView());
            adminPreferences.setStatisticLoadType(form.getStatisticLoadType());

            adminPreferencesDao.save(adminPreferences);
        }
    }

    private void updateSessionAttributes(HttpSession session, Admin admin, AdminPreferences adminPreferences) {
        session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin);
        session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
        session.setAttribute("emmLayoutBase", layoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID()));
        session.setAttribute("emm.locale", admin.getLocale());

        logonService.updateSessionsLanguagesAttributes(admin);
    }

    private void fillForm(UserSelfForm form, Admin admin) {
        AdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());

        String[] groupIds = new String[admin.getGroupIds().size()];

        for (int i = 0; i < admin.getGroupIds().size(); i++) {
            groupIds[i] = Integer.toString(admin.getGroupIds().get(i));
        }

        form.setGender(admin.getGender());
        form.setId(admin.getAdminID());
        form.setTitle(admin.getTitle());
        form.setUsername(admin.getUsername());
        form.setPassword("");
        form.setPasswordConfirm("");
        form.setCompanyID(admin.getCompanyID());
        form.setFullname(admin.getFullname());
        form.setAdminLocale(new Locale(admin.getAdminLang(), admin.getAdminCountry()));
        form.setAdminTimezone(admin.getAdminTimezone());
        form.setGroupIDs(groupIds);
        form.setStatEmail(admin.getStatEmail());
        form.setCompanyName(admin.getCompanyName());
        form.setEmail(admin.getEmail());
        form.setLayoutBaseId(admin.getLayoutBaseID());
        form.setUiLayoutType(admin.getLayoutType());
        form.setInitialCompanyName(companyDao.getCompany(admin.getCompanyID()).getShortname());
        form.setFirstname(admin.getFirstName());
        form.setEmployeeID(admin.getEmployeeID());
        form.setMailingContentView(adminPreferences.getMailingContentView());
        form.setDashboardMailingsView(adminPreferences.getDashboardMailingsView());
        form.setMailingSettingsView(adminPreferences.getMailingSettingsView());
        form.setLivePreviewPosition(adminPreferences.getLivePreviewPosition());
        form.setStatisticLoadType(adminPreferences.getStatisticLoadType());

        if (logger.isDebugEnabled()) {
            logger.debug("loadAdmin: admin {} loaded", form.getId());
        }
    }

    private void writeUserPreferencesChangesLog(Admin admin, AdminPreferences adminPreferences, UserSelfForm form) {
        try {
            String userName = admin.getUsername();

            //Log changes of default dashboard mailings view
            int oldDashboardMailingsView = adminPreferences.getDashboardMailingsView();
            int newDashboardMailingsView = form.getDashboardMailingsView();

            if (oldDashboardMailingsView != newDashboardMailingsView) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Dashboard mailings view type changed from %s to %s",
                        userName, getDashboardMailingsView(oldDashboardMailingsView), getDashboardMailingsView(newDashboardMailingsView)));
            }

            // Log changes of Statistic-Summary load type
            int oldStatisticLoadType = adminPreferences.getStatisticLoadType();
            int newStatisticLoadType = form.getStatisticLoadType();

            if (oldStatisticLoadType != newStatisticLoadType) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Statistic-Summary load type changed from %s to %s",
                        userName, getStatisticLoadType(oldStatisticLoadType), getStatisticLoadType(newStatisticLoadType)));
            }

            // Log changes of default mailing content view
            int oldMailingContentView = adminPreferences.getMailingContentView();
            int newMailingContentView = form.getMailingContentView();

            if (oldMailingContentView != newMailingContentView) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. User mailing content view type changed from %s to %s",
                        userName, getMailingContentViewName(oldMailingContentView), getMailingContentViewName(newMailingContentView)));
            }

            // Log changes of default mailing settings view (expanded ot collapsed)
            int oldMailingSettingsView = adminPreferences.getMailingSettingsView();
            int newMailingSettingsView = form.getMailingSettingsView();

            if (oldMailingSettingsView != newMailingSettingsView) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Default mailing settings view changed from %s to %s",
                        userName, getMailingSettingsViewName(oldMailingSettingsView), getMailingSettingsViewName(newMailingSettingsView)));
            }

            // Log changes of default position of the mailing content live preview (right/bottom/deactivated)
            int oldLivePreviewPosition = adminPreferences.getLivePreviewPosition();
            int newLivePreviewPosition = form.getLivePreviewPosition();

            if (oldLivePreviewPosition != newLivePreviewPosition) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Mailing content live preview position changed from %s to %s",
                        userName, getMailingLivePreviewPosition(oldLivePreviewPosition), getMailingLivePreviewPosition(newLivePreviewPosition)));
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user preferences {}", form.getId());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self user preferences changes error: {}", e.getMessage(), e);
        }
    }

    private void writeUserChangesLog(Admin admin, UserSelfForm form) {
        try {
            String userName = admin.getUsername();
            //Log changes of gender (Salutation)
            if (admin.getGender() != form.getGender()) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Gender changed from %s to %s",
                        userName, getGenderText(admin.getGender()), getGenderText(form.getGender())));
            }
            //Log changes of first name
            if (!(admin.getFirstName().equals(form.getFirstname()))) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. First Name changed from %s to %s",
                        userName, admin.getFirstName(), form.getFirstname()));
            }
            //Log changes of last name
            if (!(admin.getFullname().equals(form.getFullname()))) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Last Name changed from %s to %s",
                        userName, admin.getFullname(), form.getFullname()));
            }
            //Log changes of email
            if (!(admin.getEmail().equals(form.getEmail()))) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Email changed from %s to %s",
                        userName, admin.getEmail(), form.getEmail()));
            }
            //Log changes of password
            if (PasswordUtil.passwordChanged(adminService, admin.getUsername(), form.getPassword())) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Password changed", userName));
            }
            //Log changes of language
            if (!(admin.getAdminLang().equals(form.getAdminLocale().getLanguage()))) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Language changed from %s to %s",
                        userName, Locale.forLanguageTag(admin.getAdminLang()).getDisplayLanguage(),
                        Locale.forLanguageTag(form.getAdminLocale().getLanguage()).getDisplayLanguage())
                );
            }
            //Log changes of timezone
            if (!(admin.getAdminTimezone().equals(form.getAdminTimezone()))) {
                writeUserActivityLog(admin, EDIT_USER_STR, String.format("%s. Timezone changed from %s to %s",
                        userName, admin.getAdminTimezone(), form.getAdminTimezone()));
            }

            //Log changes of statistic email address
            String existingStatEmail = admin.getStatEmail();
            if (StringUtils.isBlank(existingStatEmail)) {
                existingStatEmail = "";
            }
            String newStatEmail = form.getStatEmail();
            if (StringUtils.isBlank(newStatEmail)) {
                newStatEmail = "";
            }

            if (!existingStatEmail.equals(newStatEmail)) {
                if (existingStatEmail.isEmpty() && !newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, EDIT_USER_STR,
                            String.format("%s. Statistic email %s added", userName, newStatEmail));
                }
                if (!existingStatEmail.isEmpty() && newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, EDIT_USER_STR,
                            String.format("%s. Statistic email %s removed", userName, existingStatEmail));
                }
                if (!existingStatEmail.isEmpty() && !newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, EDIT_USER_STR,
                            String.format("%s. Statistic email changed from %s to %s", userName, existingStatEmail, newStatEmail));
                }
            }

            // Log changes of userGroup
            Set<Integer> currentGroupIds = new HashSet<>(admin.getGroupIds());
            Set<Integer> newGroupIds = new HashSet<>();
            if (form.getGroupIDs() != null) {
                for (String groupId : form.getGroupIDs()) {
                    newGroupIds.add(Integer.parseInt(groupId));
                }
            }
            if (!currentGroupIds.equals(newGroupIds)) {
                String oldGroupIdsList = StringUtils.join(currentGroupIds, ",");
                String newGroupIdsList = StringUtils.join(newGroupIds, ",");

                writeUserActivityLog(admin, EDIT_USER_STR,
                        String.format("%s. User Group changed from %s to %s", userName, oldGroupIdsList, newGroupIdsList));
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user {}", form.getId());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self changes error: {}", e.getMessage(), e);
        }
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return "password".equals(param) || "passwordConfirm".equals(param);
    }
}
