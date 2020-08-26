/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.web;

import java.util.Objects;

import javax.validation.Valid;

import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.beans.ComAdmin;
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

@Controller
@RequestMapping(value = "/administration/wsmanager")
@PermissionMapping(value = "webservice.manager.user")
public class WebserviceUserManagerController {

    private static final Logger logger = Logger.getLogger(WebserviceUserManagerController.class);

    private final ConfigService configService;
    private final WebserviceUserService webserviceUserService;
    private final CompanyService companyService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final WebservicePermissionService webservicePermissionService;
    private final WebservicePermissionGroupService webservicePermissionGroupService;

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
    public String list(ComAdmin admin, @ModelAttribute WebserviceUserListForm userListForm,
                       WebserviceUserForm userForm, Model model) throws WebserviceUserServiceException {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.WS_MANAGER_OVERVIEW, userListForm);

        model.addAttribute("webserviceUserList",
                webserviceUserService.getPaginatedWSUserList(admin.getCompanyID(),
                        userListForm.getSort(),
                        userListForm.getOrder(),
                        userListForm.getPage(),
                        userListForm.getNumberOfRows(),
                        admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)));

        model.addAttribute("companyList", companyService.getActiveOwnCompanyEntries(admin.getCompanyID()));
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

        writeUserActivityLog(admin, "webservice manager", "active tab - manage webservice user");

        return "webserviceuser_list";
    }

    @PostMapping(value = "/user/new.action")
    public String create(ComAdmin admin, @Valid @ModelAttribute WebserviceUserForm userForm, Popups popups, final Model model) {
        processCompanyId(admin, userForm);
        if (creationValidation(userForm, popups) ) {
            if (saveWebserviceUser(admin, true, userForm, popups)) {
                return "redirect:/administration/wsmanager/users.action";
            }
        }

        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

        popups.alert("error.webserviceuser.cannot_create");
        return "forward:/administration/wsmanager/users.action";
    }

    @PostMapping(value = "/user/update.action")
    public String update(ComAdmin admin, @Valid @ModelAttribute WebserviceUserForm userForm, Popups popups) {
        if (editingValidation(userForm, popups)) {
            if (saveWebserviceUser(admin, false, userForm, popups)) {
                return "redirect:/administration/wsmanager/users.action";
            }
        }
        
        popups.alert("error.webserviceuser.cannot_update");
        return "messages";
    }

    private void processCompanyId(final ComAdmin admin, final WebserviceUserForm userForm) {
        if(!admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)) {
            userForm.setCompanyId(admin.getCompanyID());
        }
    }
    
    private boolean validateMaxNumberWSUsers(Popups popups) {
        int currentNumberOfWebserviceUsers = webserviceUserService.getNumberOfWebserviceUsers();
        int maxNumberWSUsers = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfWebserviceUsers);
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
            popups.field("password", "error.password.missing");
            valid = false;
        }
        
        if (userForm.getCompanyId() <= 0) {
            popups.field("company", "error.webserviceuser.no_company");
            valid = false;
        }
        
        if (!validateMaxNumberWSUsers(popups)) {
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
        
        if (!validateMaxNumberWSUsers(popups)) {
            valid = false;
        }
        return valid;
    }
    
    private boolean saveWebserviceUser(ComAdmin admin, boolean isNew, WebserviceUserForm userForm, Popups popups) {
        try {
            webserviceUserService.saveWebServiceUser(admin, conversionService.convert(userForm, WebserviceUserDto.class), isNew);

            popups.success("default.changes_saved");

            writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "webservice user", getWsUserDescription(userForm));
            
        	return true;
        } catch (WebserviceUserAlreadyExistsException e) {
            logger.error("Cannot create webservice user. " + e.getMessage());
            popups.field("userName", "error.webserviceuser.already_exists");
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot update webservice user. " + e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        } catch (Exception e) {
            logger.error("Cannot create webservice user. " + e.getMessage());
            popups.alert("error.default.message");
        }
        
        return false;
    }

    @GetMapping(value = "/user/{username}/view.action")
    public String view(ComAdmin admin, @PathVariable String username, Model model, Popups popups) {
        try {
        	final WebserviceUserDto webserviceUser = this.webserviceUserService.getWebserviceUserByUserName(username);
        	final WebserviceUserForm userForm = conversionService.convert(webserviceUser, WebserviceUserForm.class);
        	
        	// Determine company ID of webservice user to show permissions only if WS permissions are enabled for his company!
        	final int companyIdOfWebserviceUser = webserviceUser.getCompanyId();
        	
        	
            model.addAttribute("webserviceUserForm", userForm);
            model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");

            if(this.configService.getBooleanValue(ConfigValue.WebserviceEnablePermissions, companyIdOfWebserviceUser)) {
            	final WebservicePermissions permissions = this.webservicePermissionService.listAllPermissions();
            	final WebservicePermissionGroups permissionGroups = this.webservicePermissionGroupService.listAllPermissionGroups();
           	
	            model.addAttribute("PERMISSIONS", permissions);
	            model.addAttribute("PERMISSION_GROUPS", permissionGroups);
	            model.addAttribute("PERMISSIONS_ENABLED", true);
            } else {
	            model.addAttribute("PERMISSIONS_ENABLED", false);
            }

            writeUserActivityLog(admin, "view webservice user", getWsUserDescription(userForm));

            return "webserviceuser_view";
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot obtain webservice user. " + e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        } catch (Exception e) {
            logger.error("Cannot obtain webservice user. " + e.getMessage());
            popups.alert("error.default.message");
        }

        return "messages";
    }

    private String getWsUserDescription(WebserviceUserForm userForm) {
        return String.format("%s - companyID %d", userForm.getUserName(), userForm.getCompanyId());
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), logger);
    }
}
