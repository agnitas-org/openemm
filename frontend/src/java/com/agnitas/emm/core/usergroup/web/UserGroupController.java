/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.web;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupForm;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;
import com.agnitas.emm.core.usergroup.form.UserGroupSearchParams;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.beans.AdminGroup;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.PermissionDeniedDataAccessException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/administration/usergroup")
@PermissionMapping("user.group")
@SessionAttributes(types = UserGroupSearchParams.class)
public class UserGroupController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(UserGroupController.class);

    private static final String REDIRECT_TO_OVERVIEW = "redirect:/administration/usergroup/list.action?restoreSort=true";

    public static final int ROOT_ADMIN_ID = PermissionsOverviewData.ROOT_ADMIN_ID;
    public static final int ROOT_COMPANY_ID = 1;

    public static final int NEW_USER_GROUP_ID = -1;

    private final UserGroupService userGroupService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final ConversionService conversionService;
    private final PermissionDao permissionDao;

    public UserGroupController(UserGroupService userGroupService, WebStorage webStorage, UserActivityLogService userActivityLogService,
                               ConversionService conversionService, PermissionDao permissionDao) {
        this.userGroupService = userGroupService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.permissionDao = permissionDao;
    }

    @RequestMapping("/list.action")
    public String list(Admin admin, @ModelAttribute("userGroupListForm") UserGroupOverviewFilter form, UserGroupSearchParams searchParams,
                       @RequestParam(required = false) Boolean restoreSort, Model model) {
        if (admin.isRedesignedUiUsed()) {
            form.setCurrentAdminId(admin.getAdminID());
            form.setCurrentAdminCompanyId(admin.getCompanyID());
            FormUtils.syncSearchParams(searchParams, form, true);
        }

        FormUtils.syncPaginationData(webStorage, WebStorage.USER_GROUP_OVERVIEW, form, restoreSort);

        model.addAttribute("userGroupList", admin.isRedesignedUiUsed()
                ? userGroupService.overview(form)
                : userGroupService.getUserGroupPaginatedList(admin, form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows())
        );

        userActivityLogService.writeUserActivityLog(admin, "user group", "active tab - user groups", logger);
        return "settings_usergroup_list";
    }

    @PostMapping("/restore.action")
    public String restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);
        userGroupService.restore(bulkIds, admin.getCompanyID());
        popups.success(CHANGES_SAVED_MSG);
        return REDIRECT_TO_OVERVIEW + "&showDeleted=true";
    }

    @RequestMapping("/{id:-?[0-9]\\d*}/view.action")
    public String view(Admin admin, @PathVariable("id") int userGroupId, Model model) {
        if (userGroupId == NEW_USER_GROUP_ID) {
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
                model.addAttribute("usersCount", userGroup.getUsersCount());
            }
        }

        if (form == null) {
            return "redirect:/administration/usergroup/create.action";
        }

        logger.info("loadAdmin: admin {} loaded", userGroupId);
        userActivityLogService.writeUserActivityLog(admin, "admin group view", getDescription(form), logger);

        model.addAttribute("userGroupForm", form);
        int groupIdToEdit = form.getId();
        model.addAttribute("availableAdminGroups", userGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), adminGroup).stream().filter(group -> group.getGroupID() != groupIdToEdit).collect(Collectors.toList()));
        loadPermissionsSettings(admin, form.getId(), form.getCompanyId(), model);

        return "settings_usergroup_view";
    }

    @GetMapping("/search.action")
    public String search(UserGroupOverviewFilter listForm, UserGroupSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, listForm, false);
        ra.addFlashAttribute("userGroupListForm", listForm);
        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping("/create.action")
    public String create(Admin admin, UserGroupForm form, Model model) {
        int companyId = admin.getCompanyID();

        form.setId(NEW_USER_GROUP_ID);
        form.setCompanyId(companyId);
        loadPermissionsSettings(admin, NEW_USER_GROUP_ID, companyId, model);

        model.addAttribute("currentCompanyId", admin.getCompanyID());
        return "settings_usergroup_view";
    }

    @PostMapping("/save.action")
    public String save(Admin admin, UserGroupForm form, Popups popups) {
        int userGroupId = form.getId();
        boolean isNew = userGroupId == NEW_USER_GROUP_ID;

        if (!isValid(admin, form, popups)) {
            return MESSAGES_VIEW;
        }

        boolean isAvailableToCreateOrUpdate = isNew ||
                form.getCompanyId() == admin.getCompanyID() ||
                admin.getAdminID() == ROOT_ADMIN_ID;

        if (isAvailableToCreateOrUpdate) {
            UserGroupDto oldUserGroup = userGroupService.getUserGroup(admin, userGroupId);

            int savedUserGroupId;
            try {
                savedUserGroupId = userGroupService.saveUserGroup(admin, conversionService.convert(form, UserGroupDto.class));
            } catch (Exception e) {
                popups.alert("error.usergroup.save.message", e.getMessage());
                return MESSAGES_VIEW;
            }
            if (savedUserGroupId > 0) {
                form.setId(savedUserGroupId);

                popups.success("default.changes_saved");
                userActivityLogService.writeUserActivityLog(admin, (isNew ? "create " : "edit ") + "user group", getDescription(form), logger);

                UserGroupDto userGroup = userGroupService.getUserGroup(admin, savedUserGroupId);
                Set<String> savedPermissions = userGroup.getGrantedPermissions();

                Set<String> oldPermissions = oldUserGroup != null ? oldUserGroup.getGrantedPermissions() : Collections.emptySet();
                List<String> removedPermissions = ListUtils.removeAll(oldPermissions, savedPermissions);
                List<String> addedPermissions = ListUtils.removeAll(savedPermissions, oldPermissions);

                writeUserGroupPermissionChangesLog(admin, form, addedPermissions, removedPermissions);
                return REDIRECT_TO_OVERVIEW;
            } else {
                logger.error("Cannot save userGroup with ID: " + userGroupId);
                popups.alert(isNew ? "error.usergroup.save" : "error.group.change.permission");
            }
        }

        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping("/{id:-?[0-9]\\d*}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String confirmDelete(Admin admin, @PathVariable("id") int userGroupId, Model model, Popups popups) {
        UserGroupDto userGroup = userGroupService.getUserGroup(admin, userGroupId);
        boolean hasError = false;
        if (userGroup != null && userGroup.getUserGroupId() > NEW_USER_GROUP_ID) {
            model.addAttribute("userGroupForm", conversionService.convert(userGroup, UserGroupForm.class));
        } else {
            popups.alert(ERROR_MSG);
            hasError = true;
        }

        model.addAttribute("excludeDialog", hasError);

        return "settings_usergroup_delete";
    }

    @PostMapping("/delete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String delete(Admin admin, UserGroupForm form, Popups popups) {
        int userGroupId = form.getId();

        List<String> adminNames = userGroupService.getAdminNamesOfGroup(userGroupId, admin.getCompanyID());
        List<String> groupNames = userGroupService.getGroupNamesUsingGroup(userGroupId, admin.getCompanyID());

        if (!adminNames.isEmpty()) {
            String adminNamesString = StringUtils.abbreviate(StringUtils.join(adminNames, ", "), 64);
            popups.alert("error.group.delete.hasAdmins", adminNamesString);
        } else if (!groupNames.isEmpty()) {
            String groupNamesString = StringUtils.abbreviate(StringUtils.join(groupNames, ", "), 64);
            popups.alert("error.group.delete.hasGroups", groupNamesString);

        } else if (userGroupService.deleteUserGroup(userGroupId, admin)) {
            popups.success(SELECTION_DELETED_MSG);

            userActivityLogService.writeUserActivityLog(admin, "delete user group", getDescription(form), logger);
            logger.info("UserGroup " + userGroupId + " deleted");

        } else {
            popups.alert(ERROR_MSG);
        }

        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping(value = "/deleteRedesigned.action")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateSelectedIds(bulkIds);

        ServiceResult<List<UserGroupDto>> result = userGroupService.getAllowedGroupsForDeletion(bulkIds, admin);
        popups.addPopups(result);
        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(UserGroupDto::getShortname).toList(),
                "settings.usergroup.delete", "settings.usergroup.delete.question",
                "bulkAction.delete.usergroup", "bulkAction.delete.usergroup.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateSelectedIds(bulkIds);

        List<UserGroupDto> userGroups = userGroupService.markDeleted(bulkIds, admin);
        writeUserActivityLog(admin, getDeleteUserActionLog(userGroups));
        logger.info("User groups {} deleted", bulkIds);
        popups.success(SELECTION_DELETED_MSG);
        return REDIRECT_TO_OVERVIEW;
    }

    private static void validateSelectedIds(Collection<Integer> bulkIds) {
        if (CollectionUtils.isEmpty(bulkIds)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    @GetMapping("/{id:\\d+}/copy.action")
    public String copy(@PathVariable int id, Admin admin, Popups popups) {
        int copiedGroupId = tryCopyUserGroup(id, admin);

        if (copiedGroupId < 0) {
            logger.error("Couldn't copy user group ID:" + id);
            popups.alert(ERROR_MSG);
            return "redirect:/administration/usergroup/" + id + "/view.action";
        }
        userActivityLogService.writeUserActivityLog(admin, "copied user group",
                String.format("User group ID = %d - copied from ID = %d)", copiedGroupId, id));
        return "redirect:/administration/usergroup/" + copiedGroupId + "/view.action";
    }

    private boolean isValid(Admin admin, UserGroupForm form, Popups popups) {
        int userGroupId = form.getId();

        if (!admin.permissionAllowed(Permission.ROLE_CHANGE)) {
            throw new PermissionDeniedDataAccessException("Missing permission to change rights", null);
        }

        String shortname = form.getShortname();
        if (StringUtils.trimToNull(shortname) == null) {
            popups.fieldError("shortname", "error.name.is.empty");
            return false;
        } else if (StringUtils.trimToNull(shortname).length() < 3) {
            popups.fieldError("shortname", "error.name.too.short");
            return false;
        } else if (StringUtils.length(shortname) > 100) {
            popups.fieldError("shortname", "error.username.tooLong");
            return false;
        }

        if (!userGroupService.isShortnameUnique(shortname, userGroupId, form.getCompanyId())) {
            popups.fieldError("shortname", "error.usergroup.duplicate");
            return false;
        }

        return true;
    }

    private void loadPermissionsSettings(Admin admin, int groupId, int groupCompanyId, Model model) {
        Map<String, PermissionsOverviewData.PermissionCategoryEntry> permissionCategories = userGroupService.getPermissionOverviewData(admin, groupId, groupCompanyId);
        List<PermissionsOverviewData.PermissionCategoryEntry> list = new ArrayList<>(permissionCategories.values());
        Collections.sort(list);
        model.addAttribute("permissionCategories", list);
    }

    private UserAction getDeleteUserActionLog(List<UserGroupDto> userGroups) {
        return new UserAction("delete user groups", "[" + userGroups.stream()
                .map(userGroup -> getDescription(userGroup.getShortname(), userGroup.getUserGroupId(), userGroup.getCompanyId()))
                .collect(Collectors.joining(", ")) + "]");
    }

    private String getDescription(String shortname, int userGroupId, int userGroupCompanyId) {
        return String.format("%s (id - %d, companyId - %d)", shortname, userGroupId, userGroupCompanyId);
    }

    private String getDescription(UserGroupForm form) {
        return getDescription(form.getShortname(), form.getId(), form.getCompanyId());
    }

    private void writeUserGroupPermissionChangesLog(Admin admin, UserGroupForm form, List<String> addedPermissions, List<String> removedPermissions) {
        try {
            int userGroupId = form.getId();
            String userGroupName = form.getShortname();

            if (!CollectionUtils.isEmpty(addedPermissions)) {
                List<UserAction> addedPermissionUserActions = getPermissionsChangesLog(userGroupId, userGroupName, addedPermissions, true);
                addedPermissionUserActions.forEach(action -> writeUserActivityLog(admin, action));
            }

            if (!CollectionUtils.isEmpty(removedPermissions)) {
                List<UserAction> removedPermissionUserActions = getPermissionsChangesLog(userGroupId, userGroupName, removedPermissions, false);
                removedPermissionUserActions.forEach(action -> writeUserActivityLog(admin, action));
            }
            if (logger.isInfoEnabled()) {
                logger.info("Log User Group: edit user group permissions");
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log User Group permissions save error" + e);
            }
        }
    }

    private List<UserAction> getPermissionsChangesLog(int userGroupId, String userGroupName, List<String> changedPermissions, boolean isAdded) {
        List<UserAction> userActions = new ArrayList<>();

        if (CollectionUtils.isEmpty(changedPermissions)) {
            return userActions;
        }

        Map<String, PermissionInfo> permissionsInfo = permissionDao.getPermissionInfos();
        Map<String, List<String>> changedPermissionGroups = new HashMap<>();

        for (String permissionToken : changedPermissions) {
            PermissionInfo info = permissionsInfo.get(permissionToken);
            if (info != null) {
                changedPermissionGroups.computeIfAbsent(info.getCategory(), category -> new ArrayList<>())
                        .add(permissionToken);
            }
        }
        changedPermissionGroups.forEach((category, categoryPermissionsList) -> {
            UserAction userAction = new UserAction("Edit user group permissions in category: " + category,
                    constructChangesDescription(userGroupId, userGroupName, isAdded, categoryPermissionsList));
            userActions.add(userAction);
        });

        return userActions;
    }

    private String constructChangesDescription(int userGroupId, String userGroupName, boolean isAdded, List<String> changedPermissions) {
        return String.format("Usergroup: \"%s\"(%d). Permissions were %s: %s",
                userGroupName, userGroupId, (isAdded ? "activated" : "deactivated"),
                StringUtils.join(changedPermissions, ", "));
    }

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }

    private int tryCopyUserGroup(int id, Admin admin) {
        try {
            return userGroupService.copyUserGroup(id, admin);
        } catch (Exception e) {
            logger.error("Error while copying the user group ID = %d".formatted(id), e);
            return NEW_USER_GROUP_ID;
        }
    }
}
