/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.form.AdminListForm;
import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.form.AdminRightsForm;
import com.agnitas.emm.core.admin.form.validation.AdminFormValidator;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordCheck;
import com.agnitas.emm.core.commons.password.PasswordCheckHandler;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import com.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import com.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.web.LogonController;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.CSVService;
import com.agnitas.service.PdfService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.Tuple;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class AdminController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(AdminController.class);

    private static final String USERNAME_DUPLICATE_MSG = "error.username.duplicate";
    private static final String REDIRECT_TO_LIST = "redirect:/admin/list.action";
    private static final String SAVE_ERROR_MSG = "error.admin.save";

    protected final ConfigService configService;
    protected final AdminService adminService;
    protected final UserActivityLogService userActivityLogService;
    private final LogonService logonService;
    private final CompanyService companyService;
    private final AdminGroupService adminGroupService;
    private final WebStorage webStorage;
    private final AdminChangesLogService adminChangesLogService;
    private final PasswordCheck passwordCheck;
    private final CSVService csvService;
    private final PdfService pdfService;
    private final ConversionService conversionService;
    protected final TargetService targetService;

    public AdminController(ConfigService configService,
                           AdminService adminService,
                           CompanyService companyService,
                           AdminGroupService adminGroupService, WebStorage webStorage,
                           UserActivityLogService userActivityLogService,
                           AdminChangesLogService adminChangesLogService,
                           PasswordCheck passwordCheck, CSVService csvService, PdfService pdfService,
                           ConversionService conversionService,
                           TargetService targetService,
                           LogonService logonService) {
        this.configService = configService;
        this.adminService = adminService;
        this.companyService = companyService;
        this.adminGroupService = adminGroupService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.adminChangesLogService = adminChangesLogService;
        this.passwordCheck = passwordCheck;
        this.csvService = csvService;
        this.pdfService = pdfService;
        this.conversionService = conversionService;
        this.targetService = targetService;
        this.logonService = logonService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        if (admin != null) {
            binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
        }
    }

    @ModelAttribute("adminForm")
    public AdminForm getSettingsForm() {
        return getNewAdminForm();
    }

    @ModelAttribute
    public AdminListFormSearchParams getAdminFormSearchParams(){
        return new AdminListFormSearchParams();
    }

    protected AdminForm getNewAdminForm() {
        return new AdminForm();
    }

    @RequestMapping("/list.action")
    @RequiredPermission("admin.show")
    public Pollable<ModelAndView> list(Admin admin, AdminListForm form,
                                       Model model, HttpSession session,
                                       @RequestParam(required = false) Boolean restoreSort,
                                       @ModelAttribute AdminListFormSearchParams adminListSearchParams) {
        FormUtils.syncPaginationData(webStorage, WebStorage.ADMIN_OVERVIEW, form, restoreSort);
        adminListSearchParams.restoreParams(form);

        int companyID = admin.getCompanyID();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute("adminEntries", getAdminListFromAdminListForm(admin, form));
            form.setCompanies(companyService.getCreatedCompanies(companyID));
            form.setAdminGroups(adminGroupService.getAdminGroupsByCompanyIdAndDefault(companyID, admin, null));

            return new ModelAndView("settings_admin_list", model.asMap());
        };

        PollingUid pollingUid = PollingUid.builder(session.getId(), "admin-entries")
                .arguments(form.toArray())
                .build();

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, new ModelAndView(REDIRECT_TO_LIST, form.toMap()), worker);
    }

    @GetMapping("/search.action")
    @RequiredPermission("admin.show")
    public String search(AdminListForm form, @ModelAttribute AdminListFormSearchParams searchParams, RedirectAttributes model) {
        searchParams.storeParams(form);
        model.addFlashAttribute("adminListForm", form);

        return REDIRECT_TO_LIST + "?restoreSort=true";
    }

    @RequestMapping("/{adminID}/view.action")
    @RequiredPermission("admin.show")
    public String view(Admin admin, AdminForm form, Popups popups, Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups, REDIRECT_TO_LIST + "?restoreSort=true");
        }
        if (adminToEdit.getGroups() == null || adminToEdit.getGroups().isEmpty()) {
            popups.alert("error.admin.invalidGroup");
        }

        initializeForm(form, adminToEdit, admin);

        LOGGER.info("loadAdmin: admin {} loaded", form.getAdminID());

        userActivityLogService.writeUserActivityLog(admin, "view user", adminToEdit.getUsername());

        loadDataForViewPage(admin, adminToEdit, model);
        model.addAttribute("EDIT_ALTG_ENABLED", canEditAltg(admin, form));

        return "user_view";
    }

    private boolean canEditAltg(Admin admin, AdminForm adminForm) {
    	return admin.getCompanyID() == adminForm.getCompanyID();
    }

    @RequestMapping("/welcome.action")
    @RequiredPermission("admin.sendWelcome")
    public String sendWelcome(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, HttpServletRequest req, Popups popups) {
        validateSelectedIds(bulkIds);

        logonService.sendWelcomeMail(
                bulkIds,
                req.getRemoteAddr(),
                admin.getCompanyID(),
                LogonController.PASSWORD_RESET_LINK_PATTERN
        );

        popups.success("admin.password.sent");
        return MESSAGES_VIEW;
    }

    @RequestMapping("/{adminID}/rights/view.action")
    @RequiredPermission("admin.show")
    public String viewRights(Admin admin, AdminRightsForm form, Popups popups, Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups, REDIRECT_TO_LIST + "?restoreSort=true");
        }

        prepareRightsViewPageData(admin, form, model, adminToEdit);

        return "user_permissions";
    }

    @PostMapping(value = "/{adminID}/save.action")
    @RequiredPermission("admin.change")
    public ModelAndView save(Admin admin, AdminForm form, Popups popups) {
        if (!AdminFormValidator.validate(form, popups)){
        	popups.alert(SAVE_ERROR_MSG);
        	return new ModelAndView(MESSAGES_VIEW, HttpStatus.BAD_REQUEST);
        }

        if (adminUsernameChangedToExisting(form)) {
            popups.alert(USERNAME_DUPLICATE_MSG);
            return new ModelAndView(MESSAGES_VIEW, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(form.getPassword()) || checkPassword(form, popups)) {
            admin.setRestful(false);
            saveAdminAndGetView(form, admin, popups);
            logonService.updateSessionsLanguagesAttributes(admin);
        }

        return new ModelAndView(redirectToView(form.getAdminID()));
    }

    private String redirectToView(int adminId) {
        return "redirect:/admin/" + adminId + "/view.action";
    }

    @PostMapping(value = "/{adminID}/rights/save.action")
    @RequiredPermission("admin.change")
    public String saveRights(Admin admin, AdminRightsForm form, Popups popups) {
        try {
            if (saveAdminRightsAndWriteToActivityLog(admin, form, popups)) {
                popups.changesSaved();
            }

            return "redirect:/admin/" + form.getAdminID() + "/rights/view.action";
        } catch (Exception e) {
            LOGGER.error("Exception saving rights", e);
            popups.alert(SAVE_ERROR_MSG, e);
            return MESSAGES_VIEW;
        }
    }

   @RequestMapping("/create.action")
   @RequiredPermission("admin.new")
    public String create(Admin admin, @ModelAttribute("adminForm") AdminForm form, Model model) {
        loadDataForViewPage(admin, null, model);
        adminService.setDefaultPreferencesSettings(form.getAdminPreferences());

        return "user_view";
    }

    @PostMapping(value = "/saveNew.action")
    @RequiredPermission("admin.new")
    public String saveNew(Admin admin, AdminForm form, Popups popups) {
        final int maximumNumberOfAdmins = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAdmins, admin.getCompanyID());
        if (maximumNumberOfAdmins >= 0 && maximumNumberOfAdmins <= adminService.getNumberOfGuiAdmins(admin.getCompanyID())) {
            popups.alert("error.numberOfAdminsExceeded", maximumNumberOfAdmins);
            return MESSAGES_VIEW;
        }
        form.setAdminID(0);
        if (!AdminFormValidator.validate(form, popups)) {
        	popups.alert(SAVE_ERROR_MSG);
        	return MESSAGES_VIEW;
        }
        if (StringUtils.isBlank(form.getPassword())) {
            popups.alert("error.password.missing");
            return MESSAGES_VIEW;
        }

        if (adminService.adminExists(form.getUsername())) {
            popups.alert(USERNAME_DUPLICATE_MSG);
            return MESSAGES_VIEW;
        }

        if (!checkPassword(form, popups)) {
            return MESSAGES_VIEW;
        }

        admin.setRestful(false);

        return saveAdminAndGetView(form, admin, popups);
    }

    @RequestMapping("/list/export/csv.action")
    @RequiredPermission("admin.show")
    public Object exportCsv(Admin admin, AdminListForm form, Popups popups) throws Exception {
        byte[] csvData = csvService.getUserCSV(getAdminListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList());

        if (csvData == null) {
            popups.alert("error.export.file_not_ready");
            return MESSAGES_VIEW;
        }

        String description = "Page: %d, sort: %s, direction: %s".formatted(form.getPage(), form.getSort(), form.getDir());
        userActivityLogService.writeUserActivityLog(admin, "export admins csv", description, LOGGER);
        return ResponseEntity.ok()
                .contentLength(csvData.length)
                .contentType(MediaType.parseMediaType("application/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment("users.csv"))
                .body(new InputStreamResource(new ByteArrayInputStream(csvData)));
    }

    @RequestMapping("/list/export/pdf.action")
    @RequiredPermission("admin.show")
    public ResponseEntity<InputStreamResource> exportPdf(Admin admin, AdminListForm form) {
        List<AdminEntry> admins = getAdminListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList();
        byte[] pdfFileBytes = pdfService.writeUsersToPdfAndGetByteArray(admins);

        String description = "Page: %d, sort: %s, direction: %s".formatted(form.getPage(), form.getSort(), form.getDir());
        userActivityLogService.writeUserActivityLog(admin, "export admins pdf", description, LOGGER);

        return ResponseEntity.ok()
                .contentLength(pdfFileBytes.length)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        HttpUtils.getContentDispositionAttachment("users.pdf"))
                .body(new InputStreamResource(new ByteArrayInputStream(pdfFileBytes)));
    }

    @GetMapping(value = "/delete.action")
    @RequiredPermission("admin.delete|restfulUser.delete")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateSelectedIds(bulkIds);

        ServiceResult<List<Admin>> result = adminService.getAllowedForDeletion(bulkIds, admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        model.addAttribute("usernames", result.getResult().stream().map(Admin::getUsername).toList());
        return "user_delete_modal";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("admin.delete|restfulUser.delete")
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds,
                         @RequestParam(required = false) String backToUrl,
                         @RequestParam(required = false) Boolean includingActivity,
                         Admin admin, Popups popups, HttpServletResponse response, HttpSession session) {
        validateSelectedIds(bulkIds);

        List<Admin> deletedAdmins = adminService.delete(bulkIds, admin.getCompanyID());
        if (Boolean.TRUE.equals(includingActivity)) {
            userActivityLogService.deleteActivity(deletedAdmins);
        }

        writeUALForDelete(admin, deletedAdmins);

        if (deletedAdmins.stream().anyMatch(a -> a.getAdminID() == admin.getAdminID())) {
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/logon.action";
        }

        popups.selectionDeleted();

        return StringUtils.isNotEmpty(backToUrl)
                ? "redirect:" + backToUrl
                : REDIRECT_TO_LIST + "?restoreSort=true";
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    private void writeUALForDelete(Admin admin, List<Admin> deletedAdmins) {
        List<String> identifiers = deletedAdmins.stream()
                .map(a -> String.format("%s (%d)", a.getUsername(), a.getAdminID()))
                .toList();

        userActivityLogService.writeUserActivityLog(admin, "delete users","users: " + StringUtils.join(identifiers, ", "));
    }

    private boolean adminUsernameChangedToExisting(AdminForm aForm) {
        final Admin currentAdmin = adminService.getAdmin(aForm.getAdminID(), aForm.getCompanyID());
        return !StringUtils.equals(currentAdmin.getUsername(), aForm.getUsername()) && adminService.adminExists(aForm.getUsername());
    }

    private boolean checkPassword(AdminForm form, Popups popups) {
        Admin admin = adminService.getAdmin(form.getAdminID(), form.getCompanyID());
        PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups, "password");
        if (admin != null && admin.getAdminID() != 0) {
            // Existing user changes his password
            return passwordCheck.checkAdminPassword(form.getPassword(), admin, handler);
        } else {
            // New user changes wants to set his initial password
            return passwordCheck.checkNewAdminPassword(form.getPassword(), form.getCompanyID(), handler);
        }
    }

    private PaginatedList<AdminEntry> getAdminListFromAdminListForm(Admin admin, AdminListForm form) {
        return getAdminListFromAdminListForm(admin, form, form.getNumberOfRows());
    }

    private PaginatedList<AdminEntry> getAdminListFromAdminListForm(Admin admin, AdminListForm form, int numberOfRows) {
        return adminService.getAdminList(admin.getCompanyID(),
                form.getSearchFirstName(),
                form.getSearchLastName(),
                form.getSearchEmail(),
                form.getSearchCompany(),
                form.getFilterCompanyId(),
                form.getFilterAdminGroupId(),
                form.getFilterMailinglistId(),
                form.getFilterLanguage(),
                form.getFilterCreationDate(),
                form.getFilterLastLoginDate(),
                form.getFilterUsername(),
                form.getSort(),
                form.getDir(),
                form.getPage(),
                numberOfRows);
    }

    private String saveAdminAndGetView(AdminForm form, Admin admin, Popups popups) {
        final boolean isNew = form.getAdminID() == 0;

        Admin oldSavingAdmin = null;
        AdminPreferences oldSavingAdminPreferences = null;

        if (!isNew) {
            oldSavingAdmin = adminService.getAdmin(form.getAdminID(), form.getCompanyID());
            oldSavingAdminPreferences = adminService.getAdminPreferences(form.getAdminID());
            if (!StringUtils.equals(oldSavingAdmin.getUsername(), form.getUsername())
                    && adminService.checkBlacklistedAdminNames(form.getUsername())) {
                popups.alert(USERNAME_DUPLICATE_MSG);
                return MESSAGES_VIEW;
            }
        } else if (adminService.isGuiAdminLimitReached(form.getCompanyID())) {
            popups.alert("error.admin.limit");
            return MESSAGES_VIEW;
        }

        // Set values to persisted values to not overwrite with invalid values
        if(!canEditAltg(admin, form) && oldSavingAdmin != null) {
        	form.setAltgId(oldSavingAdmin.getAccessLimitingTargetGroupID());
        	form.setAltgIds(oldSavingAdmin.getAltgIds());
        }

        final AdminSavingResult result = adminService.saveAdmin(form, false, admin);

        if (!result.isSuccess()) {
            popups.alert(result.getError());
            return MESSAGES_VIEW;
        }

        final Admin savedAdmin = result.getResult();

        if(admin.getAdminID() == savedAdmin.getAdminID()) {
            updateCurrentAdmin(admin, savedAdmin);
        }

		if (isNew) {
			// Log successful creation of new user
			userActivityLogService.writeUserActivityLog(admin, "create user", savedAdmin.getUsername() + " (" + savedAdmin.getAdminID() + ")");
		} else {
			adminChangesLogService.getChangesAsUserActions(form, oldSavingAdmin, oldSavingAdminPreferences)
				.forEach(action -> userActivityLogService.writeUserActivityLog(admin, action));

			if (result.isPasswordChanged()) {
				userActivityLogService.writeUserActivityLog(admin, "change password", form.getUsername() + " (" + form.getAdminID() + ")");
			}
		}

        popups.changesSaved();

        return "redirect:/admin/" + savedAdmin.getAdminID() + "/view.action";
    }

    protected void loadDataForViewPage(Admin admin, Admin adminToEdit, Model model) {
        PasswordPolicies passwordPolicy = PasswordPolicyUtil.loadCompanyPasswordPolicy(
                adminToEdit == null ? admin.getCompanyID() : adminToEdit.getCompanyID(),
                configService
        );

        model.addAttribute("adminGroups", adminGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), admin, adminToEdit));
        model.addAttribute("layouts", adminService.getEmmLayoutsBase(admin));
        model.addAttribute("availableTimeZones", TimeZone.getAvailableIDs());
        model.addAttribute("createdCompanies", adminService.getCreatedCompanies(admin.getCompanyID()));
        model.addAttribute("PASSWORD_POLICY", passwordPolicy.getPolicyName());
    }

    protected void initializeForm(AdminForm form, Admin adminToEdit, Admin editorAdmin) {
        form.setUsername(adminToEdit.getUsername());
        form.setGender(adminToEdit.getGender());
        form.setTitle(adminToEdit.getTitle());
        form.setFullname(adminToEdit.getFullname());
        form.setFirstname(adminToEdit.getFirstName());
        form.setEmployeeID(adminToEdit.getEmployeeID());
        form.setStatEmail(adminToEdit.getStatEmail());
        form.setPassword("");
        form.setAdminPhone(adminToEdit.getAdminPhone());
        form.setPasswordConfirm("");
        form.setCompanyID(adminToEdit.getCompanyID());
        form.setAdminLocale(new Locale(adminToEdit.getAdminLang(), adminToEdit.getAdminCountry()));
        form.setAdminTimezone(adminToEdit.getAdminTimezone());
        form.setGroupIDs(adminToEdit.getGroupIds());
        form.setCompanyName(adminToEdit.getCompanyName());
        form.setEmail(adminToEdit.getEmail());
        form.setLayoutBaseId(adminToEdit.getLayoutBaseID());
        form.setUiLayoutType(adminToEdit.getLayoutType());
        form.setInitialCompanyName(adminToEdit.getInitialCompanyName());
        PasswordReminderState passwordReminderState = adminToEdit.getPasswordReminderState();
        form.setPasswordReminderEnabled(passwordReminderState != null && passwordReminderState.ordinal() > 0);

        form.setAdminPreferences(
                conversionService.convert(
                        adminService.getAdminPreferences(adminToEdit.getAdminID()), AdminPreferences.class)
        );
    }

    private void prepareRightsViewPageData(Admin admin, AdminRightsForm form, Model model, Admin adminToEdit) {
        form.setUsername(adminToEdit.getUsername());
        Map<String, PermissionsOverviewData.PermissionCategoryEntry> permissionsOverview =
                adminService.getPermissionOverviewData(admin, adminToEdit);
        List<PermissionsOverviewData.PermissionCategoryEntry> list = new ArrayList<>(permissionsOverview.values());
        Collections.sort(list);
        model.addAttribute("permissionCategories", list);
    }

    private boolean saveAdminRightsAndWriteToActivityLog(Admin admin, AdminRightsForm aForm, Popups popups) {
        Tuple<List<String>, List<String>> changes = adminService.saveAdminPermissions(admin.getCompanyID(),
                aForm.getAdminID(), aForm.getGrantedPermissions(), admin.getAdminID());

        if (changes == null) {
            popups.alert("error.admin.change.permission");
            return false;
        }
        Admin savingAdmin = adminService.getAdmin(aForm.getAdminID(), admin.getCompanyID());

        String action = String.format("User: \"%s\"(%d).", savingAdmin.getUsername(), savingAdmin.getAdminID());
        String added = StringUtils.join(changes.getFirst(), ", ");
        String removed = StringUtils.join(changes.getSecond(), ", ");

        if (!added.isEmpty()) {
            userActivityLogService.writeUserActivityLog(
                    admin, "edit user", action + " Added permissions: " + added, LOGGER);
        }
        if (!removed.isEmpty()) {
            userActivityLogService.writeUserActivityLog(
                    admin, "edit user", action + " Removed permissions: " + removed, LOGGER);
        }
        return true;
    }

    private void updateCurrentAdmin(Admin currentAdmin, Admin savedAdmin) {
        if (!currentAdmin.getAdminLang().equals(savedAdmin.getAdminLang())) {
            AgnUtils.updateBrowserCacheMarker();
        }

        currentAdmin.setAdminLang(savedAdmin.getAdminLang());
        currentAdmin.setAdminCountry(savedAdmin.getAdminCountry());
        currentAdmin.setAdminTimezone(savedAdmin.getAdminTimezone());
    }

    protected String prepareErrorPageForNotLoadedAdmin(int adminId, int companyID, Popups popups, String viewName) {
        popups.defaultError();
        LOGGER.warn("Could not load admin by admin id: {}, company id: {}.", adminId, companyID);
        return viewName;
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return "password".equals(param) || "passwordConfirm".equals(param);
    }

}
