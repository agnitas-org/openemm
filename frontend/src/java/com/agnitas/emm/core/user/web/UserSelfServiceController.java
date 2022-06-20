/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.web;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComEmmLayoutBaseDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.departments.service.DepartmentService;
import com.agnitas.emm.core.departments.util.DepartmentI18n;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.supervisor.common.UnknownSupervisorIdException;
import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;
import com.agnitas.emm.core.user.form.SupervisorGrantLoginPermissionForm;
import com.agnitas.emm.core.user.form.UserSelfForm;
import com.agnitas.emm.core.user.service.UserSelfService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.util.OneOf;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.AdminGroup;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import org.agnitas.emm.core.commons.password.util.PasswordUtil;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

@Controller
@RequestMapping("/selfservice")
@PermissionMapping("selfservice")
public class UserSelfServiceController {

    /**
     * ID representing selection "all departments".
     */
    public static final int ALL_DEPARTMENTS_ID = -1;
    private static final Logger logger = LogManager.getLogger(UserSelfServiceController.class);
    private static final String UNLIMITED_LOGIN_PERMISSION = "UNLIMITED";
    private static final String LIMITED_LOGIN_PERMISSION = "LIMITED";

    private WebStorage webStorage;
    private ComCompanyDao companyDao;
    private AdminPreferencesDao adminPreferencesDao;
    private ComAdminGroupDao adminGroupDao;
    private AdminService adminService;
    private ConfigService configService;
    private UserActivityLogService userActivityLogService;
    private PasswordCheck passwordCheck;
    private ComEmmLayoutBaseDao layoutBaseDao;
    private ComLogonService logonService;
    private UserSelfService userSelfService;
    private SupervisorLoginPermissionService supervisorLoginPermissionService;
    private DepartmentService departmentService;

    public UserSelfServiceController(WebStorage webStorage, ComCompanyDao companyDao, AdminPreferencesDao adminPreferencesDao, AdminService adminService,
                                     ComAdminGroupDao adminGroupDao, ConfigService configService, UserActivityLogService userActivityLogService, PasswordCheck passwordCheck,
                                     ComEmmLayoutBaseDao layoutBaseDao, ComLogonService logonService, UserSelfService userSelfService, DepartmentService departmentService,
                                     SupervisorLoginPermissionService supervisorLoginPermissionService) {

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
        this.departmentService = departmentService;
        this.supervisorLoginPermissionService = supervisorLoginPermissionService;
    }

    @RequestMapping("/view.action")
    public String view(ComAdmin admin, @ModelAttribute("adminForm") UserSelfForm form, Model model) {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.ADMIN_LOGIN_LOG_OVERVIEW, form);

        fillForm(form, admin);
        List<LoginData> loginTrackingList = userSelfService.getLoginTrackingList(admin, UserSelfService.DEFAULT_LOGIN_MIN_PERIOD_DAYS, admin.getDateTimeFormatWithSeconds());

