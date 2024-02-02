/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.import_profile.component.ImportProfileChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileFormValidator;
import com.agnitas.emm.core.import_profile.form.ImportProfileForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

public class ImportProfileController {

    private static final Logger LOGGER = LogManager.getLogger(ImportProfileController.class);

    private final ImportProfileService importProfileService;
    private final UserActivityLogService userActivityLogService;
    private final AdminService adminService;
    private final WebStorage webStorage;
    private final ColumnInfoService columnInfoService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final ConversionService conversionService;
    private final ImportProfileChangesDetector changesDetector;
    private final ImportProfileFormValidator formValidator;
    private final RecipientService recipientService;

    public ImportProfileController(ImportProfileService importProfileService, UserActivityLogService userActivityLogService, AdminService adminService, WebStorage webStorage,
                                   ColumnInfoService columnInfoService, MailinglistApprovalService mailinglistApprovalService, ConversionService conversionService,
                                   ImportProfileChangesDetector changesDetector, ImportProfileFormValidator formValidator, RecipientService recipientService) {

        this.importProfileService = importProfileService;
        this.userActivityLogService = userActivityLogService;
        this.adminService = adminService;
        this.webStorage = webStorage;
        this.columnInfoService = columnInfoService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.conversionService = conversionService;
        this.changesDetector = changesDetector;
        this.formValidator = formValidator;
        this.recipientService = recipientService;
    }

