/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.form.AdminListForm;
import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.form.AdminPreferences;
import com.agnitas.emm.core.admin.form.AdminRightsForm;
import com.agnitas.emm.core.admin.form.validation.AdminFormValidator;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComCSVService;
import com.agnitas.service.ComPDFService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.lowagie.text.DocumentException;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.Tuple;
import org.agnitas.web.forms.FormSearchParams;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PermissionMapping("admin")
@SessionAttributes(types = AdminListFormSearchParams.class)
public class AdminController extends AbstractAdminControllerBase {

    private static final transient Logger LOGGER = Logger.getLogger(AdminController.class);
    private static final String ADMIN_ENTRIES_KEY = "adminEntries";

    private final ConfigService configService;
    private final ComMailinglistService mailinglistService;
    private final AdminService adminService;
    private final CompanyService companyService;
    private final AdminGroupService adminGroupService;
    private final WebStorage webStorage;
    private final UserActivityLogService userActivityLogService;
    private final AdminChangesLogService adminChangesLogService;
    private final PasswordCheck passwordCheck;
    private final ComCSVService csvService;
    private final ComPDFService pdfService;
    private final ConversionService conversionService;
    private final ComTargetService targetService;

    protected static final String FUTURE_TASK = "GET_ADMIN_LIST";

    public AdminController(ConfigService configService,
                           ComMailinglistService mailinglistService,
                           AdminService adminService,
                           CompanyService companyService,
                           AdminGroupService adminGroupService, WebStorage webStorage,
                           UserActivityLogService userActivityLogService,
                           AdminChangesLogService adminChangesLogService,
                           PasswordCheck passwordCheck, ComCSVService csvService, ComPDFService pdfService,
                           ConversionService conversionService,
							ComTargetService targetService) {
        this.configService = configService;
        this.mailinglistService = mailinglistService;
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
    }

