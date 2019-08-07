/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.validation.Valid;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.company.service.ComCompanyService;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupForm;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.emm.core.usergroup.service.impl.UserGroupServiceImpl;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/administration/usergroup")
@PermissionMapping("user.group")
public class UserGroupController {

    private static final Logger logger = Logger.getLogger(UserGroupController.class);

    public static final int ROOT_ADMIN_ID = UserGroupServiceImpl.ROOT_ADMIN_ID;

    public static final int ROOT_GROUP_ID = UserGroupServiceImpl.ROOT_GROUP_ID;

    public static final int NEW_USER_GROUP_ID = -1;

    private UserGroupService userGroupService;

    private ComCompanyService companyService;

    private WebStorage webStorage;

    private ConfigService configService;

    private UserActivityLogService userActivityLogService;

    private ConversionService conversionService;

    private final PermissionFilter permissionFilter;

    public UserGroupController(UserGroupService userGroupService, ComCompanyService companyService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService, ConversionService conversionService, final PermissionFilter permissionFilter) {
        this.userGroupService = userGroupService;
        this.companyService = companyService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.permissionFilter = permissionFilter;
    }

    @RequestMapping("/list.action")
    public String list(ComAdmin admin, @ModelAttribute("userGroupListForm") PaginationForm form, Model model, Popups popups) {
        PaginatedListImpl<UserGroupDto> userGroupList;
		try {
			FormUtils.syncNumberOfRows(webStorage, ComWebStorage.USER_GROUP_OVERVIEW, form);

			userGroupList = userGroupService.getUserGroupPaginatedList(admin, form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows());

			userActivityLogService.writeUserActivityLog(admin, "user group", "active tab - user groups", logger);
		} catch (Exception e) {
			userGroupList = new PaginatedListImpl<>(Collections.emptyList(), 0, form.getNumberOfRows(), form.getPage(), form.getSort(), form.getOrder());
			logger.error("admin: " + e, e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		model.addAttribute("userGroupList", userGroupList);
		model.addAttribute("currentCompanyId", admin.getCompanyID());

        return "settings_usergroup_list";
    }

    @RequestMapping("/{id:-?[0-9]\\d*}/view.action")
    public String view(ComAdmin admin, @PathVariable("id") int userGroupId, Model model) {
        if(userGroupId == NEW_USER_GROUP_ID) {
            return "redirect:/administration/usergroup/create.action";
        }

        UserGroupForm form = null;
		if(model.asMap().containsKey("userGroupForm")) {
			form = (UserGroupForm) model.asMap().get("userGroupForm");
		} else {
            UserGroupDto userGroup = userGroupService.getUserGroup(admin, userGroupId);
            if (Objects.nonNull(userGroup)) {
                form = conversionService.convert(userGroup, UserGroupForm.class);
            }
        }

        if(form == null) {
            return "redirect:/administration/usergroup/create.action";
        }

        logger.info("loadAdmin: admin " + userGroupId + " loaded");
        userActivityLogService.writeUserActivityLog(admin, "admin group view", getDescription(form), logger);

        model.addAttribute("userGroupForm", form);
        model.addAttribute("currentCompanyId", admin.getCompanyID());
        loadPermissionsSettings(admin, form.getId(), form.getCompanyId(), model);

        return "settings_usergroup_view";
    }

    @GetMapping("/create.action")
    public String create(ComAdmin admin, UserGroupForm form, Model model) {
        int companyId = admin.getCompanyID();

        form.setId(NEW_USER_GROUP_ID);
        form.setCompanyId(companyId);
        loadPermissionsSettings(admin, NEW_USER_GROUP_ID, companyId, model);

        model.addAttribute("currentCompanyId", admin.getCompanyID());
        return "settings_usergroup_view";
    }

    @PostMapping("/save.action")
    public String save(ComAdmin admin, @Valid UserGroupForm form, BindingResult result, RedirectAttributes model, Popups popups) throws Exception {
        int userGroupId = form.getId();
        boolean isNew = userGroupId == NEW_USER_GROUP_ID;

        if(!isValid(admin, form, result, popups)) {
            model.addFlashAttribute("userGroupForm", form);
            return "redirect:/administration/usergroup/" + userGroupId + "/view.action";
        }

        if(isNew || form.getCompanyId() == admin.getCompanyID() || admin.getAdminID() == ROOT_ADMIN_ID) {
            UserGroupDto oldUserGroup = userGroupService.getUserGroup(admin, userGroupId);

            int savedUsergroupId = userGroupService.saveUserGroup(admin, conversionService.convert(form, UserGroupDto.class));
            if (savedUsergroupId >= 0) {
                userGroupId = savedUsergroupId;
                popups.success("default.changes_saved");
                userActivityLogService.writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "user group", getDescription(form), logger);

                UserGroupDto userGroup = userGroupService.getUserGroup(admin, savedUsergroupId);
                Set<String> savedPermissions = userGroup.getGrantedPermissions();

                Set<String> oldPermissions = oldUserGroup != null ? oldUserGroup.getGrantedPermissions() : Collections.emptySet();
                List<String> removedPermissions = ListUtils.removeAll(oldPermissions, savedPermissions);
                List<String> addedPermissions = ListUtils.removeAll(savedPermissions, oldPermissions);

                if (!addedPermissions.isEmpty()) {
					userActivityLogService.writeUserActivityLog(admin, "edit user group permissions", getPermissionDescription(userGroup, addedPermissions, true), logger);
				}
				if (!removedPermissions.isEmpty()) {
					userActivityLogService.writeUserActivityLog(admin, "edit user group permissions", getPermissionDescription(userGroup, removedPermissions, false), logger);
				}
            } else {
                logger.error("Cannot save userGroup with ID:" + userGroupId);
                popups.alert(isNew ? "error.usergroup.save": "error.group.change.permission");
            }
        }

        return "redirect:/administration/usergroup/" + userGroupId + "/view.action";
    }

    @GetMapping("/{id:-?[0-9]\\d*}/confirmDelete.action")
    public String confirmDelete(ComAdmin admin, @PathVariable("id") int userGroupId, Model model, Popups popups) {
        UserGroupDto userGroup = userGroupService.getUserGroup(admin, userGroupId);
        boolean hasError = false;
        if(userGroup != null && userGroup.getUserGroupId() > NEW_USER_GROUP_ID) {
            model.addAttribute("userGroupForm", conversionService.convert(userGroup, UserGroupForm.class));
        } else {
            popups.alert("Error");
            hasError = true;
        }

        model.addAttribute("excludeDialog", hasError);

        return "settings_usergroup_delete";
    }

    @PostMapping("/delete.action")
    public String delete(ComAdmin admin, UserGroupForm form, Popups popups) {
        int userGroupId = form.getId();
        List<String> adminNames = userGroupService.getAdminNamesOfGroup(userGroupId, admin.getCompanyID());
        if(!adminNames.isEmpty()) {
            String adminNamesString = StringUtils.abbreviate(StringUtils.join(adminNames, ", "), 64);
            popups.alert("error.group.delete.hasAdmins", adminNamesString);

        } else if(userGroupService.deleteUserGroup(userGroupId, admin)) {
            popups.success("default.selection.deleted");

            userActivityLogService.writeUserActivityLog(admin, "delete user group", getDescription(form), logger);
            logger.info("UserGroup " + userGroupId + " deleted");

        } else {
            popups.alert("Error");
        }

        return "redirect:/administration/usergroup/list.action";
    }

    private boolean isValid(ComAdmin admin, UserGroupForm form, BindingResult result, Popups popups) throws Exception {
        int userGroupId = form.getId();

        if(!admin.permissionAllowed(Permission.ROLE_CHANGE)) {
            throw new Exception("Missing permission to change rights", null);
        }

        if(result.hasErrors()) {
            return false;
        }

        if(!userGroupService.isShortnameUnique(form.getShortname(), userGroupId, form.getCompanyId())) {
            popups.field("shortname", "error.usergroup.duplicate");
            return false;
        }

        return  true;
    }

    private void loadPermissionsSettings(ComAdmin admin, int groupId, int groupCompanyId, Model model) {
        Map<String, Map<String, Set<String>>> permissionCategoriesMap = new HashMap<>();
        Set<String> permissionChangeable = new HashSet<>();
        List<String> permissionCategories = userGroupService.getUserGroupPermissionCategories(groupId, groupCompanyId, admin);

        if(groupId == ROOT_GROUP_ID || groupCompanyId == admin.getCompanyID() || groupCompanyId == admin.getCompany().getCreatorID() || admin.getAdminID() == ROOT_ADMIN_ID) {
            Set<Permission> companyPermissions = companyService.getCompanyPermissions(groupCompanyId);
            for(Permission permission: Permission.getAllPermissionsAndCategories().keySet()) {
            	if(permissionFilter.isVisible(permission)) {
	                String permissionCategory = permission.getCategory();
	                if(permissionCategories.contains(permissionCategory)) {
	                    String category = permission.getCategory();
	                    String subCategory = StringUtils.defaultIfEmpty(permission.getSubCategory(), "");
	                    String permissionName = permission.toString();

	                    permissionCategoriesMap
	                            .computeIfAbsent(category, (key) -> new TreeMap<>())
	                            .computeIfAbsent(subCategory, (key) -> new TreeSet<>())
	                            .add(permissionName);


	                    if(userGroupService.isUserGroupPermissionChangeable(admin, permission, companyPermissions)) {
	                        permissionChangeable.add(permissionName);
	                    }
	                }
            	}
            }
        }

        model.addAttribute("permissionCategoryList", permissionCategories);
        model.addAttribute("permissionCategoriesMap", permissionCategoriesMap);
        model.addAttribute("permissionChangeable", permissionChangeable);
    }

    private String getDescription(String shortname, int userGroupId, int userGroupCompanyId) {
        return String.format("%s (id - %d, companyId - %d)", shortname, userGroupId, userGroupCompanyId);
    }

    private String getDescription(UserGroupForm form) {
        return getDescription(form.getShortname(), form.getId(), form.getCompanyId());
    }

    private String getPermissionDescription(UserGroupDto userGroupDto, List<String> permissions, boolean isAdded) {
        return String.format("Usergroup: \"%s\"(%d). %s permissions: %s",
                userGroupDto.getShortname(),
                userGroupDto.getUserGroupId(),
                (isAdded ? "Added" : "Removed"),
                StringUtils.join(permissions, ","));
    }

}