    @RequestMapping("/list.action")
    public String list(@ModelAttribute("form") PaginationForm form, Admin admin, Model model) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.IMPORT_PROFILE_OVERVIEW, form);
        model.addAttribute("profiles", importProfileService.getAvailableImportProfiles(admin));
        model.addAttribute("defaultProfileId", admin.getDefaultImportProfileID());

        return "import_wizard_profile_list";
    }

    @PostMapping("/setDefault.action")
    @PermissionMapping("setDefault")
    public String setDefaultProfile(@RequestParam(name = "id") Integer id, Admin admin, Popups popups) throws Exception {
        if (id != null) {
            admin.setDefaultImportProfileID(id);

            Admin existingAdmin = adminService.getAdmin(admin.getAdminID(), admin.getCompanyID());
            existingAdmin.setDefaultImportProfileID(id);
            adminService.save(existingAdmin);

            popups.success(CHANGES_SAVED_MSG);
        }

        return "redirect:/import-profile/list.action";
    }

    @GetMapping("/create.action")
    public String create(@ModelAttribute("form") ImportProfileForm form, Model model, Admin admin) throws Exception {
        prepareModelAttributesForViewPage(model, form, admin, null);

        form.setFirstKeyColumn("email");
        form.setCheckForDuplicates(1);
        form.setDefaultMailType(1);
        form.setReportLocale(localeAsStr(admin.getLocale()));
        form.setReportTimezone(admin.getAdminTimezone());

        return "import_wizard_profile_view";
    }

    @GetMapping("/{id:\\d+}/view.action")
    public String view(@PathVariable("id") int id, Admin admin, Model model) throws Exception {
        ImportProfile profile = importProfileService.getImportProfileById(id);

        if (profile == null) {
            return "redirect:/import-profile/create.action";
        }

        ImportProfileForm form = conversionService.convert(profile, ImportProfileForm.class);
        form.setMailinglists(prepareSelectedMailinglistsMap(profile));

        prepareModelAttributesForViewPage(model, form, admin, profile);
        writeUserActivityLog(admin, "view import profile", getImportProfileDescription(profile));

        return "import_wizard_profile_view";
    }

    @PostMapping("/save.action")
    public String save(@ModelAttribute ImportProfileForm form, Admin admin, Popups popups) throws Exception {
        if (!formValidator.validate(form, admin, popups)) {
            return MESSAGES_VIEW;
        }

        ImportProfile profile = conversionService.convert(form, ImportProfile.class);
        ImportProfile existingProfile = null;

        if (profile.getId() != 0) {
            existingProfile = importProfileService.getImportProfileById(profile.getId());
        }

        profile.setCompanyId(admin.getCompanyID());
        profile.setAdminId(admin.getAdminID());

        if (profile.getActionForNewRecipients() <= 0 && importProfileService.isAllowedToShowMailinglists(admin)) {
            profile.setMailinglists(getSelectedMailinglists(form.getMailinglist()));
        }

        importProfileService.saveImportProfileWithoutColumnMappings(profile, admin);

        if (existingProfile != null) {
            writeImportChangeLog(existingProfile, profile, admin);
        }

        popups.success(CHANGES_SAVED_MSG);

        if (form.getImportMode() == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
            popups.warning("warning.import.mode.bounceractivation");
        }

        checkIfProfileKeyColumnIndexed(profile, popups);

        return String.format("redirect:/import-profile/%d/view.action", profile.getId());
    }

    @GetMapping("/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(@PathVariable("id") int id, Model model) {
        ImportProfile profile = importProfileService.getImportProfileById(id);

        model.addAttribute("name", profile.getName());
        model.addAttribute("id", id);

        return "import_wizard_profile_delete";
    }

    @PostMapping("/{id:\\d+}/delete.action")
    public String delete(@PathVariable("id") int id, Admin admin, Popups popups) {
        ImportProfile importProfile = importProfileService.getImportProfileById(id);

        if (importProfile != null) {
            if (!importProfileService.isManageAllowed(importProfile, admin)) {
                throw new UnsupportedOperationException();
            }

            AutoImportLight dependentAutoImport = findDependentAutoImport(id);

            if (dependentAutoImport != null) {
                popups.alert("error.profileStillUsed", getAutoImportDescription(dependentAutoImport));
                return MESSAGES_VIEW;
            }

            importProfileService.deleteImportProfileById(id);
            popups.success(SELECTION_DELETED_MSG);
            writeUserActivityLog(admin, "delete import profile", getImportProfileDescription(importProfile));
        }

        return "redirect:/import-profile/list.action";
    }

    protected void prepareModelAttributesForViewPage(Model model, ImportProfileForm form, Admin admin, ImportProfile importProfile) throws Exception {
        model.addAttribute("charsets", Charset.values());
        model.addAttribute("delimiters", TextRecognitionChar.values());
        model.addAttribute("dateFormats", DateFormat.values());
        model.addAttribute("nullValuesActions", NullValuesAction.values());
        model.addAttribute("checkForDuplicatesValues", CheckForDuplicates.values());
        model.addAttribute("availableTimeZones", TimeZone.getAvailableIDs());
        model.addAttribute("importModes", importProfileService.getAvailableImportModes(admin));
        model.addAttribute("availableImportProfileFields", getAvailableImportProfileFields(admin));
        model.addAttribute("availableMailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        model.addAttribute("isUserHasPermissionForSelectedMode", importProfileService.isImportModeAllowed(form.getImportMode(), admin));
        model.addAttribute("allowedModesForAllMailinglists", getAllowedModesForAllMailinglists());
        model.addAttribute("availableGenderIntValues", getAvailableGenders(admin));
        model.addAttribute("isCheckForDuplicatesAllowed", importProfileService.isCheckForDuplicatesAllowed(admin));
        model.addAttribute("isUpdateDuplicatesChangeAllowed", importProfileService.isUpdateDuplicatesChangeAllowed(admin));
        model.addAttribute("isPreprocessingAllowed", importProfileService.isPreprocessingAllowed(admin));
        model.addAttribute("isAllMailinglistsAllowed", importProfileService.isAllMailinglistsAllowed(admin));
        model.addAttribute("isCustomerIdImportAllowed", importProfileService.isCustomerIdImportAllowed(admin));
        model.addAttribute("isAllowedToShowMailinglists", importProfileService.isAllowedToShowMailinglists(admin));

        if (importProfile != null) {
            if (!importProfile.isMailinglistsAll() && importProfile.getActionForNewRecipients() != 0) {
                model.addAttribute("mailinglistsToShow", new HashSet<>(importProfile.getMailinglistIds()));
            }

            model.addAttribute("mediatypes", importProfile.getMediatypes());
            model.addAttribute("form", form);
            model.addAttribute("genderMappingJoined", importProfile.getGenderMappingJoined());
        }
    }

    private Map<Integer, String> prepareSelectedMailinglistsMap(ImportProfile profile) {
        if (profile.isMailinglistsAll()) {
            return new HashMap<>();
        }

        List<Integer> selectedMailinglists;
        if (profile.getActionForNewRecipients() != 0) {
            selectedMailinglists = importProfileService.getSelectedMailingListIds(profile.getId(), profile.getCompanyId());
        } else {
            selectedMailinglists = new ArrayList<>(profile.getMailinglistIds());
        }

        return selectedMailinglists.stream()
                .collect(Collectors.toMap(Function.identity(), m -> "true"));
    }

    private String localeAsStr(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    private List<Integer> getAvailableGenders(Admin admin) {
        List<Integer> genders = new ArrayList<>();

        int maxGenderValue;
        if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_EXTENDED;
        } else {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_BASIC;
        }

        for (int i = 0; i <= maxGenderValue; i++) {
            genders.add(i);
        }

        return genders;
    }

    private List<Integer> getAllowedModesForAllMailinglists() {
        List<ImportMode> allowedImportModes = List.of(
                ImportMode.ADD,
                ImportMode.ADD_AND_UPDATE,
                ImportMode.UPDATE,
                ImportMode.MARK_OPT_OUT,
                ImportMode.MARK_BOUNCED,
                ImportMode.REACTIVATE_BOUNCED,
                ImportMode.MARK_SUSPENDED,
                ImportMode.ADD_AND_UPDATE_FORCED,
                ImportMode.REACTIVATE_SUSPENDED
        );

        return allowedImportModes.stream()
                .map(ImportMode::getIntValue)
                .collect(Collectors.toList());
    }

    private List<Integer> getSelectedMailinglists(Map<Integer, String> mailinglistsMap) {
        return mailinglistsMap.entrySet().stream()
                .filter(e -> "true".equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void checkIfProfileKeyColumnIndexed(ImportProfile importProfile, Popups popups) {
        if (importProfileService.isKeyColumnsIndexed(importProfile)) {
            return;
        }

        if (recipientService.hasBeenReachedLimitOnNonIndexedImport(importProfile.getCompanyId())) {
            popups.alert("error.import.keyColumn.index");
        } else {
            popups.warning("warning.import.keyColumn.index");
        }
    }

    protected AutoImportLight findDependentAutoImport(int profileId) {
        return null;
    }

    private List<ProfileField> getAvailableImportProfileFields(Admin admin) throws Exception {
        List<ProfileField> dbColumnsAvailable = columnInfoService.getComColumnInfos(admin.getCompanyID(), admin.getAdminID(), true);
        List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);

        return dbColumnsAvailable.stream()
                .filter(pf -> !isHiddenProfileField(pf.getColumn(), hiddenColumns))
                .collect(Collectors.toList());
    }

    private boolean isHiddenProfileField(String fieldName, List<String> hiddenFields) {
        return hiddenFields.stream()
                .anyMatch(hf -> hf.equalsIgnoreCase(fieldName));
    }

    private void writeImportChangeLog(ImportProfile existingImportProfile, ImportProfile importProfile, Admin admin) {
        StringBuilder messageBuilder = changesDetector.detectChanges(existingImportProfile, importProfile, admin);

        if (StringUtils.isNotBlank(messageBuilder.toString())) {
            messageBuilder.insert(0, ". ");
            messageBuilder.insert(0, getImportProfileDescription(existingImportProfile));

            writeUserActivityLog(admin, "edit import profile", messageBuilder.toString());
        }
    }

    private String getAutoImportDescription(AutoImportLight autoImport) {
        return String.format("%s (ID: %d)", autoImport.getShortname(), autoImport.getAutoImportId());
    }

    private String getImportProfileDescription(ImportProfile importProfile) {
        return String.format("%s (%d)", importProfile.getName(), importProfile.getId());
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), LOGGER);
    }
}