    @RequestMapping("/list.action")
    public Pollable<ModelAndView> list(final ComAdmin admin, final AdminListForm form,
                                       final Model model, final HttpSession session,
                                       @RequestParam(value = FormSearchParams.RESET_PARAM_NAME, required = false) boolean resetSearchParams,
                                       @RequestParam(value = FormSearchParams.RESTORE_PARAM_NAME, required = false) boolean restoreSearchParams,
                                       @ModelAttribute AdminListFormSearchParams adminListSearchParams) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.ADMIN_OVERVIEW, form);
        if(resetSearchParams) {
            FormUtils.resetSearchParams(adminListSearchParams, form);
        } else {
            FormUtils.syncSearchParams(adminListSearchParams, form, restoreSearchParams);
        }
        model.addAttribute(ADMIN_ENTRIES_KEY, new PaginatedListImpl<CompanyEntry>());
        int companyID = admin.getCompanyID();

        PollingUid pollingUid = PollingUid.builder(session.getId(), ADMIN_ENTRIES_KEY)
                .arguments(form.toArray())
                .build();

        Callable<ModelAndView> worker = () -> {
            model.addAttribute(ADMIN_ENTRIES_KEY, getAdminListFromAdminListForm(admin, form));
            form.setCompanies(companyService.getCreatedCompanies(companyID));
            form.setAdminGroups(adminGroupService.getAdminGroupsByCompanyIdAndDefault(companyID));
            form.setMailinglists(mailinglistService.getAllMailingListsNames(companyID));

            return new ModelAndView("settings_admin_list", model.asMap());
        };

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/list.action", form.toMap());

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    @RequestMapping("/{adminID}/view.action")
    public String view(final ComAdmin admin, final AdminForm form, final Popups popups, final Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final ComAdmin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups, "redirect:/admin/list.action");
        }
        if (adminToEdit.getGroup() == null) {
            popups.alert("error.admin.invalidGroup");
        }

        initializeForm(form, adminToEdit);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("loadAdmin: admin " + form.getAdminID() + " loaded");
        }

        userActivityLogService.writeUserActivityLog(admin, "view user", adminToEdit.getUsername());

        loadDataForViewPage(admin, model);

        return "settings_admin_view";
    }

    @RequestMapping("/{adminID}/rights/view.action")
    public String viewRights(final ComAdmin admin, final AdminRightsForm form, final Popups popups, final Model model) {
        final int adminIdToEdit = form.getAdminID();
        final int companyID = admin.getCompanyID();
        final ComAdmin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
        if (adminToEdit == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToEdit, companyID, popups, "redirect:/admin/list.action");
        }

        prepareRightsViewPageData(admin, form, model, adminToEdit);

        return "settings_admin_rights";
    }

    @PostMapping(value = "/{adminID}/save.action")
    public String save(final ComAdmin admin, final AdminForm form, final Popups popups, final Model model,
            final HttpSession session) {
        if(!AdminFormValidator.validate(form, popups)){
            return "messages";
        }
        if (adminUsernameChangedToExisting(form)) {
            popups.alert("error.username.duplicate");
        } else if (form.getGroupID() <= 0) {
            popups.alert("error.user.group");
        } else {
            if (StringUtils.isEmpty(form.getPassword()) || checkPassword(form, popups)) {
                saveAdminAndGetView(form, admin, session, popups);
                // Show "changes saved"
                popups.success("default.changes_saved");
            }
        }

        loadDataForViewPage(admin, model);
        return "settings_admin_view";
    }

    @PostMapping(value = "/{adminID}/rights/save.action")
    public String saveRights(final ComAdmin admin, final AdminRightsForm form, final Popups popups, final Model model) {
        try {
            final boolean isSuccess = saveAdminRightsAndWriteToActivityLog(admin, form, popups);
            final int adminIdToEdit = form.getAdminID();
            final int companyID = admin.getCompanyID();
            final ComAdmin adminToEdit = adminService.getAdmin(adminIdToEdit, companyID);
            prepareRightsViewPageData(admin, form, model, adminToEdit);

            if (isSuccess) {
                // Show "changes saved"
                popups.success("default.changes_saved");
            }
        } catch (Exception e) {
            LOGGER.error("Exception saving rights", e);
            popups.alert("error.admin.save", e);
            return "messages";
        }
        return "settings_admin_rights";
    }

   @RequestMapping("/create.action")
    public String create(final ComAdmin admin, AdminForm form, final Model model) {

        loadDataForViewPage(admin, model);

        return "settings_admin_view";
    }

    @PostMapping(value = "/saveNew.action")
    public String saveNew(final ComAdmin admin, final AdminForm form, final Popups popups, final HttpSession session) {
        final int maximumNumberOfAdmins = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAdmins);
        if (maximumNumberOfAdmins >= 0 && maximumNumberOfAdmins <= adminService.getNumberOfAdmins()) {
            popups.alert("error.numberOfAdminsExceeded", maximumNumberOfAdmins);
            return "messages";
        }
        form.setAdminID(0);
        if (StringUtils.isBlank(form.getPassword())) {
            popups.alert("error.password.missing");
            return "messages";
        }

        if (form.getGroupID() <= 0) {
            popups.alert("error.user.group");
            return "messages";
        }

        if (adminService.adminExists(form.getUsername())) {
            popups.alert("error.username.duplicate");
            return "messages";
        }

        if (!checkPassword(form, popups)) {
            return "messages";
        }

        return saveAdminAndGetView(form, admin, session, popups);
    }

    @RequestMapping("/list/export/csv.action")
    public Object exportCsv(final ComAdmin admin, final AdminListForm form, final Popups popups)
            throws UnsupportedEncodingException {

        String csv = csvService.getUserCSV(getAdminListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList());
        String fileName = "users.csv";

        if (csv == null) {
            popups.alert("error.export.file_not_ready");
            return "messages";
        }

        String description = "Page: " + form.getPage()
                + ", sort: " + form.getSort()
                + ", direction: " + form.getDir();
        userActivityLogService.writeUserActivityLog(admin, "export admins csv", description, LOGGER);
        return ResponseEntity.ok()
                .contentLength(csv.length())
                .contentType(MediaType.parseMediaType("application/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionHeaderContent(fileName))
                .body(new InputStreamResource(new ByteArrayInputStream(csv.getBytes("UTF-8"))));
    }

    @RequestMapping("/list/export/pdf.action")
    public ResponseEntity<InputStreamResource> exportPdf(final ComAdmin admin, final AdminListForm form)
            throws IOException, DocumentException {

        String pdfFileNameToDownload = "users.pdf";
        byte[] pdfFileBytes = pdfService
                .writeUsersToPdfAndGetByteArray(getAdminListFromAdminListForm(admin, form, Integer.MAX_VALUE).getList());

        String description = "Page: " + form.getPage()
                + ", sort: " + form.getSort()
                + ", direction: " + form.getDir();
        userActivityLogService.writeUserActivityLog(admin, "export admins pdf", description, LOGGER);

        return ResponseEntity.ok()
                .contentLength(pdfFileBytes.length)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        HttpUtils.getContentDispositionHeaderContent(pdfFileNameToDownload))
                .body(new InputStreamResource(new ByteArrayInputStream(pdfFileBytes)));
    }

    @RequestMapping("/{adminID}/confirmDelete.action")
    public String confirmDelete(final ComAdmin admin, final AdminForm form, final Popups popups, final Model model) {
        final int adminIdToDelete = form.getAdminID(), companyID = admin.getCompanyID();
        final ServiceResult<ComAdmin> deleteConfirmSR = adminService.isPossibleToDeleteAdmin(adminIdToDelete, companyID);
        if (deleteConfirmSR.isSuccess()) {
            form.setUsername(deleteConfirmSR.getResult().getUsername());
            return "settings_admin_delete_ajax";
        }

        if (deleteConfirmSR.getResult() == null) {
            return prepareErrorPageForNotLoadedAdmin(adminIdToDelete, companyID, popups, "redirect:/admin/list.action");
        }

        popups.addPopups(deleteConfirmSR);
        return "messages";
    }

    @RequestMapping("/{adminID}/delete.action")
    public String delete(final ComAdmin admin, final AdminForm form, final Popups popups,
                         final HttpServletResponse response, final HttpSession session,
                         RedirectAttributes redirectAttributes) {
        final ServiceResult<ComAdmin> result = adminService.delete(admin, form.getAdminID());

        if (result.isSuccess()) {
            final ComAdmin deletedAdmin = result.getResult();

            userActivityLogService.writeUserActivityLog(admin, "delete user", deletedAdmin.getUsername() + " (" + deletedAdmin.getAdminID() + ")", LOGGER);

            if (admin.getAdminID() == deletedAdmin.getAdminID()) {
                session.invalidate();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "redirect:/logon.action";
            }

            popups.success("default.selection.deleted");
        } else {
            popups.alert("Error");
        }

       redirectAttributes.addAttribute(FormSearchParams.RESTORE_PARAM_NAME, true);
        return "redirect:/admin/list.action";
    }

    @ModelAttribute
    public AdminListFormSearchParams getAdminFormSearchParams(){
        return new AdminListFormSearchParams();
    }

    private boolean adminUsernameChangedToExisting(final AdminForm aForm) {
        final ComAdmin currentAdmin = adminService.getAdmin(aForm.getAdminID(), aForm.getCompanyID());
        return !StringUtils.equals(currentAdmin.getUsername(), aForm.getUsername()) && adminService.adminExists(aForm.getUsername());
    }

    private boolean checkPassword(final AdminForm form, final Popups popups) {
        ComAdmin admin = adminService.getAdmin(form.getAdminID(), form.getCompanyID());
        PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups, "password");
        if (admin != null && admin.getAdminID() != 0) {
            // Existing user changes his password
            return passwordCheck.checkAdminPassword(form.getPassword(), admin, handler);
        } else {
            // New user changes wants to set his initial password
            return passwordCheck.checkAdminPassword(form.getPassword(), null, handler);
        }
    }

    private PaginatedListImpl<AdminEntry> getAdminListFromAdminListForm(ComAdmin admin, AdminListForm form) {
        return getAdminListFromAdminListForm(admin, form, form.getNumberOfRows());
    }

    private PaginatedListImpl<AdminEntry> getAdminListFromAdminListForm(ComAdmin admin, AdminListForm form, int numberOfRows) {
        return adminService.getAdminList(admin.getCompanyID(),
                form.getSearchFirstName(),
                form.getSearchLastName(),
                form.getSearchEmail(),
                form.getSearchCompany(),
                form.getFilterCompanyId(),
                form.getFilterAdminGroupId(),
                form.getFilterMailinglistId(),
                form.getFilterLanguage(),
                form.getSort(),
                form.getDir(),
                form.getPage(),
                numberOfRows);
    }

    private String saveAdminAndGetView(AdminForm form, ComAdmin admin, HttpSession session, Popups popups) {
        final boolean isNew = form.getAdminID() == 0;

        ComAdmin oldSavingAdmin = null;
        ComAdminPreferences oldSavingAdminPreferences = null;

        if (!isNew) {
            oldSavingAdmin = adminService.getAdmin(form.getAdminID(), form.getCompanyID());
            oldSavingAdminPreferences = adminService.getAdminPreferences(form.getAdminID());
            if (!StringUtils.equals(oldSavingAdmin.getUsername(), form.getUsername())
                    && adminService.checkBlacklistedAdminNames(form.getUsername())) {
                popups.alert("error.username.duplicate");
                return "messages";
            }
        } else if (adminService.adminLimitReached(form.getCompanyID())) {
            popups.alert("error.admin.limit");
            return "messages";
        }

        final AdminSavingResult result = adminService.saveAdmin(form, admin);

        if (!result.isSuccess()) {
            popups.alert(result.getError());
            return "messages";
        }

        final ComAdmin savedAdmin = result.getResult();

        if (isNew) {
            // Log successful creation of new user
            userActivityLogService.writeUserActivityLog(admin, "create user", savedAdmin.getUsername() + " (" + savedAdmin.getAdminID() + ")");
        } else {
            afterAdminUpdate(form, oldSavingAdmin, oldSavingAdminPreferences, admin, savedAdmin, session, result.isPasswordChanged());
        }

        popups.success("default.changes_saved");

        return "redirect:/admin/" + savedAdmin.getAdminID() + "/view.action";
    }

    private void afterAdminUpdate(AdminForm form, ComAdmin oldSavingAdmin, ComAdminPreferences oldSavingAdminPreferences,
                                  ComAdmin admin, ComAdmin savedAdmin, HttpSession session, boolean isPasswordChanged) {
        adminChangesLogService.getChangesAsUserActions(form, oldSavingAdmin, oldSavingAdminPreferences)
                .forEach(action -> userActivityLogService.writeUserActivityLog(admin, action));

        if (isPasswordChanged) {
            userActivityLogService.writeUserActivityLog(admin, "change password", form.getUsername() + " (" + form.getAdminID() + ")");
        }

        // Set the new values for this session if user edit own profile via Administration -> User -> OwnProfile
        if (savedAdmin.getAdminID() == admin.getAdminID()) {
            ComAdminPreferences adminPreferences = adminService.getAdminPreferences(savedAdmin.getAdminID());

            savedAdmin.setSupervisor(admin.getSupervisor());

            session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, savedAdmin);
            session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
        }
    }

    protected void loadDataForViewPage(final ComAdmin admin, final Model model){
        model.addAttribute("adminGroups", adminGroupService.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID()));
        model.addAttribute("layouts", adminService.getEmmLayoutsBase(admin.getCompanyID()));
        model.addAttribute("availableTimeZones", TimeZone.getAvailableIDs());
        model.addAttribute("createdCompanies", adminService.getCreatedCompanies(admin.getCompanyID()));
		model.addAttribute("altgs", targetService.getLimitingTargetLights(admin.getCompanyID()));
    }

    private void initializeForm(AdminForm form, ComAdmin adminToEdit) {
        form.setUsername(adminToEdit.getUsername());
        form.setGender(adminToEdit.getGender());
        form.setTitle(adminToEdit.getTitle());
        form.setFullname(adminToEdit.getFullname());
        form.setFirstname(adminToEdit.getFirstName());
        form.setStatEmail(adminToEdit.getStatEmail());
        form.setPassword("");
        form.setAdminPhone(adminToEdit.getAdminPhone());
        form.setPasswordConfirm("");
        form.setCompanyID(adminToEdit.getCompanyID());
        form.setAdminLocale(new Locale(adminToEdit.getAdminLang(), adminToEdit.getAdminCountry()));
        form.setAdminTimezone(adminToEdit.getAdminTimezone());
        form.setGroupID(adminToEdit.getGroup() == null ? 1 : adminToEdit.getGroup().getGroupID());
        form.setCompanyName(adminToEdit.getCompanyName());
        form.setEmail(adminToEdit.getEmail());
        form.setLayoutBaseId(adminToEdit.getLayoutBaseID());
        form.setInitialCompanyName(adminToEdit.getInitialCompanyName());
        form.setOneTimePassword(adminToEdit.isOneTimePassword());
        form.setAltgId(adminToEdit.getAltgId());

        form.setAdminPreferences(
                conversionService.convert(
                        adminService.getAdminPreferences(adminToEdit.getAdminID()), AdminPreferences.class)
        );
    }

    private void prepareRightsViewPageData(ComAdmin admin, AdminRightsForm form, Model model, ComAdmin adminToEdit) {
        form.setUsername(adminToEdit.getUsername());
        Map<String, PermissionsOverviewData.PermissionCategoryEntry> permissionsOverview =
                adminService.getPermissionOverviewData(admin, adminToEdit);
        model.addAttribute("permissionCategories", permissionsOverview.values());
    }

    private boolean saveAdminRightsAndWriteToActivityLog(ComAdmin admin, AdminRightsForm aForm, Popups popups) {
        Tuple<List<String>, List<String>> changes = adminService.saveAdminPermissions(admin.getCompanyID(),
                aForm.getAdminID(), aForm.getUserRights(), admin.getAdminID());

        if (changes == null) {
            popups.alert("error.admin.change.permission");
            return false;
        }
        ComAdmin savingAdmin = adminService.getAdmin(aForm.getAdminID(), admin.getCompanyID());

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

}
