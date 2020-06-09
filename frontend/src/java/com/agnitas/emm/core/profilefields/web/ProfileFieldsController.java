/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.web;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.agnitas.beans.ProfileField;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.GuiConstants;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryUtil;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("profiledb")
public class ProfileFieldsController {
    private static final Logger logger = Logger.getLogger(ProfileFieldsController.class);

    private ProfileFieldService profileFieldService;
    private WebStorage webStorage;
    private ConversionService conversionService;
    private ConfigService configService;
    private ProfileFieldValidationService validationService;
    private ComTargetService targetService;
    private UserActivityLogService userActivityLogService;

    public ProfileFieldsController(ProfileFieldService profileFieldService, WebStorage webStorage,
                                   ConversionService conversionService, ConfigService configService,
                                   ProfileFieldValidationService validationService, ComTargetService targetService,
                                   UserActivityLogService userActivityLogService) {
        this.profileFieldService = profileFieldService;
        this.webStorage = webStorage;
        this.conversionService = conversionService;
        this.configService = configService;
        this.validationService = validationService;
        this.targetService = targetService;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping("/profiledb.action")
    public String list(ComAdmin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model, Popups popups) {
        int companyId = admin.getCompanyID();

        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.PROFILE_FIELD_OVERVIEW, profileForm);

        model.addAttribute("columnInfo", profileFieldService.getSortedColumnInfo(companyId));
        model.addAttribute("fieldsWithIndividualSortOrder", profileFieldService.getFieldWithIndividualSortOrder(companyId, admin.getAdminID()));

        if (!validationService.mayAddNewColumn(companyId)) {
            popups.alert("error.profiledb.maxCount");
        }

        if (profileFieldService.isAddingNearLimit(companyId)) {
            int currentField = profileFieldService.getCurrentSpecificFieldCount(companyId);
            int maximumField = profileFieldService.getMaximumSpecificFieldCount(companyId);

            popups.warning("error.profiledb.nearLimit", currentField, maximumField);
        }

        return "settings_profile_field_list";
    }

    @RequestMapping("/profiledb/{fieldname}/view.action")
    public String view(ComAdmin admin, @PathVariable("fieldname") String fieldName, ModelMap model) {
        final int companyId = admin.getCompanyID();
        ComProfileField field = profileFieldService.getProfileField(companyId, fieldName);

        if (!model.containsAttribute("profileForm")) {
            ProfileFieldForm form;

            if (field == null) {
                return "redirect:/profiledb/new.action";
            }

            form = conversionService.convert(field, ProfileFieldForm.class);
            form.setFieldname(fieldName);

            if (field.getHistorize()) {
                form.setDependentWorkflowName(profileFieldService.getTrackingDependentWorkflows(companyId, fieldName));
            }

            model.addAttribute("profileForm", form);
        }

        String creationDate = "";
        String changeDate = "";
        if (field != null) {
            creationDate = DateUtilities.format(field.getCreationDate(), admin.getDateTimeFormat());
            changeDate = DateUtilities.format(field.getChangeDate(), admin.getDateTimeFormat());
        }

        model.addAttribute("HISTORY_FEATURE_ENABLED", configService.isRecipientProfileHistoryEnabled(companyId));
        model.addAttribute("creationDate", creationDate);
        model.addAttribute("fieldsWithIndividualSortOrder", profileFieldService.getFieldWithIndividualSortOrder(companyId, admin.getAdminID()));
        model.addAttribute("changeDate", changeDate);

        return "settings_profile_field_view";
    }

    @RequestMapping("/profiledb/{fieldname}/dependents.action")
    public String dependents(ComAdmin admin, @PathVariable("fieldname") String fieldName, Model model, Popups popups) {
        int companyId = admin.getCompanyID();

        ComProfileField field = profileFieldService.getProfileField(companyId, fieldName);
        if (field == null) {
            popups.alert("Error");
            return "redirect:/profiledb.action";
        }

        model.addAttribute("fieldname", fieldName);
        model.addAttribute("dependents", profileFieldService.getDependents(companyId, fieldName));

        return "settings_profile_field_dependents_list";
    }

