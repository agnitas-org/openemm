/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminPreferences;
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
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.web.LogonController;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.CSVService;
import com.agnitas.service.PdfService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.Tuple;
import com.agnitas.web.forms.FormSearchParams;
import com.agnitas.web.forms.FormUtils;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

public class AdminController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(AdminController.class);

    private static final String ADMIN_ENTRIES_KEY = "adminEntries";
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
    public Pollable<ModelAndView> list(final Admin admin, final AdminListForm form,
                                       final Model model, final HttpSession session,
                                       @RequestParam(value = FormSearchParams.RESET_PARAM_NAME, required = false) boolean resetSearchParams,
                                       @RequestParam(value = FormSearchParams.RESTORE_PARAM_NAME, required = false) boolean restoreSearchParams,
                                       @RequestParam(required = false) Boolean restoreSort,
                                       @ModelAttribute AdminListFormSearchParams adminListSearchParams) {
        FormUtils.syncPaginationData(webStorage, WebStorage.ADMIN_OVERVIEW, form, restoreSort);
        if (resetSearchParams) {
            FormUtils.resetSearchParams(adminListSearchParams, form);
        } else {
            FormUtils.syncSearchParams(adminListSearchParams, form, restoreSearchParams || isUiRedesign(admin));
        }
        model.addAttribute(ADMIN_ENTRIES_KEY, new PaginatedListImpl<>());
        int companyID = admin.getCompanyID();

        PollingUid pollingUid = PollingUid.builder(session.getId(), ADMIN_ENTRIES_KEY)
                .arguments(form.toArray())
                .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute(ADMIN_ENTRIES_KEY, getAdminListFromAdminListForm(admin, form));
            form.setCompanies(companyService.getCreatedCompanies(companyID));
            form.setAdminGroups(adminGroupService.getAdminGroupsByCompanyIdAndDefault(companyID, admin, null));

            return new ModelAndView("settings_admin_list", model.asMap());
        };

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, new ModelAndView(REDIRECT_TO_LIST, form.toMap()), worker);
    }

    @GetMapping("/search.action")
    public String search(AdminListForm form, @ModelAttribute AdminListFormSearchParams searchParams, RedirectAttributes model) {
        FormUtils.syncSearchParams(searchParams, form, false);
        model.addFlashAttribute("adminListForm", form);

        return REDIRECT_TO_LIST + "?restoreSort=true";
    }

    @RequestMapping("/{adminID}/view.action")
    public String view(final Admin admin, final AdminForm form, final Popups popups, final Model model) {
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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("loadAdmin: admin {} loaded", form.getAdminID());
        }

        userActivityLogService.writeUserActivityLog(admin, "view user", adminToEdit.getUsername());

        loadDataForViewPage(admin, adminToEdit, model);
        model.addAttribute("EDIT_ALTG_ENABLED", canEditAltg(admin, form));

        if (isUiRedesign(admin)) {
            return "user_view";
        }
        return "settings_admin_view";
    }

    private boolean isUiRedesign(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

    private static boolean canEditAltg(final Admin admin, final AdminForm adminForm) {
    	return admin.getCompanyID() == adminForm.getCompanyID();
    }

    @RequestMapping("/{adminID}/welcome.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String sendWelcome(final Admin admin, final AdminRightsForm form, final Popups popups, HttpServletRequest request) {
    	String clientIp = request.getRemoteAddr();
    	final int adminIdToEdit = form.getAdminID();
    	final int companyID = admin.getCompanyID();
    	final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
    	logonService.sendWelcomeMail(adminToEdit, clientIp, LogonController.PASSWORD_RESET_LINK_PATTERN);
    	popups.success("admin.password.sent");
    	return MESSAGES_VIEW;
    }

    @RequestMapping("/welcome.action")
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
    public String viewRights(final Admin admin, final AdminRightsForm form, final Popups popups, final Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups, REDIRECT_TO_LIST + "?restoreSort=true");
        }

        prepareRightsViewPageData(admin, form, model, adminToEdit);

        return isUiRedesign(admin) ? "user_permissions" : "settings_admin_rights";
    }

    @PostMapping(value = "/{adminID}/save.action")
    public ModelAndView save(final Admin admin, final AdminForm form, final Popups popups) {
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
    public String saveRights(final Admin admin, final AdminRightsForm form, final Popups popups, final Model model) {
        try {
            final boolean isSuccess = saveAdminRightsAndWriteToActivityLog(admin, form, popups);

            if (isSuccess) {
                popups.success(CHANGES_SAVED_MSG);
            }

            if (admin.isRedesignedUiUsed()) {
                return "redirect:/admin/" + form.getAdminID() + "/rights/view.action";
            } else {
                final int adminIdToEdit = form.getAdminID();
                final int companyID = admin.getCompanyID();
                final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
                prepareRightsViewPageData(admin, form, model, adminToEdit);
                return "settings_admin_rights";
            }
        } catch (Exception e) {
            LOGGER.error("Exception saving rights", e);
            popups.alert(SAVE_ERROR_MSG, e);
            return MESSAGES_VIEW;
        }
    }

   @RequestMapping("/create.action")
    public String create(final Admin admin, @ModelAttribute("adminForm") AdminForm form, final Model model) {
        loadDataForViewPage(admin, null, model);
        adminService.setDefaultPreferencesSettings(form.getAdminPreferences());

        return "settings_admin_view";
    }

    @PostMapping(value = "/saveNew.action")
    public String saveNew(final Admin admin, final AdminForm form, final Popups popups) {
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
    public Object exportCsv(final Admin admin, final AdminListForm form, final Popups popups) throws Exception {
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
    public ResponseEntity<InputStreamResource> exportPdf(final Admin admin, final AdminListForm form)
            throws IOException, DocumentException {

        byte[] pdfFileBytes = pdfService
                .writeUsersToPdfAndGetByteArray(getAdminListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList());

        String description = "Page: %d, sort: %s, direction: %s".formatted(form.getPage(), form.getSort(), form.getDir());
        userActivityLogService.writeUserActivityLog(admin, "export admins pdf", description, LOGGER);

        return ResponseEntity.ok()
                .contentLength(pdfFileBytes.length)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        HttpUtils.getContentDispositionAttachment("users.pdf"))
                .body(new InputStreamResource(new ByteArrayInputStream(pdfFileBytes)));
    }

    @RequestMapping("/{adminID}/confirmDelete.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String confirmDelete(@RequestParam(required = false) String backToUrl, AdminForm form, Admin admin, Popups popups, Model model) {
        final int adminIdToDelete = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final ServiceResult<Admin> deleteConfirmSR = adminService.isPossibleToDeleteAdmin(adminIdToDelete, companyID);
        if (deleteConfirmSR.isSuccess()) {
            form.setUsername(deleteConfirmSR.getResult().getUsername());
            model.addAttribute("backToUrl", backToUrl);
            return "settings_admin_delete_ajax";
        }

        if (deleteConfirmSR.getResult() == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToDelete, companyID, popups, REDIRECT_TO_LIST + "?restoreSort=true");
        }

        popups.addPopups(deleteConfirmSR);
        return MESSAGES_VIEW;
    }

    @RequestMapping("/{adminID}/delete.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String delete(Admin admin, AdminForm form, Popups popups,
                         final HttpServletResponse response, final HttpSession session,
                         RedirectAttributes redirectAttributes) {
        final ServiceResult<Admin> result = adminService.delete(admin, form.getAdminID());

        if (result.isSuccess()) {
            final Admin deletedAdmin = result.getResult();

            userActivityLogService.writeUserActivityLog(admin, "delete user", deletedAdmin.getUsername() + " (" + deletedAdmin.getAdminID() + ")", LOGGER);

            if (admin.getAdminID() == deletedAdmin.getAdminID()) {
                session.invalidate();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "redirect:/logonOld.action";
            }

            popups.success(SELECTION_DELETED_MSG);
        } else {
            popups.alert("Error");
        }

       redirectAttributes.addAttribute(FormSearchParams.RESTORE_PARAM_NAME, true);
        return REDIRECT_TO_LIST + "?restoreSort=true";
    }

    @GetMapping(value = "/deleteRedesigned.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateSelectedIds(bulkIds);

        ServiceResult<List<Admin>> result = adminService.getAllowedForDeletion(bulkIds, admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(Admin::getUsername).toList(),
                "settings.admin.delete", "settings.admin.delete.question",
                "bulkAction.delete.admin", "bulkAction.delete.admin.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, @RequestParam(required = false) String backToUrl, Admin admin, Popups popups, HttpServletResponse response, HttpSession session) {
        validateSelectedIds(bulkIds);

        List<Admin> deletedAdmins = adminService.delete(bulkIds, admin.getCompanyID());
        writeUALForDelete(admin, deletedAdmins);

        if (deletedAdmins.stream().anyMatch(a -> a.getAdminID() == admin.getAdminID())) {
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/logon.action";
        }

        popups.success(SELECTION_DELETED_MSG);

        return StringUtils.isNotEmpty(backToUrl)
                ? "redirect:" + backToUrl
                : REDIRECT_TO_LIST + "?restoreSort=true";
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    private void writeUALForDelete(Admin admin, List<Admin> deletedAdmins) {
        List<String> identifiers = deletedAdmins.stream()
                .map(a -> String.format("%s (%d)", a.getUsername(), a.getAdminID()))
                .toList();

        userActivityLogService.writeUserActivityLog(admin, "delete users","users: " + StringUtils.join(identifiers, ", "));
    }

    private boolean adminUsernameChangedToExisting(final AdminForm aForm) {
        final Admin currentAdmin = adminService.getAdmin(aForm.getAdminID(), aForm.getCompanyID());
        return !StringUtils.equals(currentAdmin.getUsername(), aForm.getUsername()) && adminService.adminExists(aForm.getUsername());
    }

    private boolean checkPassword(final AdminForm form, final Popups popups) {
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

    private PaginatedListImpl<AdminEntry> getAdminListFromAdminListForm(Admin admin, AdminListForm form) {
        return getAdminListFromAdminListForm(admin, form, form.getNumberOfRows());
    }

    private PaginatedListImpl<AdminEntry> getAdminListFromAdminListForm(Admin admin, AdminListForm form, int numberOfRows) {
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

        popups.success(CHANGES_SAVED_MSG);

        return "redirect:/admin/" + savedAdmin.getAdminID() + "/view.action";
    }

    protected void loadDataForViewPage(final Admin admin, final Admin adminToEdit, final Model model){
        model.addAttribute("adminGroups", adminGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), admin, adminToEdit));
        model.addAttribute("layouts", adminService.getEmmLayoutsBase(admin));
        model.addAttribute("availableTimeZones", TimeZone.getAvailableIDs());
        model.addAttribute("createdCompanies", adminService.getCreatedCompanies(admin.getCompanyID()));
        model.addAttribute("PASSWORD_POLICY", PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService).getPolicyName());
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
        Set<String> userRights = admin.isRedesignedUiUsed() ? aForm.getGrantedPermissions() : aForm.getUserRights();
        Tuple<List<String>, List<String>> changes = adminService.saveAdminPermissions(admin.getCompanyID(),
                aForm.getAdminID(), userRights, admin.getAdminID());

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

    private void updateCurrentAdmin(final Admin currentAdmin, final Admin savedAdmin) {
        if (!currentAdmin.getAdminLang().equals(savedAdmin.getAdminLang())) {
            AgnUtils.updateBrowserCacheMarker();
        }

        currentAdmin.setAdminLang(savedAdmin.getAdminLang());
        currentAdmin.setAdminCountry(savedAdmin.getAdminCountry());
        currentAdmin.setAdminTimezone(savedAdmin.getAdminTimezone());
    }

    protected String prepareErrorPageForNotLoadedAdmin(final int adminId, final int companyID, final Popups popups,
                                                       final String viewName) {
        popups.alert(ERROR_MSG);
        LOGGER.warn("Could not load admin by admin id: {}, company id: {}.", adminId, companyID);
        return viewName;
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return "password".equals(param) || "passwordConfirm".equals(param);
    }
}