        model.addAttribute("loginTrackingList", loginTrackingList);
        model.addAttribute("availableAdminGroups", adminGroupDao.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), admin.getGroupIds()));
        model.addAttribute("availableTimezones", TimeZone.getAvailableIDs());
        model.addAttribute("availableLayouts", adminService.getEmmLayoutsBase(admin.getCompanyID()));
        model.addAttribute("passwordPolicy", PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService).getPolicyName());
        model.addAttribute("showSupervisorPermissionManagement", admin.getSupervisor() == null && configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID()));

        return "user_selfservice_new";
    }

    @PostMapping("/save.action")
    public String save(ComAdmin admin, @ModelAttribute("adminForm") UserSelfForm form, Popups popups, HttpSession session) {
        AdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());

        writeUserChangesLog(admin, form);
        writeUserPreferencesChangesLog(admin, adminPreferences, form);

        if (saveData(admin, form, popups, adminPreferences)) {
            updateSessionAttributes(session, admin, adminPreferences);

            return "redirect:/selfservice/view.action";
        }

        return "messages";
    }

    @GetMapping("/showSupervisorPermission.action")
    public String showSupervisorPermission(ComAdmin admin, Model model, HttpServletRequest request, @ModelAttribute("supervisorLoginForm") SupervisorGrantLoginPermissionForm form) {
        if (admin.getSupervisor() == null && configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID())) {
            model.addAttribute("showSupervisorPermissionManagement", true);
            model.addAttribute("departmentList", departmentService.listAllDepartments());
            model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
            model.addAttribute("activeLoginPermissions", supervisorLoginPermissionService.listActiveSupervisorLoginPermissions(admin));
            model.addAttribute("allDepartmentsId", ALL_DEPARTMENTS_ID);

            AgnUtils.setAdminDateTimeFormatPatterns(request);
        }

        return "user_selfservice_sv_login_new";
    }

    @RequestMapping("/revokeSupervisorPermission.action")
    public String revokeSupervisorPermission(ComAdmin admin, @RequestParam(name = "permissionID") int permissionId, Popups popups) {
        if (checkIfNotSupervisor(admin, popups)) {
            try {
                supervisorLoginPermissionService.revokeSupervisorLoginPermission(admin.getAdminID(), permissionId);

                logPermissionRevoking(admin, permissionId);

                popups.success("settings.supervisor.loginPermissionRevoked");
            } catch (UnknownSupervisorLoginPermissionException e) {
                popups.alert("error.settings.supervisor.revokingLoginPermission");
                logger.error(String.format("Error revoking login permission %d for admin %d", permissionId, admin.getAdminID()), e);
            }
        }

        return "redirect:/selfservice/showSupervisorPermission.action";
    }

    private void logPermissionRevoking(ComAdmin admin, int permissionId) {
        try {
            Department department = supervisorLoginPermissionService.getDepartmentForLoginPermission(permissionId);

            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) revoked login permission of department '%s'", admin.getUsername(), admin.getAdminID(), department.getSlug());
            } else {
                message = String.format("User '%s' (%d) revoked login permission of ALL departments", admin.getUsername(), admin.getAdminID());
            }

            writeUserActivityLog(admin, "grant login permission", message);
        } catch (final UnknownSupervisorLoginPermissionException | UnknownDepartmentIdException e) {
            String message = String.format("User '%s' (%d) revoked login permission of unknown department", admin.getUsername(), admin.getAdminID());

            writeUserActivityLog(admin, "revoked login permission", message);
        }
    }

    private boolean checkIfNotSupervisor(ComAdmin admin, Popups popups) {
        if (admin.getSupervisor() != null) {
            logger.warn(String.format("Attempt to grant supervisor login permission for EMM user '%s' (ID %d, company %d), logged in with supervisor '%s' (%d)", admin.getUsername(), admin.getAdminID(), admin.getCompanyID(), admin.getSupervisor().getSupervisorName(), admin.getSupervisor().getId()));
            popups.alert("error.settings.supervisor.revokeLoginPermissionBySupervisor");

            return false;
        }

        return true;
    }

    @PostMapping("/grantSupervisorPermission.action")
    public String grantSupervisorPermission(ComAdmin admin, @ModelAttribute("form") SupervisorGrantLoginPermissionForm form, Popups popups) {
        if (checkIfNotSupervisor(admin, popups) && validateLoginPermissionForm(form, popups)) {
            if (form.getDepartmentID() < 0 && !OneOf.oneIntOf(form.getDepartmentID(), ALL_DEPARTMENTS_ID)) {
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("Received invalid department ID %d. Changed it to 0", form.getDepartmentID()));
                }

                form.setDepartmentID(0);
            }

            if (UNLIMITED_LOGIN_PERMISSION.equals(form.getLimit())) {
                grantUnlimitedLoginPermission(form.getDepartmentID(), admin, popups);
            } else {
                grantLimitedLoginPermission(form, admin, popups);
            }
        }

        return "redirect:/selfservice/showSupervisorPermission.action";
    }

    private void grantUnlimitedLoginPermission(int departmentID, ComAdmin admin, Popups popups) {
        try {
            if (departmentID > 0) {
                supervisorLoginPermissionService.grantUnlimitedLoginPermissionToDepartment(admin, departmentID);

                Department department = departmentService.getDepartmentByID(departmentID);
                logGrantedPermission(admin, department, null);
                popups.success("settings.supervisor.loginGranted.unlimited", DepartmentI18n.translateDepartmentName(department, admin.getLocale()), department.getId());

            } else if (departmentID == ALL_DEPARTMENTS_ID) {
                supervisorLoginPermissionService.grantUnlimitedLoginPermissionToAllDepartments(admin);

                logGrantedPermission(admin, null, null);
                popups.success("settings.supervisor.loginGrantedToAll.unlimited");
            }

        } catch (UnknownSupervisorIdException e) {
            popups.alert("supervisor.error.unknown_id");
        } catch (Exception e) {
            logger.error(String.format("Error granting unlimited login permission to department %d (all if 0)", departmentID), e);
            popups.alert("supervisor.error.general");
        }
    }

    private void grantLimitedLoginPermission(SupervisorGrantLoginPermissionForm form, ComAdmin admin, Popups popups) {
        SimpleDateFormat dateFormat = admin.getDateFormat();

        try {
            Date expireDateFromForm = dateFormat.parse(form.getExpireDateLocalized());

            // As long as the user can only specify a date, the permission is valid including this day, so we have to add 1 day to achieve this.
            Date expireDate = getNextDayFromDate(expireDateFromForm);

            int departmentID = form.getDepartmentID();

            try {
                if (departmentID > 0) {
                    supervisorLoginPermissionService.grantLoginPermissionToDepartment(admin, departmentID, expireDate);

                    Department department = departmentService.getDepartmentByID(departmentID);

                    logGrantedPermission(admin, department, expireDateFromForm);
                    popups.success("settings.supervisor.loginGranted", DepartmentI18n.translateDepartmentName(department, admin.getLocale()), department.getId());
                } else if (departmentID == ALL_DEPARTMENTS_ID) {
                    supervisorLoginPermissionService.grantLoginPermissionToAllDepartments(admin, expireDate);

                    logGrantedPermission(admin, null, expireDateFromForm);
                    popups.success("settings.supervisor.loginGrantedToAll");
                }

            } catch (UnknownSupervisorIdException e) {
                popups.alert("supervisor.error.unknown_id");
            } catch (Exception e) {
                logger.error(String.format("Error granting limited login permission to department %d (all if 0)", departmentID), e);
                popups.alert("supervisor.error.general");
            }
        } catch (ParseException e) {
            popups.alert("error.date.format");
        }
    }

    private Date getNextDayFromDate(Date inputDate) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(inputDate);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    private void logGrantedPermission(ComAdmin admin, Department department, Date expireDate) {
        if (expireDate != null) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String formattedDate = dateFormat.format(expireDate);

            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) granted login permission to department '%s' until including %s", admin.getUsername(), admin.getAdminID(), department.getSlug(), formattedDate);
            } else {
                message = String.format("User '%s' (%d) granted login permission to ALL departments until including %s", admin.getUsername(), admin.getAdminID(), formattedDate);
            }

            writeUserActivityLog(admin, "grant login permission", message);
        } else {
            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) granted UNLIMITED login permission to department '%s'", admin.getUsername(), admin.getAdminID(), department.getSlug());
            } else {
                message = String.format("User '%s' (%d) granted UNLIMITED login permission to ALL departments", admin.getUsername(), admin.getAdminID());
            }

            writeUserActivityLog(admin, "grant login permission", message);
        }
    }

    private boolean validateLoginPermissionForm(SupervisorGrantLoginPermissionForm loginPermissionForm, Popups popups) {
        if (loginPermissionForm.getDepartmentID() == 0) {
            popups.alert("error.settings.supervisor.noDepartmentSelected");
        }

        if (!OneOf.oneObjectOf(loginPermissionForm.getLimit(), LIMITED_LOGIN_PERMISSION, UNLIMITED_LOGIN_PERMISSION)) {
            popups.alert("error.settings.supervisor.noLoginLimitSelected");
        }

        if (LIMITED_LOGIN_PERMISSION.equals(loginPermissionForm.getLimit()) && StringUtils.isEmpty(loginPermissionForm.getExpireDateLocalized())) {
            popups.alert("error.settings.supervisor.noExpireDate");
        }

        return !popups.hasAlertPopups();
    }

    private boolean saveData(ComAdmin admin, @ModelAttribute("adminForm") UserSelfForm form, Popups popups, AdminPreferences adminPreferences) {
        if (validate(form, popups, admin)) {
            admin.setFullname(form.getFullname());

            if (StringUtils.isNotBlank(form.getFirstname())) {
                admin.setFirstName(form.getFirstname());
            }

            if (StringUtils.isNotBlank(form.getEmployeeID())) {
                admin.setEmployeeID(form.getEmployeeID());
            }

            admin.setCompanyName(form.getCompanyName());
            admin.setEmail(form.getEmail());
            admin.setStatEmail(form.getStatEmail());

            if (StringUtils.isNotEmpty(form.getPassword())) {
                admin.setPasswordForStorage(form.getPassword());
                writeUserActivityLog(admin, "change password", form.getUsername() + " (" + form.getId() + ")");
            }

            admin.setAdminLang(form.getAdminLocale().getLanguage());
            admin.setAdminCountry(form.getAdminLocale().getCountry());
            admin.setLayoutBaseID(form.getLayoutBaseId());
            admin.setAdminTimezone(form.getAdminTimezone());
            admin.setGender(form.getGender());

            if (form.getGroupIDs() != null && admin.permissionAllowed(Permission.ADMIN_SETGROUP)) {
                List<AdminGroup> adminGroups = new ArrayList<>();

                for (String adminGroupId : form.getGroupIDs()) {
                    adminGroups.add(adminGroupDao.getAdminGroup(Integer.parseInt(adminGroupId), form.getCompanyID()));
                }

                admin.setGroups(adminGroups);
            }

            try {
                adminService.save(admin);
            } catch (Exception e) {
                e.printStackTrace();
            }

            saveNewPreferences(adminPreferences, form);
            popups.success("default.changes_saved");

            return true;
        }

        return false;
    }

    private boolean validate(UserSelfForm form, Popups popups, ComAdmin admin) {
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
            PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups, "password");

            if (adminService.isAdminPassword(admin, form.getPassword())) {
                popups.alert("error.password_must_differ");
            } else if (!form.getPassword().equals(form.getPasswordConfirm())) {
                popups.alert("error.password.mismatch");
            } else {
                passwordCheck.checkAdminPassword(form.getPassword(), admin, handler);
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

    private void updateSessionAttributes(HttpSession session, ComAdmin admin, AdminPreferences adminPreferences) {
        session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin);
        session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
        session.setAttribute("emmLayoutBase", layoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID()));
        session.setAttribute("emm.locale", admin.getLocale());

        logonService.updateSessionsLanguagesAttributes(admin);
    }

    private void fillForm(UserSelfForm form, ComAdmin admin) {
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
        form.setInitialCompanyName(companyDao.getCompany(admin.getCompanyID()).getShortname());
        form.setFirstname(admin.getFirstName());
        form.setEmployeeID(admin.getEmployeeID());
        form.setMailingContentView(adminPreferences.getMailingContentView());
        form.setDashboardMailingsView(adminPreferences.getDashboardMailingsView());
        form.setMailingSettingsView(adminPreferences.getMailingSettingsView());
        form.setLivePreviewPosition(adminPreferences.getLivePreviewPosition());
        form.setStatisticLoadType(adminPreferences.getStatisticLoadType());

        if (logger.isDebugEnabled()) {
            logger.debug("loadAdmin: admin " + form.getId() + " loaded");
        }
    }

    private void writeUserPreferencesChangesLog(ComAdmin admin, AdminPreferences adminPreferences, UserSelfForm adminForm) {
        try {
            String userName = admin.getUsername();

            //Log changes of default dashboard mailings view
            int oldDashboardMailingsView = adminPreferences.getDashboardMailingsView();
            int newDashboardMailingsView = adminForm.getDashboardMailingsView();

            if (oldDashboardMailingsView != newDashboardMailingsView) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Dashboard mailings view type changed from " + getDashboardMailingsView(oldDashboardMailingsView) +
                                " to " + getDashboardMailingsView(newDashboardMailingsView));
            }

            // Log changes of Statistic-Summary load type
            int oldStatisticLoadType = adminPreferences.getStatisticLoadType();
            int newStatisticLoadType = adminForm.getStatisticLoadType();

            if (oldStatisticLoadType != newStatisticLoadType) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Statistic-Summary load type changed from " + getStatisticLoadType(oldStatisticLoadType) +
                                " to " + getStatisticLoadType(newStatisticLoadType));
            }

            // Log changes of default mailing content view
            int oldMailingContentView = adminPreferences.getMailingContentView();
            int newMailingContentView = adminForm.getMailingContentView();

            if (oldMailingContentView != newMailingContentView) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". User mailing content view type changed from " + getMailingContentViewName(oldMailingContentView) +
                                " to " + getMailingContentViewName(newMailingContentView));
            }

            // Log changes of default mailing settings view (expanded ot collapsed)
            int oldMailingSettingsView = adminPreferences.getMailingSettingsView();
            int newMailingSettingsView = adminForm.getMailingSettingsView();

            if (oldMailingSettingsView != newMailingSettingsView) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Default mailing settings view changed from " + getMailingSettingsViewName(oldMailingSettingsView) +
                                " to " + getMailingSettingsViewName(newMailingSettingsView));
            }

            // Log changes of default position of the mailing content live preview (right/bottom/deactivated)
            int oldLivePreviewPosition = adminPreferences.getLivePreviewPosition();
            int newLivePreviewPosition = adminForm.getLivePreviewPosition();

            if (oldLivePreviewPosition != newLivePreviewPosition) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Mailing content live preview position changed from " + getMailingLivePreviewPosition(oldLivePreviewPosition) +
                                " to " + getMailingLivePreviewPosition(newLivePreviewPosition));
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user preferences " + adminForm.getId());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self user preferences changes error: " + e.getMessage(), e);
        }
    }

    private void writeUserChangesLog(ComAdmin admin, UserSelfForm adminForm) {
        try {
            String userName = admin.getUsername();
            //Log changes of gender (Salutation)
            if (admin.getGender() != adminForm.getGender()) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Gender changed from " + getGenderText(admin.getGender()) + " to " + getGenderText(adminForm.getGender()));
            }
            //Log changes of first name
            if (!(admin.getFirstName().equals(adminForm.getFirstname()))) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". First Name changed from " + admin.getFirstName() + " to " + adminForm.getFirstname());
            }
            //Log changes of last name
            if (!(admin.getFullname().equals(adminForm.getFullname()))) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Last Name changed from " + admin.getFullname() + " to " + adminForm.getFullname());
            }
            //Log changes of email
            if (!(admin.getEmail().equals(adminForm.getEmail()))) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Email changed from " + admin.getEmail() + " to " + adminForm.getEmail());
            }
            //Log changes of password
            if (PasswordUtil.passwordChanged(adminService, admin.getUsername(), adminForm.getPassword())) {
                writeUserActivityLog(admin, "edit user", userName + ". Password changed");
            }
            //Log changes of language
            if (!(admin.getAdminLang().equals(adminForm.getAdminLocale().getLanguage()))) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Language changed from " + Locale.forLanguageTag(admin.getAdminLang()).getDisplayLanguage() +
                                " to " + Locale.forLanguageTag(adminForm.getAdminLocale().getLanguage()).getDisplayLanguage());
            }
            //Log changes of timezone
            if (!(admin.getAdminTimezone().equals(adminForm.getAdminTimezone()))) {
                writeUserActivityLog(admin, "edit user",
                        userName + ". Timezone changed from " + admin.getAdminTimezone() + " to " + adminForm.getAdminTimezone());
            }

            //Log changes of statistic email address
            String existingStatEmail = admin.getStatEmail();
            if (StringUtils.isBlank(existingStatEmail)) {
                existingStatEmail = "";
            }
            String newStatEmail = adminForm.getStatEmail();
            if (StringUtils.isBlank(newStatEmail)) {
                newStatEmail = "";
            }

            if (!existingStatEmail.equals(newStatEmail)) {
                if (existingStatEmail.isEmpty() && !newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email " + newStatEmail + " added");
                }
                if (!existingStatEmail.isEmpty() && newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email " + existingStatEmail + " removed");
                }
                if (!existingStatEmail.isEmpty() && !newStatEmail.isEmpty()) {
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email changed from " + existingStatEmail + " to " + newStatEmail);
                }
            }

            // Log changes of userGroup
            Set<Integer> currentGroupIds = new HashSet<>(admin.getGroupIds());
            Set<Integer> newGroupIds = new HashSet<>();
            if (adminForm.getGroupIDs() != null) {
                for (String groupId : adminForm.getGroupIDs()) {
                    newGroupIds.add(Integer.parseInt(groupId));
                }
            }
            if (!currentGroupIds.equals(newGroupIds)) {
                String oldGroupIdsList = StringUtils.join(currentGroupIds, ",");
                String newGroupIdsList = StringUtils.join(newGroupIds, ",");

                writeUserActivityLog(admin, "edit user", userName + ". User Group changed from " + oldGroupIdsList + " to " + newGroupIdsList);
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user " + adminForm.getId());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self changes error: " + e.getMessage(), e);
        }
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        try {
            if (userActivityLogService != null) {
                userActivityLogService.writeUserActivityLog(admin, action, description);
            } else {
                logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
                logger.info("Userlog: " + admin.getUsername() + " " + action + " " + description);
            }
        } catch (Exception e) {
            logger.error("Error writing ActivityLog: " + e.getMessage(), e);
            logger.info("Userlog: " + admin.getUsername() + " " + action + " " + description);
        }
    }
}