    @PostMapping("/profiledb/save.action")
    public String save(ComAdmin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Popups popups) {
        if (StringUtils.isBlank(profileForm.getFieldname())) {
            popups.alert("error.profiledb.invalid_fieldname", "''");
        } else {
            ComProfileField field = profileFieldService.getProfileField(admin.getCompanyID(), profileForm.getFieldname());

            boolean isNewField = Objects.isNull(field);

            if (existAllowedValues(profileForm)) {
                clearAllowedValues(profileForm);
            }

            validateField(admin, profileForm, field, popups);

            if (!popups.hasAlertPopups()) {
                if (isNewField ? createField(admin, profileForm) : updateField(admin, field, profileForm)) {
                    popups.success("default.changes_saved");
                    return "redirect:/profiledb.action";
                } else {
                    popups.alert("Error");
                }
            }
        }

        return "messages";
    }

    @RequestMapping("/profiledb/saveWizardField.action")
    public String saveWizardField(ComAdmin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, RedirectAttributes model, Popups popups) {
        String tile = save(admin, profileForm, popups);

        if (tile.equals("messages")) {
            return "messages";
        }

        model.addFlashAttribute("profileForm", profileForm);

        return "redirect:/profiledb/newWizardField.action";
    }

    @RequestMapping("/profiledb/{fieldname}/confirmDelete.action")
    public String confirmDelete(ComAdmin admin, @PathVariable("fieldname") String fieldName, Model model) {
        String result = "settings_profile_field_delete_ajax";
        final int companyId = admin.getCompanyID();

        List<String> dependentWorkflows = profileFieldService.getDependentWorkflows(companyId, fieldName);
        List<TargetLight> targetLights = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId); //TODO optimize

        if (CollectionUtils.isNotEmpty(targetLights)) {
            model.addAttribute("affectedTargetGroupsMessageKey", "settings.ProfileFieldErrorMsg");
            model.addAttribute("affectedTargetGroupsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
            model.addAttribute("affectedTargetGroups", targetLights);

            result = "messages";
        }

        if (CollectionUtils.isNotEmpty(dependentWorkflows)) {
            model.addAttribute("affectedDependentWorkflowsMessageKey", "error.profiledb.dependency.workflow");
            model.addAttribute("affectedDependentWorkflowsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
            model.addAttribute("affectedDependentWorkflows", dependentWorkflows);

            result = "messages";
        }

        if (CollectionUtils.isEmpty(targetLights) && CollectionUtils.isEmpty(dependentWorkflows)) {
            model.addAttribute("fieldname", fieldName);
        }

        return result;
    }

    @RequestMapping(value = "/profiledb/{fieldname}/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(ComAdmin admin, @PathVariable("fieldname") String fieldName, Popups popups) {
        final int companyId = admin.getCompanyID();

        if (validationService.notContainsInDb(companyId, fieldName)) {
            popups.alert("error.profiledb.NotExists", fieldName);
        } else if (validationService.hasNotAllowedNumberOfEntries(companyId)) {
            popups.alert("error.profiledb.delete.tooMuchRecipients", fieldName);
        } else if (validationService.hasTargetGroups(companyId, fieldName)) {
            popups.alert("error.profiledb.delete.usedInTargetGroups", fieldName);
        } else if (validationService.isStandardColumn(fieldName)) {
            popups.alert("error.profiledb.cannotDropColumn", fieldName);
        }

        if (!popups.hasAlertPopups()) {
            profileFieldService.removeProfileField(companyId, fieldName);

            popups.success("default.selection.deleted");
            writeUserActivityLog(admin, profileFieldService.getDeleteFieldLog(fieldName));
        }

        return "redirect:/profiledb.action";
    }

    @RequestMapping("/profiledb/new.action")
    public String newField(ComAdmin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model) {
        profileForm.setFieldType(DbColumnType.GENERIC_TYPE_VARCHAR);
        profileForm.setFieldLength(100);

        model.addAttribute("fieldsWithIndividualSortOrder", profileFieldService.getFieldWithIndividualSortOrder(admin.getCompanyID(), admin.getAdminID()));
        model.addAttribute("isNewField", true);
        model.addAttribute("HISTORY_FEATURE_ENABLED", configService.isRecipientProfileHistoryEnabled(admin.getCompanyID()));

        return "settings_profile_field_view";
    }

    @RequestMapping("/profiledb/newWizardField.action")
    public String newWizardField(ComAdmin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model) {
        model.addAttribute("isNewField", isNewField(admin, profileForm.getFieldname()));
        model.addAttribute("customerFields", profileFieldService.getFieldWithIndividualSortOrder(admin.getCompanyID(), admin.getAdminID()));

        return "mailing_wizard_new_field";
    }

    @RequestMapping("/profiledb/backToTarget.action")
    public String backToTarget() {
        return "mailing_wizard_new_target";
    }

    private boolean isNewField(ComAdmin admin, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return true;
        }

        if (validationService.isValidDbFieldName(fieldName)) {
            return !profileFieldService.exists(admin.getCompanyID(), fieldName);
        } else {
            return true;
        }
    }

