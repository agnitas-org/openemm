/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.password.PasswordCheck;
import com.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import com.agnitas.emm.core.commons.password.WebservicePasswordCheckImpl;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.commons.util.ConfigValue.Webservices;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserForm;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserFormSearchParams;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserOverviewFilter;
import com.agnitas.emm.wsmanager.bean.WebservicePermissionGroups;
import com.agnitas.emm.wsmanager.bean.WebservicePermissions;
import com.agnitas.emm.wsmanager.common.UnknownWebserviceUsernameException;
import com.agnitas.emm.wsmanager.service.WebservicePermissionGroupService;
import com.agnitas.emm.wsmanager.service.WebservicePermissionService;
import com.agnitas.emm.wsmanager.service.WebserviceUserAlreadyExistsException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping(value = "/administration/wsmanager")
@SessionAttributes(types = WebserviceUserFormSearchParams.class)
public class WebserviceUserManagerController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(WebserviceUserManagerController.class);
    private static final String REDIRECT_TO_OVERVIEW = "redirect:/administration/wsmanager/users.action?restoreSort=true";

    private static final String PASSWORD_STR = "password";

    private final ConfigService configService;
    private final WebserviceUserService webserviceUserService;
    private final CompanyService companyService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final WebservicePermissionService webservicePermissionService;
    private final WebservicePermissionGroupService webservicePermissionGroupService;

    private final PasswordCheck passwordCheck = new WebservicePasswordCheckImpl();

    public WebserviceUserManagerController(ConfigService configService, WebserviceUserService webserviceUserService, CompanyService companyService,
                                           ConversionService conversionService, WebStorage webStorage, UserActivityLogService userActivityLogService,
                                           WebservicePermissionService webservicePermissionService, WebservicePermissionGroupService webservicePermissionGroupService) {

        this.configService = Objects.requireNonNull(configService);
        this.webserviceUserService = Objects.requireNonNull(webserviceUserService);
        this.companyService = Objects.requireNonNull(companyService);
        this.conversionService = Objects.requireNonNull(conversionService);
        this.webStorage = Objects.requireNonNull(webStorage);
        this.userActivityLogService = Objects.requireNonNull(userActivityLogService);
        this.webservicePermissionService = Objects.requireNonNull(webservicePermissionService, "Webservice permission service is null");
        this.webservicePermissionGroupService = Objects.requireNonNull(webservicePermissionGroupService, "Webservice permission group service is null");
    }

    @RequestMapping(value = "/users.action")
    @RequiredPermission("webservice.user.show")
    public String list(Admin admin, @ModelAttribute("filter") WebserviceUserOverviewFilter filter, WebserviceUserFormSearchParams searchParams,
                                 @RequestParam(required = false) Boolean restoreSort, WebserviceUserForm userForm, Model model) throws WebserviceUserServiceException {
        searchParams.restoreParams(filter);
        FormUtils.syncPaginationData(webStorage, WebStorage.WS_MANAGER_OVERVIEW, filter, restoreSort);

        model.addAttribute("webserviceUserList", webserviceUserService.getPaginatedWSUserList(filter, admin));
        model.addAttribute("companyList", companyService.getActiveOwnCompanyEntries(admin.getCompanyID(), true));
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

        writeUserActivityLog(admin, "webservice manager", "active tab - manage webservice user");

        return "webserviceuser_list";
    }

    @GetMapping("/search.action")
    @RequiredPermission("webservice.user.show")
    public String search(@ModelAttribute WebserviceUserOverviewFilter filter, WebserviceUserFormSearchParams searchParams) {
        searchParams.storeParams(filter);
        return REDIRECT_TO_OVERVIEW;
    }

    @PostMapping(value = "/user/new.action")
    @RequiredPermission("webservice.user.create")
    public String create(Admin admin, @ModelAttribute("webserviceUserForm") WebserviceUserForm userForm, Popups popups) {
        processCompanyId(admin, userForm);
        if (creationValidation(userForm, popups)) {
            if (saveWebserviceUser(admin, true, userForm, popups)) {
                return REDIRECT_TO_OVERVIEW;
            }
        }

        popups.alert("error.webserviceuser.cannot_create");
        return REDIRECT_TO_OVERVIEW;
    }

    @PostMapping(value = "/user/update.action")
    @RequiredPermission("webservice.user.change")
    public String update(Admin admin, @ModelAttribute("webserviceUserForm") WebserviceUserForm userForm, Popups popups) {
        if (editingValidation(userForm, popups)) {
            if (saveWebserviceUser(admin, false, userForm, popups)) {
                return REDIRECT_TO_OVERVIEW;
            }
        }

        popups.alert("error.webserviceuser.cannot_update");
        return MESSAGES_VIEW;
    }

    @GetMapping(value = "/user/delete.action")
    @RequiredPermission("webservice.user.change")
    public String confirmDelete(@RequestParam(required = false) Set<String> usernames, Model model) {
        validateSelectedUsernames(usernames);
        model.addAttribute("usernames", usernames);
        return "user_delete_modal";
    }

    @RequestMapping(value = "/user/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("webservice.user.change")
    public String delete(@RequestParam(required = false) Set<String> usernames, Popups popups,
                         @RequestParam(required = false) Boolean includingActivity) {
        validateSelectedUsernames(usernames);

        for (String username : usernames) {
            this.webserviceUserService.deleteWebserviceUser(username);
        }

        if (Boolean.TRUE.equals(includingActivity)) {
            userActivityLogService.deleteSoapActivity(usernames);
        }

        popups.selectionDeleted();
        return REDIRECT_TO_OVERVIEW;
    }

    private void validateSelectedUsernames(Set<String> usernames) {
        if (CollectionUtils.isEmpty(usernames)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    private void processCompanyId(final Admin admin, final WebserviceUserForm userForm) {
        if (!admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)) {
            userForm.setCompanyId(admin.getCompanyID());
        }
    }

    private boolean validateMaxNumberWSUsers(int companyID, Popups popups) {
        int currentNumberOfWebserviceUsers = webserviceUserService.getNumberOfWebserviceUsers(companyID);
        int maxNumberWSUsers = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfWebserviceUsers, companyID);
        if (maxNumberWSUsers >= 0 && maxNumberWSUsers <= currentNumberOfWebserviceUsers) {
        	popups.alert("error.numberOfWebserviceUsersExceeded", currentNumberOfWebserviceUsers);
        	return false;
        }

        return true;
    }

    private boolean creationValidation(WebserviceUserForm userForm, Popups popups) {
        boolean valid = true;
        if (StringUtils.isBlank(userForm.getUserName())) {
            popups.fieldError("userName", "error.webserviceuser.no_username");
            valid = false;
        }

        String email = userForm.getEmail();
        if (StringUtils.isBlank(email) || !AgnUtils.isEmailValid(email)) {
            popups.fieldError("email", "error.email.invalid");
            valid = false;
        }

        if (StringUtils.isBlank(userForm.getPassword())) {
            popups.fieldError(PASSWORD_STR, "error.password.missing");
            valid = false;
        }

        if (!passwordCheck.checkAdminPassword(userForm.getPassword(), null, new SpringPasswordCheckHandler(popups, PASSWORD_STR))) {
        	valid = false;
        }

        if (userForm.getCompanyId() <= 0) {
            popups.fieldError("companyId", "error.webserviceuser.no_company");
            valid = false;
        }

        if (!validateMaxNumberWSUsers(userForm.getCompanyId(), popups)) {
            valid = false;
        }

        return valid;
    }

    private boolean editingValidation(WebserviceUserForm userForm, Popups popups) {
        boolean valid = true;

        String email = userForm.getEmail();
        if (StringUtils.isBlank(email) || !AgnUtils.isEmailValid(email)) {
            popups.fieldError("email", "error.email.invalid");
            valid = false;
        }

        if (!validateMaxNumberWSUsers(userForm.getCompanyId(), popups)) {
            valid = false;
        }
        return valid;
    }

    private boolean saveWebserviceUser(Admin admin, boolean isNew, WebserviceUserForm userForm, Popups popups) {
        try {
            webserviceUserService.saveWebServiceUser(admin, conversionService.convert(userForm, WebserviceUserDto.class), isNew);

            popups.changesSaved();

            writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "webservice user", getWsUserDescription(userForm));

        	return true;
        } catch (WebserviceUserAlreadyExistsException e) {
            logger.error("Cannot create webservice user. {}", e.getMessage());
            popups.fieldError("userName", "error.webserviceuser.already_exists");
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot update webservice user. {}", e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        } catch (Exception e) {
            logger.error("Cannot create webservice user. {}", e.getMessage());
            popups.alert("error.default.message");
        }

        return false;
    }

    @GetMapping(value = "/user/{username}/view.action")
    @RequiredPermission("webservice.user.change")
    public String view(Admin admin, @PathVariable String username, Model model, Popups popups) {
        try {
        	final WebserviceUserDto webserviceUser = webserviceUserService.getWebserviceUserByUserName(username);
        	final WebserviceUserForm userForm = conversionService.convert(webserviceUser, WebserviceUserForm.class);

            model.addAttribute("webserviceUserForm", userForm);
            model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

            boolean permissionsEnabled = configService.getBooleanValue(Webservices.WebserviceEnablePermissions, webserviceUser.getCompanyId());

            if (permissionsEnabled) {
                // show permissions only if WS permissions are enabled for company of webservice user!
                final WebservicePermissions permissions = webservicePermissionService.listAllPermissions();
            	final WebservicePermissionGroups permissionGroups = webservicePermissionGroupService.listAllPermissionGroups();

	            model.addAttribute("PERMISSIONS", permissions);
	            model.addAttribute("PERMISSION_GROUPS", permissionGroups);
            }

            model.addAttribute("PERMISSIONS_ENABLED", permissionsEnabled);

            writeUserActivityLog(admin, "view webservice user", getWsUserDescription(userForm));

            return "webserviceuser_view";
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot obtain webservice user. {}", e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        } catch (Exception e) {
            logger.error("Cannot obtain webservice user. {}", e.getMessage());
            popups.alert("error.default.message");
        }

        return MESSAGES_VIEW;
    }

    private String getWsUserDescription(WebserviceUserForm userForm) {
        return String.format("%s - companyID %d", userForm.getUserName(), userForm.getCompanyId());
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), logger);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return PASSWORD_STR.equals(param);
    }

}
