/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.web;

import java.util.Objects;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.password.WebservicePasswordCheckImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.ConfigValue.Webservices;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormUtils;
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

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserForm;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserListForm;
import com.agnitas.emm.wsmanager.bean.WebservicePermissionGroups;
import com.agnitas.emm.wsmanager.bean.WebservicePermissions;
import com.agnitas.emm.wsmanager.common.UnknownWebserviceUsernameException;
import com.agnitas.emm.wsmanager.service.WebservicePermissionGroupService;
import com.agnitas.emm.wsmanager.service.WebservicePermissionService;
import com.agnitas.emm.wsmanager.service.WebserviceUserAlreadyExistsException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

@Controller
@RequestMapping(value = "/administration/wsmanager")
@PermissionMapping(value = "webservice.manager.user")
public class WebserviceUserManagerController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(WebserviceUserManagerController.class);
    
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

    public WebserviceUserManagerController(final ConfigService configService,
    		final WebserviceUserService webserviceUserService,
    		final CompanyService companyService,
    		final ConversionService conversionService,
    		final WebStorage webStorage,
    		final UserActivityLogService userActivityLogService,
    		final WebservicePermissionService webservicePermissionService,
    		final WebservicePermissionGroupService webservicePermissionGroupService) {
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
    public String list(Admin admin, @ModelAttribute WebserviceUserListForm userListForm, WebserviceUserForm userForm, Model model) throws WebserviceUserServiceException {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.WS_MANAGER_OVERVIEW, userListForm);

        model.addAttribute("webserviceUserList",
                webserviceUserService.getPaginatedWSUserList(admin.getCompanyID(),
                        userListForm.getSort(),
                        userListForm.getOrder(),
                        userListForm.getPage(),
                        userListForm.getNumberOfRows(),
                        admin.permissionAllowed(Permission.MASTER_SHOW)));

        model.addAttribute("companyList", companyService.getActiveOwnCompanyEntries(admin.getCompanyID(), true));
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

        writeUserActivityLog(admin, "webservice manager", "active tab - manage webservice user");

        return "webserviceuser_list";
    }

    @PostMapping(value = "/user/new.action")
    public String create(Admin admin, @ModelAttribute("webserviceUserForm") WebserviceUserForm userForm, Popups popups, final Model model) {
        processCompanyId(admin, userForm);
        if (creationValidation(userForm, popups)) {
            if (saveWebserviceUser(admin, true, userForm, popups)) {
                return "redirect:/administration/wsmanager/users.action";
            }
        }
        
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");
    
        popups.alert("error.webserviceuser.cannot_create");
        return "forward:/administration/wsmanager/users.action";
    }

    @PostMapping(value = "/user/update.action")
    public String update(Admin admin, @ModelAttribute("webserviceUserForm") WebserviceUserForm userForm, Popups popups) {
        if (editingValidation(userForm, popups)) {
            if (saveWebserviceUser(admin, false, userForm, popups)) {
                return "redirect:/administration/wsmanager/users.action";
            }
        }
        
        popups.alert("error.webserviceuser.cannot_update");
        return MESSAGES_VIEW;
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
            popups.field("userName", "error.webserviceuser.no_username");
            valid = false;
        }
        
        String email = userForm.getEmail();
        if (StringUtils.isBlank(email) || !AgnUtils.isEmailValid(email)) {
            popups.field("email", "error.email.invalid");
            valid = false;
        }
        
        if (StringUtils.isBlank(userForm.getPassword())) {
            popups.field(PASSWORD_STR, "error.password.missing");
            valid = false;
        }
        
        if (!passwordCheck.checkAdminPassword(userForm.getPassword(), null, new SpringPasswordCheckHandler(popups, PASSWORD_STR))) {
        	valid = false;
        }
        
        if (userForm.getCompanyId() <= 0) {
            popups.field("company", "error.webserviceuser.no_company");
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
            popups.field("email", "error.email.invalid");
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

            popups.success(CHANGES_SAVED_MSG);

            writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "webservice user", getWsUserDescription(userForm));
            
        	return true;
        } catch (WebserviceUserAlreadyExistsException e) {
            logger.error("Cannot create webservice user. {}", e.getMessage());
            popups.field("userName", "error.webserviceuser.already_exists");
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