    private boolean createField(ComAdmin admin, ProfileFieldForm form) {
        ComProfileField field = conversionService.convert(form, ComProfileField.class);
        field.setCompanyID(admin.getCompanyID());

        writeUserActivityLog(admin, profileFieldService.getCreateFieldLog(form.getShortname()));

        return profileFieldService.createNewField(field, admin);
    }

    private boolean updateField(ComAdmin admin, ComProfileField field, ProfileFieldForm form) {
        UserAction openEmmChangeLog = profileFieldService.getOpenEmmChangeLog(field, form);
        UserAction emmChangeLog = profileFieldService.getEmmChangeLog(field, form);

        if (Objects.nonNull(openEmmChangeLog)) {
            writeUserActivityLog(admin, openEmmChangeLog);
        }

        if (Objects.nonNull(emmChangeLog)) {
            writeUserActivityLog(admin, emmChangeLog);
        }

        field.setDataType(form.getFieldType());
        field.setDataTypeLength(form.getFieldLength());
        field.setDescription(form.getDescription());
        field.setShortname(form.getShortname());
        field.setDefaultValue(form.getFieldDefault());
        field.setSort(form.getFieldSort());
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest());
        field.setHistorize(form.isIncludeInHistory());
        field.setAllowedValues(form.getAllowedValues());

        if (admin.permissionAllowed(Permission.PROFILEFIELD_VISIBLE)) {
            field.setModeEdit(form.isFieldVisible() ? ProfileField.MODE_EDIT_EDITABLE : ProfileField.MODE_EDIT_NOT_VISIBLE);
        }

