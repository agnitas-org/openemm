/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.departments.service.DepartmentService;
import com.agnitas.emm.core.departments.util.DepartmentI18n;
import com.agnitas.emm.core.supervisor.common.UnknownSupervisorIdException;
import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;
import com.agnitas.emm.core.user.form.SupervisorGrantLoginPermissionForm;
import com.agnitas.util.OneOf;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequestMapping("/user/self/supervisor-permission")
@Controller
@PermissionMapping("user.self.supervisor")
public class SelfSupervisorPermissionController implements XssCheckAware {

    /** ID representing selection "all departments". */
    public static final int ALL_DEPARTMENTS_ID = -1;

    private static final Logger logger = LogManager.getLogger(SelfSupervisorPermissionController.class);
    private static final String UNLIMITED_LOGIN_PERMISSION = "UNLIMITED";
    private static final String LIMITED_LOGIN_PERMISSION = "LIMITED";
    private final ConfigService configService;
    private final DepartmentService departmentService;
    private final SupervisorLoginPermissionService supervisorLoginPermissionService;
    private final UserActivityLogService userActivityLogService;

    public SelfSupervisorPermissionController(ConfigService configService, DepartmentService departmentService, SupervisorLoginPermissionService supervisorLoginPermissionService,
                                              UserActivityLogService userActivityLogService) {
        this.configService = configService;
        this.departmentService = departmentService;
        this.supervisorLoginPermissionService = supervisorLoginPermissionService;
        this.userActivityLogService = userActivityLogService;
    }

    @GetMapping("/show.action")
    public String show(Admin admin, Model model, @ModelAttribute("supervisorLoginForm") SupervisorGrantLoginPermissionForm form) {
        if (admin.getSupervisor() == null && configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID())) {
            model.addAttribute("showSupervisorPermissionManagement", true);
            model.addAttribute("departmentList", departmentService.listAllDepartments());
            model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
            model.addAttribute("activeLoginPermissions", supervisorLoginPermissionService.listActiveSupervisorLoginPermissions(admin));
            model.addAttribute("allDepartmentsId", ALL_DEPARTMENTS_ID);

            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        }

        return "user_selfservice_sv_login";
    }

    @RequestMapping("/{permissionId:\\d+}/revoke.action")
    public String revoke(@PathVariable("permissionId") int permissionId, Admin admin, Popups popups) {
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

        return "redirect:/user/self/supervisor-permission/show.action";
    }

    @PostMapping("/grant.action")
    public String grant(Admin admin, @ModelAttribute SupervisorGrantLoginPermissionForm form, Popups popups) {
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

        return "redirect:/user/self/supervisor-permission/show.action";
    }

    private boolean checkIfNotSupervisor(Admin admin, Popups popups) {
        if (admin.getSupervisor() != null) {
            logger.warn(String.format("Attempt to grant supervisor login permission for EMM user '%s' (ID %d, company %d), logged in with supervisor '%s' (%d)", admin.getUsername(), admin.getAdminID(), admin.getCompanyID(), admin.getSupervisor().getSupervisorName(), admin.getSupervisor().getId()));
            popups.alert("error.settings.supervisor.revokeLoginPermissionBySupervisor");

            return false;
        }

        return true;
    }

    private void logPermissionRevoking(Admin admin, int permissionId) {
        try {
            Department department = supervisorLoginPermissionService.getDepartmentForLoginPermission(permissionId);

            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) revoked login permission of department '%s'", admin.getUsername(), admin.getAdminID(), department.getSlug());
            } else {
                message = String.format("User '%s' (%d) revoked login permission of ALL departments", admin.getUsername(), admin.getAdminID());
            }

            writeUserActivityLog(admin, "grant login permission", message);
        } catch (UnknownSupervisorLoginPermissionException | UnknownDepartmentIdException e) {
            String message = String.format("User '%s' (%d) revoked login permission of unknown department", admin.getUsername(), admin.getAdminID());

            writeUserActivityLog(admin, "revoked login permission", message);
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

    private void grantUnlimitedLoginPermission(int departmentID, Admin admin, Popups popups) {
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

    private void grantLimitedLoginPermission(SupervisorGrantLoginPermissionForm form, Admin admin, Popups popups) {
        SimpleDateFormat dateFormat = admin.getDateFormat();

        try {
            Date expireDateFromForm = dateFormat.parse(form.getExpireDateLocalized());

            // As long as the user can only specify a date, the permission is valid including this day, so we have to add 1 day to achieve this.
            Date expireDate = DateUtilities.getDateOfNextDay(expireDateFromForm);

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

    private void logGrantedPermission(Admin admin, Department department, Date expireDate) {
        if (expireDate != null) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String formattedDate = dateFormat.format(expireDate);

            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) granted login permission to department '%s' until including %s",
                        admin.getUsername(), admin.getAdminID(), department.getSlug(), formattedDate);
            } else {
                message = String.format("User '%s' (%d) granted login permission to ALL departments until including %s",
                        admin.getUsername(), admin.getAdminID(), formattedDate);
            }

            writeUserActivityLog(admin, "grant login permission", message);
        } else {
            String message;

            if (department != null) {
                message = String.format("User '%s' (%d) granted UNLIMITED login permission to department '%s'",
                        admin.getUsername(), admin.getAdminID(), department.getSlug());
            } else {
                message = String.format("User '%s' (%d) granted UNLIMITED login permission to ALL departments", admin.getUsername(), admin.getAdminID());
            }

            writeUserActivityLog(admin, "grant login permission", message);
        }
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }
}
