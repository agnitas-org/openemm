/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupForm;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/administration/usergroup")
@PermissionMapping("user.group")
public class UserGroupController implements XssCheckAware {

    private static final Logger logger = Logger.getLogger(UserGroupController.class);

    public static final int ROOT_ADMIN_ID = PermissionsOverviewData.ROOT_ADMIN_ID;

    public static final int NEW_USER_GROUP_ID = -1;

    private UserGroupService userGroupService;

    private WebStorage webStorage;

    private ConfigService configService;

    private UserActivityLogService userActivityLogService;

    private ConversionService conversionService;
    
    public UserGroupController(UserGroupService userGroupService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService, ConversionService conversionService) {
        this.userGroupService = userGroupService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
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
        AdminGroup adminGroup = userGroupService.getAdminGroup(userGroupId, admin.getCompanyID());
		if (model.containsAttribute("userGroupForm")) {
			form = (UserGroupForm) model.asMap().get("userGroupForm");
		} else {
			UserGroupDto userGroup = userGroupService.getUserGroup(admin, userGroupId);
            if (Objects.nonNull(userGroup)) {
                form = conversionService.convert(userGroup, UserGroupForm.class);
            }
        }

        if (form == null) {
            return "redirect:/administration/usergroup/create.action";
        }

        logger.info("loadAdmin: admin " + userGroupId + " loaded");
        userActivityLogService.writeUserActivityLog(admin, "admin group view", getDescription(form), logger);

        model.addAttribute("userGroupForm", form);
        model.addAttribute("currentCompanyId", admin.getCompanyID());
        int groupIdToEdit = form.getId();
        model.addAttribute("availableAdminGroups", userGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), adminGroup).stream().filter(group -> group.getGroupID() != groupIdToEdit).collect(Collectors.toList()));
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
    public String save(ComAdmin admin, UserGroupForm form, Popups popups) throws Exception {
        int userGroupId = form.getId();
        boolean isNew = userGroupId == NEW_USER_GROUP_ID;

        if (!isValid(admin, form, popups)) {
            return "messages";
        }
        
        boolean isAvailableToCreateOrUpdate = isNew ||
                form.getCompanyId() == admin.getCompanyID() ||
                admin.getAdminID() == ROOT_ADMIN_ID;
        
        if (isAvailableToCreateOrUpdate) {
            UserGroupDto oldUserGroup = userGroupService.getUserGroup(admin, userGroupId);

            int savedUsergroupId;
			try {
				savedUsergroupId = userGroupService.saveUserGroup(admin, conversionService.convert(form, UserGroupDto.class));
			} catch (Exception e) {
	        	popups.field("parentGroupIDs", "error.usergroup.save.message", e.getMessage());
	            return "messages";
			}
            if (savedUsergroupId > 0) {
                form.setId(savedUsergroupId);
                
                popups.success("default.changes_saved");
                userActivityLogService.writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "user group", getDescription(form), logger);

                UserGroupDto userGroup = userGroupService.getUserGroup(admin, savedUsergroupId);
                Set<String> savedPermissions = userGroup.getGrantedPermissions();

                Set<String> oldPermissions = oldUserGroup != null ? oldUserGroup.getGrantedPermissions() : Collections.emptySet();
                List<String> removedPermissions = ListUtils.removeAll(oldPermissions, savedPermissions);
                List<String> addedPermissions = ListUtils.removeAll(savedPermissions, oldPermissions);

                writeUserGroupPermissionChangesLog(admin, form, addedPermissions, removedPermissions);
                return "redirect:/administration/usergroup/list.action";
            } else {
                logger.error("Cannot save userGroup with ID: " + userGroupId);
                popups.alert(isNew ? "error.usergroup.save": "error.group.change.permission");
            }
        }

        return "redirect:/administration/usergroup/list.action";
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
        List<String> groupNames = userGroupService.getGroupNamesUsingGroup(userGroupId, admin.getCompanyID());
        if(!adminNames.isEmpty()) {
            String adminNamesString = StringUtils.abbreviate(StringUtils.join(adminNames, ", "), 64);
            popups.alert("error.group.delete.hasAdmins", adminNamesString);
        } else if(!groupNames.isEmpty()) {
            String groupNamesString = StringUtils.abbreviate(StringUtils.join(groupNames, ", "), 64);
            popups.alert("error.group.delete.hasGroups", groupNamesString);

        } else if(userGroupService.deleteUserGroup(userGroupId, admin)) {
            popups.success("default.selection.deleted");

            userActivityLogService.writeUserActivityLog(admin, "delete user group", getDescription(form), logger);
            logger.info("UserGroup " + userGroupId + " deleted");

        } else {
            popups.alert("Error");
        }

        return "redirect:/administration/usergroup/list.action";
    }

    @GetMapping("/{id:\\d+}/copy.action")
    public String copy(@PathVariable int id, ComAdmin admin, Popups popups) {
        int copiedGroupId = tryCopyUserGroup(id, admin);
        
        if (copiedGroupId < 0) {
            logger.error("Couldn't copy user group ID:" + id);
            popups.alert("Error");
            return "redirect:/administration/usergroup/" + id + "/view.action";
        }
        userActivityLogService.writeUserActivityLog(admin, "copied user group", 
                String.format("User group ID = %d - copied from ID = %d)", copiedGroupId, id));
        return "redirect:/administration/usergroup/" + copiedGroupId + "/view.action";
    }

    private boolean isValid(ComAdmin admin, UserGroupForm form, Popups popups) {
        int userGroupId = form.getId();

        if(!admin.permissionAllowed(Permission.ROLE_CHANGE)) {
            throw new PermissionDeniedDataAccessException("Missing permission to change rights", null);
        }

        String shortname = form.getShortname();
        if (StringUtils.isBlank(shortname)) {
            popups.field("shortname", "error.name.is.empty");
            return false;
        }
    
        if (StringUtils.length(shortname) < 3) {
            popups.field("shortname", "error.name.too.short");
            return false;
        }
    
        if (StringUtils.length(shortname) > 100) {
            popups.field("shortname", "error.username.tooLong");
            return false;
        }

        if(!userGroupService.isShortnameUnique(shortname, userGroupId, form.getCompanyId())) {
            popups.field("shortname", "error.usergroup.duplicate");
            return false;
        }

        return  true;
    }

    private void loadPermissionsSettings(ComAdmin admin, int groupId, int groupCompanyId, Model model) {
        Map<String, PermissionsOverviewData.PermissionCategoryEntry> permissionCategories = userGroupService.getPermissionOverviewData(admin, groupId, groupCompanyId);
        List<PermissionsOverviewData.PermissionCategoryEntry> list = new ArrayList<>(permissionCategories.values());
        Collections.sort(list);
        model.addAttribute("permissionCategories", list);
    }

    private String getDescription(String shortname, int userGroupId, int userGroupCompanyId) {
        return String.format("%s (id - %d, companyId - %d)", shortname, userGroupId, userGroupCompanyId);
    }

    private String getDescription(UserGroupForm form) {
        return getDescription(form.getShortname(), form.getId(), form.getCompanyId());
    }

    private void writeUserGroupPermissionChangesLog(ComAdmin admin, UserGroupForm form, List<String> addedPermissions, List<String> removedPermissions) {
        try {
            List<String> permissionCategories = userGroupService.getUserGroupPermissionCategories(form.getId(), form.getCompanyId(), admin);
            int userGroupId = form.getId();
            String userGroupName = form.getShortname();
            
            List<UserAction> addedPermissionUserActions = getPermissionsChangesLog(userGroupId, userGroupName,
                    addedPermissions, permissionCategories, true);
            addedPermissionUserActions.forEach(action -> writeUserActivityLog(admin, action));
            
            List<UserAction> removedPermissionUserActions = getPermissionsChangesLog(userGroupId, userGroupName,
                    removedPermissions, permissionCategories, false);
            removedPermissionUserActions.forEach(action -> writeUserActivityLog(admin, action));
            
            if (logger.isInfoEnabled()) {
                logger.info("Log User Group: edit user group permissions");
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log User Group permissions save error" + e);
            }
        }
    }
    
    private List<UserAction> getPermissionsChangesLog(int userGroupId, String userGroupName, List<String> changedPermissions, List<String> permissionCategories, boolean isAdded) {
        List<UserAction> userActions = new ArrayList<>();
        
        if (CollectionUtils.isEmpty(changedPermissions) || CollectionUtils.isEmpty(permissionCategories)) {
            return userActions;
        }
       
        for (String category : permissionCategories) {
            List<String> permissionsForCategory = Permission.getAllSystemPermissions().stream()
                    .filter(permission -> StringUtils.equals(permission.getCategory(), category))
                    .filter(Permission::isVisible)
                    .map(Permission::getTokenString)
                    .filter(changedPermissions::contains)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(permissionsForCategory)) {
                UserAction userAction = new UserAction("edit user group permissions in category: " + category,
                        getPermissionChangesDescription(userGroupId, userGroupName, isAdded, permissionsForCategory));
                userActions.add(userAction);
            }
        }
        return userActions;
    }
    
    private String getPermissionChangesDescription(int userGroupId, String userGroupName, boolean isAdded, List<String> changedPermissions) {
        return String.format("Usergroup: \"%s\"(%d). Permissions were %s: %s",
                                userGroupName,  userGroupId, (isAdded ? "activated" : "deactivated"),
                                StringUtils.join(changedPermissions, ", "));
    }
    
    private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }
    
    private int tryCopyUserGroup(int id, ComAdmin admin) {
        try {
            return userGroupService.copyUserGroup(id, admin);
        } catch (Exception e) {
            logger.error("Error while copying the user group ID = " + id, e);
            return NEW_USER_GROUP_ID;
        }
    }
}