        return profileFieldService.updateField(field, admin);
    }

    private void validateField(ComAdmin admin, ProfileFieldForm form, ComProfileField profileField, Popups popups) {
        final int companyId = admin.getCompanyID();

        if (!validationService.isValidDbFieldName(form.getFieldname())) {
            popups.field("fieldname", "error.profiledb.invalid_fieldname", form.getFieldname());
        }

        if (StringUtils.length(StringUtils.trim(form.getShortname())) < 3) {
            popups.field("shortname", "error.profiledb.shortname_too_short");
        } else if (!validationService.isValidShortname(companyId, form.getShortname(), form.getFieldname())) {
            popups.field("shortname", "error.profiledb.invalidShortname");
        }

        if (StringUtils.length(StringUtils.trim(form.getDescription())) < 3) {
            popups.field("description", "error.descriptionToShort");
        }

        if (validationService.isInvalidIntegerField(form.getFieldType(), form.getFieldDefault())) {
            popups.alert("error.profiledb.numeric.invalid");
        }

        if (validationService.isInvalidVarcharField(form.getFieldType(), form.getFieldLength())) {
            popups.alert("error.profiledb.length");
        }

        if (form.isUseAllowedValues() && ArrayUtils.isEmpty(form.getAllowedValues())) {
            popups.field("allowedValues", "error.profiledb.missingFixedValues");
        }

        if (profileField == null) {
            validateForCreating(companyId, form, popups, admin.getDateFormat());
        } else {
            validationForUpdating(companyId, profileField, form, popups, admin.getDateFormat());
        }
    }

    private void validateForCreating(int companyId, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat) {
        if (!form.isFieldNull() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (validationService.isInvalidLengthForDbFieldName(form.getFieldname())) {
            popups.alert("error.profiledb.invalidFieldName", form.getFieldname());
        } else if (validationService.isShortnameInDB(companyId, form.getShortname())) {
            popups.alert("error.profiledb.exists");
        } else if (!validationService.isAllowedDefaultValue(form.getFieldType(), form.getFieldDefault(), dateFormat)) {
            popups.alert("error.profiledb.invalidDefaultValue", dateFormat.toPattern());
        } else if (!validationService.isDefaultValueAllowedInDb(companyId, form.getFieldname(), form.getFieldDefault())) {
            popups.alert("error.profiledb.tooManyEntriesToChangeDefaultValue", configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId));
        } else if (containNotAllowedValue(form, form.getFieldType(), dateFormat)) {
            popups.alert("error.profiledb.invalidFixedValue");
        } else if (!validationService.mayAddNewColumn(companyId)) {
            popups.alert("error.profiledb.maxCount");
        }
    }

    private void validationForUpdating(int companyId, ProfileField field, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat) {
        if (!field.getNullable() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (!validationService.isAllowedDefaultValue(field.getDataType(), form.getFieldDefault(), dateFormat)) {
            popups.alert("error.profiledb.invalidDefaultValue", dateFormat.toPattern());
        } else if (containNotAllowedValue(form, field.getDataType(), dateFormat)) {
            popups.alert("error.profiledb.invalidFixedValue");
        } else if (form.isIncludeInHistory()) {
            final int maximumHistoryFields = configService.getMaximumNumberOfUserDefinedHistoryProfileFields(companyId);
            final Set<String> columnSet = profileFieldService.getSelectedFieldsWithHistoryFlag(companyId);

            String fieldNameInLower = form.getFieldname().toLowerCase();
            if (!RecipientProfileHistoryUtil.DEFAULT_COLUMNS_FOR_HISTORY.contains(fieldNameInLower)) {
                columnSet.add(fieldNameInLower);
            }

            if (columnSet.size() > maximumHistoryFields) {
                popups.alert("error.profileHistory.tooManyFields");
            }
        }
    }

    private boolean containNotAllowedValue(ProfileFieldForm form, String dataType, SimpleDateFormat dateFormat) {
        String[] values = form.getAllowedValues();
        boolean[] validationResult;
        boolean invalid = false;

        if (!form.isUseAllowedValues() || values == null) {
            return false;
        }

        validationResult = new boolean[values.length];

        for (int i = 0; i < values.length; i++) {
            validationResult[i] = validationService.isAllowedDefaultValue(dataType, values[i], dateFormat);

            if (!validationResult[i]) {
                invalid = true;
            }
        }

        form.setAllowedValuesValidationResults(validationResult);

        return invalid;
    }

    private void clearAllowedValues(ProfileFieldForm form) {
        Set<String> values = new LinkedHashSet<>();

        for (String value : form.getAllowedValues()) {
            if (StringUtils.isNotEmpty(value)) {
                values.add(value);
            }
        }

        if (values.size() != form.getAllowedValues().length) {
            form.setAllowedValues(values.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }
    }

    private boolean existAllowedValues(ProfileFieldForm form) {
        return form.isUseAllowedValues() && form.getAllowedValues() != null;
    }

    private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction, logger);
    }
}
