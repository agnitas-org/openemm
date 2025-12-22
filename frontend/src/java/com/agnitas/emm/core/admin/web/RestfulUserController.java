/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import com.agnitas.emm.core.commons.password.WebservicePasswordCheckImpl;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.service.CSVService;
import com.agnitas.service.PdfService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.Tuple;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class RestfulUserController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(RestfulUserController.class);
    
    private static final String ADMIN_ENTRIES_KEY = "adminEntries";
    private static final String REDIRECT_TO_LIST = "redirect:/restfulUser/list.action";

    protected final ConfigService configService;
    private final AdminService adminService;
    private final LogonService logonService;
    private final CompanyService companyService;
    private final AdminGroupService adminGroupService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final AdminChangesLogService adminChangesLogService;
    private final PasswordCheck passwordCheck = new WebservicePasswordCheckImpl();
    private final CSVService csvService;
    private final PdfService pdfService;
    private final ConversionService conversionService;

    public RestfulUserController(ConfigService configService,
                                 AdminService adminService,
                                 CompanyService companyService,
                                 AdminGroupService adminGroupService, WebStorage webStorage,
                                 UserActivityLogService userActivityLogService,
                                 AdminChangesLogService adminChangesLogService,
                                 CSVService csvService, PdfService pdfService,
                                 ConversionService conversionService,
                                 LogonService logonService) {
        this.configService = configService;
        this.adminService = adminService;
        this.companyService = companyService;
        this.adminGroupService = adminGroupService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.adminChangesLogService = adminChangesLogService;
        this.csvService = csvService;
        this.pdfService = pdfService;
        this.conversionService = conversionService;
        this.logonService = logonService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @RequestMapping("/list.action")
    @RequiredPermission("restfulUser.show")
    public Pollable<ModelAndView> list(Admin admin, AdminListForm form, Model model, HttpSession session,
                                       @RequestParam(required = false) Boolean restoreSort,
                                       @ModelAttribute AdminListFormSearchParams adminListSearchParams) {
        FormUtils.syncPaginationData(webStorage, WebStorage.ADMIN_OVERVIEW, form, restoreSort);
        adminListSearchParams.restoreParams(form);

        model.addAttribute(ADMIN_ENTRIES_KEY, new PaginatedList<>());
        int companyID = admin.getCompanyID();

        PollingUid pollingUid = PollingUid.builder(session.getId(), "restful" + ADMIN_ENTRIES_KEY)
                .arguments(form.toArray())
                .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute(ADMIN_ENTRIES_KEY, getRestfulUserListFromAdminListForm(admin, form));
            form.setCompanies(companyService.getCreatedCompanies(companyID));
            form.setAdminGroups(adminGroupService.getAdminGroupsByCompanyIdAndDefault(companyID, admin, null));

            return new ModelAndView("settings_restfuluser_list", model.asMap());
        };

        ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_LIST, form.toMap());

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    @GetMapping("/search.action")
    @RequiredPermission("restfulUser.show")
    public String search(AdminListForm form, @ModelAttribute AdminListFormSearchParams searchParams, RedirectAttributes model) {
        searchParams.storeParams(form);
        model.addFlashAttribute("adminListForm", form);

        return REDIRECT_TO_LIST + "?restoreSort=true";
    }

    @RequestMapping("/{adminID}/view.action")
    @RequiredPermission("restfulUser.show")
    public String view(Admin admin, AdminForm form, Popups popups, Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups);
        }
        if (adminToEdit.getGroups() == null || adminToEdit.getGroups().isEmpty()) {
            popups.alert("error.admin.invalidGroup");
        }

        initializeForm(form, adminToEdit);

        LOGGER.info("loadAdmin: admin {} loaded", form.getAdminID());

        userActivityLogService.writeUserActivityLog(admin, "view user", adminToEdit.getUsername());

        loadDataForViewPage(admin, adminToEdit, model);
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");
        model.addAttribute("isRestfulUser", true);

        return "user_view";
    }

    @RequestMapping("/{adminID}/rights/view.action")
    @RequiredPermission("restfulUser.show")
    public String viewRights(Admin admin, AdminRightsForm form, Popups popups, Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final Admin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups);
        }

        prepareRightsViewPageData(admin, form, model, adminToEdit);

        return "user_permissions";
    }

    @PostMapping(value = "/{adminID}/save.action")
    @RequiredPermission("restfulUser.change")
    public String save(Admin admin, AdminForm form, Popups popups) {
        if (!AdminFormValidator.validate(form, popups)) {
        	popups.alert("error.admin.save");
        	return redirectToView(form.getAdminID());
        } else if (adminUsernameChangedToExisting(form)) {
            popups.alert("error.username.duplicate");
            return redirectToView(form.getAdminID());
        } else {
            if (StringUtils.isEmpty(form.getPassword()) || checkPassword(form, popups)) {
                saveAdminAndGetView(form, admin, popups);
                logonService.updateSessionsLanguagesAttributes(admin);
            }
            return redirectToView(form.getAdminID());
        }
    }

    @PostMapping(value = "/{adminID}/rights/save.action")
    @RequiredPermission("restfulUser.change")
    public String saveRights(Admin admin, AdminRightsForm form, Popups popups) {
        try {
            if (saveAdminRightsAndWriteToActivityLog(admin, form, popups)) {
                popups.changesSaved();
            }

            return "redirect:/restfulUser/" + form.getAdminID() + "/rights/view.action";
        } catch (Exception e) {
            LOGGER.error("Exception saving rights", e);
            popups.alert("error.admin.save", e);
            return MESSAGES_VIEW;
        }
    }

    private String redirectToView(int adminId) {
        return "redirect:/restfulUser/" + adminId + "/view.action";
    }

    @RequestMapping("/create.action")
    @RequiredPermission("restfulUser.new")
    public String create(Admin admin, @ModelAttribute AdminForm form, Model model) {
        model.addAttribute("PASSWORD_POLICY", "WEBSERVICE");
        model.addAttribute("isRestfulUser", true);

        loadDataForViewPage(admin, null, model);

        return "user_view";
    }

    @PostMapping(value = "/saveNew.action")
    @RequiredPermission("restfulUser.new")
    public String saveNew(Admin admin, AdminForm form, Popups popups) {
        final int maximumNumberOfRestfulUsers = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfRestfulUsers, admin.getCompanyID());
        if (maximumNumberOfRestfulUsers >= 0 && maximumNumberOfRestfulUsers <= adminService.getNumberOfRestfulUsers(admin.getCompanyID())) {
            popups.alert("error.numberOfRestfulUsersExceeded", maximumNumberOfRestfulUsers);
            return MESSAGES_VIEW;
        }
        form.setAdminID(0);
        if (!AdminFormValidator.validate(form, popups)) {
        	popups.alert("error.admin.save");
        	return MESSAGES_VIEW;
        }
        if (StringUtils.isBlank(form.getPassword())) {
            popups.alert("error.password.missing");
            return MESSAGES_VIEW;
        }

        if (form.getGroupIDs() == null || form.getGroupIDs().isEmpty()) {
            popups.alert("error.user.group");
            return MESSAGES_VIEW;
        }

        if (adminService.adminExists(form.getUsername())) {
            popups.alert("error.username.duplicate");
            return MESSAGES_VIEW;
        }

        if (!checkPassword(form, popups)) {
            return MESSAGES_VIEW;
        }

        return saveAdminAndGetView(form, admin, popups);
    }

    @RequestMapping("/list/export/csv.action")
    @RequiredPermission("restfulUser.show")
    public Object exportCsv(Admin admin, AdminListForm form, Popups popups) throws Exception {
        byte[] csvData = csvService.getUserCSV(getRestfulUserListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList());

        if (csvData == null) {
            popups.alert("error.export.file_not_ready");
            return MESSAGES_VIEW;
        }

        String description = "Page: " + form.getPage()
                + ", sort: " + form.getSort()
                + ", direction: " + form.getDir();
        userActivityLogService.writeUserActivityLog(admin, "export admins csv", description, LOGGER);
        return ResponseEntity.ok()
                .contentLength(csvData.length)
                .contentType(MediaType.parseMediaType("application/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment("users.csv"))
                .body(new InputStreamResource(new ByteArrayInputStream(csvData)));
    }

    @RequestMapping("/list/export/pdf.action")
    @RequiredPermission("restfulUser.show")
    public ResponseEntity<InputStreamResource> exportPdf(Admin admin, AdminListForm form) {
        List<AdminEntry> admins = getRestfulUserListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList();
        byte[] pdfFileBytes = pdfService.writeUsersToPdfAndGetByteArray(admins);

        String description = "Page: " + form.getPage()
                + ", sort: " + form.getSort()
                + ", direction: " + form.getDir();
        userActivityLogService.writeUserActivityLog(admin, "export admins pdf", description, LOGGER);

        return ResponseEntity.ok()
                .contentLength(pdfFileBytes.length)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        HttpUtils.getContentDispositionAttachment("users.pdf"))
                .body(new InputStreamResource(new ByteArrayInputStream(pdfFileBytes)));
    }

    @ModelAttribute
    public AdminListFormSearchParams getAdminFormSearchParams(){
        return new AdminListFormSearchParams();
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

    private PaginatedList<AdminEntry> getRestfulUserListFromAdminListForm(Admin admin, AdminListForm form) {
        return getRestfulUserListFromAdminListForm(admin, form, form.getNumberOfRows());
    }

    private PaginatedList<AdminEntry> getRestfulUserListFromAdminListForm(Admin admin, AdminListForm form, int numberOfRows) {
        return adminService.getRestfulUserList(admin.getCompanyID(),
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
                popups.alert("error.username.duplicate");
                return MESSAGES_VIEW;
            }
        } else if (adminService.isGuiAdminLimitReached(form.getCompanyID())) {
            popups.alert("error.admin.limit");
            return MESSAGES_VIEW;
        }
        
        final AdminSavingResult result = adminService.saveAdmin(form, true, admin);

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

        return redirectToView(savedAdmin.getAdminID());
    }

    protected void loadDataForViewPage(Admin admin, Admin adminToEdit, Model model) {
        model.addAttribute("adminGroups", adminGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID(), admin, adminToEdit));
        model.addAttribute("layouts", adminService.getEmmLayoutsBase(admin));
        model.addAttribute("availableTimeZones", TimeZone.getAvailableIDs());
        model.addAttribute("createdCompanies", adminService.getCreatedCompanies(admin.getCompanyID()));
    }

    protected void initializeForm(AdminForm form, Admin adminToEdit) {
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
        model.addAttribute("isRestfulUser", true);
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
        currentAdmin.setAdminLang(savedAdmin.getAdminLang());
        currentAdmin.setAdminCountry(savedAdmin.getAdminCountry());
        currentAdmin.setAdminTimezone(savedAdmin.getAdminTimezone());
    }

    protected String prepareErrorPageForNotLoadedAdmin(int adminId, int companyID, Popups popups) {
        popups.defaultError();
        LOGGER.warn("Could not load admin by admin id: {}, company id: {}.", adminId, companyID);
        return "redirect:/restfulUser/list.action?restoreSort=true";
    }

}
