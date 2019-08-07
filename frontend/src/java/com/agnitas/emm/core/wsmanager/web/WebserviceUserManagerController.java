/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.web;

import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserForm;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserListForm;
import com.agnitas.emm.wsmanager.common.UnknownWebserviceUsernameException;
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

    private static final String COMPANY_LIST = "companyList";
    private static final String WS_USER_LIST = "webserviceUserList";
    private static final String WS_USER_FORM = "webserviceUserForm";

    private static final String LIST_VIEW = "webserviceuser_list";
    private static final String USER_VIEW = "webserviceuser_view";
    private static final String REDIRECT_LIST_URL = "redirect:/administration/wsmanager/users.action";
    private static final String REDIRECT_VIEW_URL = "redirect:/administration/wsmanager/user/%s/view.action";

    private ConfigService configService;
    private WebserviceUserService webserviceUserService;
    private CompanyService companyService;
    private ConversionService conversionService;
    private WebStorage webStorage;
    private UserActivityLogService userActivityLogService;

    public WebserviceUserManagerController(ConfigService configService,
    										WebserviceUserService webserviceUserService,
                                           CompanyService companyService,
                                           ConversionService conversionService,
                                           WebStorage webStorage,
                                           UserActivityLogService userActivityLogService) {
        this.configService = configService;
        this.webserviceUserService = webserviceUserService;
        this.companyService = companyService;
        this.conversionService = conversionService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping(value = "/users.action")
    public String list(ComAdmin admin, WebserviceUserListForm userListForm, Model model) throws WebserviceUserServiceException {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.WS_MANAGER_OVERVIEW, userListForm);

        model.addAttribute(WS_USER_LIST,
                webserviceUserService.getPaginatedWSUserList(admin.getCompanyID(),
                        userListForm.getSort(),
                        userListForm.getOrder(),
                        userListForm.getPage(),
                        userListForm.getNumberOfRows(),
                        admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)));

        if (!model.containsAttribute(WS_USER_FORM)) {
            model.addAttribute(WS_USER_FORM, new WebserviceUserForm());
        }

        model.addAttribute(COMPANY_LIST, companyService.getActiveOwnCompanyEntries(admin.getCompanyID()));

        writeUserActivityLog(admin, "webservice manager", "active tab - manage webservice user");

        return LIST_VIEW;
    }

    @PostMapping(value = "/user/new.action")
    public String create(ComAdmin admin,
                             @Validated({WebserviceUserForm.ValidationStepOne.class, WebserviceUserForm.ValidationStepTwo.class}) WebserviceUserForm userForm,
                             BindingResult result, RedirectAttributes model, Popups popups) {

        saveWebserviceUser(admin, true, userForm, result, model, popups);
        return REDIRECT_LIST_URL;
    }

    @PostMapping(value = "/user/save.action")
    public String save(ComAdmin admin, @Validated({WebserviceUserForm.ValidationEditUser.class}) WebserviceUserForm userForm, BindingResult result, RedirectAttributes model, Popups popups) {
        boolean savingIsSuccessful = saveWebserviceUser(admin, false, userForm, result, model, popups);
        return savingIsSuccessful ? REDIRECT_LIST_URL : String.format(REDIRECT_VIEW_URL, userForm.getUserName());
    }

    private boolean saveWebserviceUser(ComAdmin admin, boolean isNew, WebserviceUserForm userForm, BindingResult result, RedirectAttributes model, Popups popups) {
        if (result.hasErrors()) {
            model.addFlashAttribute(WS_USER_FORM, userForm);
            popups.alert(isNew ? "error.webserviceuser.cannot_create": "error.webserviceuser.cannot_update");
            return false;
        }
        
        int currentNumberOfWebserviceUsers = webserviceUserService.getNumberOfWebserviceUsers();
        int integerValue = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfWebserviceUsers);
        if (integerValue >= 0 && integerValue <= currentNumberOfWebserviceUsers) {
        	popups.alert("error.numberOfWebserviceUsersExceeded", currentNumberOfWebserviceUsers);
        	return false;
		}

        try {
        	if (!admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)) {
        		userForm.setCompanyId(admin.getCompanyID());
        	}
            webserviceUserService.saveWebServiceUser(conversionService.convert(userForm, WebserviceUserDto.class), isNew);

            popups.success("default.changes_saved");

            writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "webservice user", getWsUserdescription(userForm));
            
        	return true;
        } catch (WebserviceUserAlreadyExistsException e) {
            logger.error("Cannot create webservice user. " + e.getMessage());
            popups.alert("error.webserviceuser.cannot_create");

            popups.field("userName", "error.webserviceuser.already_exists");
            model.addFlashAttribute(WS_USER_FORM, userForm);
        	return false;
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot update webservice user. " + e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        	return false;
        } catch (Exception e) {
            logger.error("Cannot create webservice user. " + e.getMessage());
            popups.alert("error.default.message");
        	return false;
        }
    }

    @GetMapping(value = "/user/{username}/view.action")
    public String view(ComAdmin admin, @PathVariable String username, Model model, Popups popups) {
        try {
            WebserviceUserForm userForm = conversionService.convert(webserviceUserService.getWebserviceUserByUserName(username), WebserviceUserForm.class);
            model.addAttribute(WS_USER_FORM, userForm);

            writeUserActivityLog(admin, "view webservice user", getWsUserdescription(userForm));

            return USER_VIEW;
        } catch (UnknownWebserviceUsernameException e) {
            logger.error("Cannot obtain webservice user. " + e.getMessage());
            popups.alert("error.webserviceuser.unknown_user");
        } catch (Exception e) {
            logger.error("Cannot obtain webservice user. " + e.getMessage());
            popups.alert("error.default.message");
        }

        return REDIRECT_LIST_URL;
    }

    private String getWsUserdescription(WebserviceUserForm userForm) {
        return String.format("%s - companyID %d", userForm.getUserName(), userForm.getCompanyId());
    }

    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description);
    }
}
