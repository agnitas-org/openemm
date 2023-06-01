/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.web;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToPopups;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryUtil;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import static com.agnitas.emm.core.Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

@Controller
@PermissionMapping("profiledb")
public class ProfileFieldsController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(ProfileFieldsController.class);

    private static final String INVALID_DEFAULT_VALUE_ERROR_MSG = "error.profiledb.invalidDefaultValue";
    
    private final ProfileFieldService profileFieldService;
    private final WebStorage webStorage;
    private final ConversionService conversionService;
    private final ConfigService configService;
    private final ProfileFieldValidationService validationService;
    private final UserActivityLogService userActivityLogService;
    private final ObjectUsageService objectUsageService;
    
    public ProfileFieldsController(ProfileFieldService profileFieldService, WebStorage webStorage, ConversionService conversionService, ConfigService configService,
                                   ProfileFieldValidationService validationService, UserActivityLogService userActivityLogService, ObjectUsageService objectUsageService) {
        this.profileFieldService = profileFieldService;
        this.webStorage = webStorage;
        this.conversionService = conversionService;
        this.configService = configService;
        this.validationService = validationService;
        this.userActivityLogService = userActivityLogService;
        this.objectUsageService = Objects.requireNonNull(objectUsageService, "ObjectUsageService is null");
    }

    @RequestMapping("/profiledb.action")
    public String list(@ModelAttribute("profileForm") ProfileFieldForm profileForm, Admin admin, Model model, Popups popups,
                       @RequestParam(value = "syncSorting", required = false) boolean syncSorting) throws Exception {
    	FormUtils.syncNumberOfRows(webStorage, ComWebStorage.PROFILE_FIELD_OVERVIEW, profileForm);

    	if (syncSorting) {
            FormUtils.syncSortingParams(webStorage, ComWebStorage.PROFILE_FIELD_OVERVIEW, profileForm);
        }

    	int companyId = admin.getCompanyID();

        PaginatedListImpl<ProfileField> profileFields = profileFieldService.getPaginatedFieldsList(
                companyId,
                profileForm.getSort(),
                profileForm.getDir(),
                profileForm.getPage(),
                profileForm.getNumberOfRows()
        );

        model.addAttribute("profileFields", profileFields);
        model.addAttribute("fieldsWithIndividualSortOrder", profileFieldService.getFieldWithIndividualSortOrder(companyId, admin.getAdminID()));

        if (!validationService.mayAddNewColumn(companyId)) {
            popups.alert("error.profiledb.maxCount");
        }

        if (profileFieldService.isAddingNearLimit(companyId)) {
            int currentField = profileFieldService.getCurrentSpecificFieldCount(companyId);
            int maximumField = profileFieldService.getMaximumCompanySpecificFieldCount(companyId);

            popups.warning("error.profiledb.nearLimit", currentField, maximumField);
        }

        return "settings_profile_field_list";
    }

    @RequestMapping("/profiledb/{fieldname}/view.action")
    public String view(Admin admin, @PathVariable("fieldname") String fieldName, ModelMap model) {
        final int companyId = admin.getCompanyID();
        ProfileField field = profileFieldService.getProfileField(companyId, fieldName);

        if (!model.containsAttribute("profileForm")) {
            if (field == null) {
                return "redirect:/profiledb/new.action";
            }

            ProfileFieldForm form = conversionService.convert(field, ProfileFieldForm.class);
            form.setFieldname(fieldName);
            prepareDefaultValue(form, admin.getLocale());

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

    private void prepareDefaultValue(ProfileFieldForm form, Locale locale) {
        if (("NUMBER".equalsIgnoreCase(form.getFieldType()) ||
                "FLOAT".equalsIgnoreCase(form.getFieldType()) ||
                "DOUBLE".equalsIgnoreCase(form.getFieldType())) &&
                StringUtils.isNotBlank(form.getFieldDefault())) {
        	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
            String decimalSeparator = Character.toString(decimalFormatSymbols.getDecimalSeparator());
            form.setFieldDefault(form.getFieldDefault().replace(".", decimalSeparator));
        }
    }

    @RequestMapping("/profiledb/{fieldname}/dependents.action")
    public String dependents(Admin admin, @PathVariable("fieldname") String fieldName, Model model, Popups popups) {
        int companyId = admin.getCompanyID();

        ProfileField field = profileFieldService.getProfileField(companyId, fieldName);
        if (field == null) {
            popups.alert("Error");
            return "redirect:/profiledb.action";
        }

        model.addAttribute("fieldname", fieldName);
        model.addAttribute("dependents", profileFieldService.getDependents(companyId, fieldName));

        return "settings_profile_field_dependents_list";
    }

    @PostMapping("/profiledb/save.action")
    public String save(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Popups popups) {
        if (StringUtils.isBlank(profileForm.getFieldname())) {
            popups.alert("error.profiledb.invalid_fieldname", "''");
        } else if (profileFieldService.isReservedKeyWord(profileForm.getFieldname())) {
            popups.alert("error.name.reserved", profileForm.getFieldname());
        } else {
            ProfileField field = profileFieldService.getProfileField(admin.getCompanyID(), profileForm.getFieldname());

            boolean isNewField = Objects.isNull(field);

            if (existAllowedValues(profileForm)) {
                clearAllowedValues(profileForm);
            }

            validateField(admin, profileForm, field, popups);

            if (!popups.hasAlertPopups()) {
                if (isNewField ? createField(admin, profileForm) : updateField(admin, field, profileForm)) {
                    popups.success("default.changes_saved");
                    return "redirect:/profiledb.action?syncSorting=true";
                } else {
                    popups.alert("Error");
                }
            }
        }

        return "messages";
    }

    @RequestMapping("/profiledb/saveWizardField.action")
    public String saveWizardField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, RedirectAttributes model, Popups popups) {
        String tile = save(admin, profileForm, popups);

        if (tile.equals("messages")) {
            return "messages";
        }

        model.addFlashAttribute("profileForm", profileForm);

        return "redirect:/profiledb/newWizardField.action";
    }

    @RequestMapping("/profiledb/{column}/confirmDelete.action")
    public String confirmDelete(Admin admin, @PathVariable String column, final Popups popups) {
        ObjectUsages usages = objectUsageService.listUsageOfProfileFieldByDatabaseName(admin.getCompanyID(), column);
        if (!usages.isEmpty()) {
        	ObjectUsagesToPopups.objectUsagesToPopups(
        	        "error.profilefield.used", "error.profilefield.used.withMore", usages, popups, admin.getLocale());
        	return MESSAGES_VIEW;
        }
        return "settings_profile_field_delete_ajax";
    }

    @RequestMapping(value = "/profiledb/{fieldname}/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @PathVariable("fieldname") String fieldName, Popups popups) {
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
    public String newField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model) {
        profileForm.setFieldType(DbColumnType.GENERIC_TYPE_VARCHAR);
        profileForm.setFieldLength(100);

        model.addAttribute("fieldsWithIndividualSortOrder", profileFieldService.getFieldWithIndividualSortOrder(admin.getCompanyID(), admin.getAdminID()));
        model.addAttribute("isNewField", true);
        model.addAttribute("HISTORY_FEATURE_ENABLED", configService.isRecipientProfileHistoryEnabled(admin.getCompanyID()));

        return "settings_profile_field_view";
    }

    @RequestMapping("/profiledb/newWizardField.action")
    public String newWizardField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model) {
        model.addAttribute("isNewField", isNewField(admin, profileForm.getFieldname()));
        model.addAttribute("customerFields", profileFieldService.getFieldWithIndividualSortOrder(admin.getCompanyID(), admin.getAdminID()));

        return "mailing_wizard_new_field";
    }

    @RequestMapping("/profiledb/backToTarget.action")
    public String backToTarget() {
        return "mailing_wizard_new_target";
    }

    private boolean isNewField(Admin admin, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return true;
        }

        if (validationService.isValidDbFieldName(fieldName)) {
            return !profileFieldService.exists(admin.getCompanyID(), fieldName);
        } else {
            return true;
        }
    }

    private boolean createField(Admin admin, ProfileFieldForm form) {
        ProfileField field = conversionService.convert(form, ProfileField.class);
        field.setCompanyID(admin.getCompanyID());

        writeUserActivityLog(admin, profileFieldService.getCreateFieldLog(form.getShortname()));

        return profileFieldService.createNewField(field, admin);
    }

    private boolean updateField(Admin admin, ProfileField field, ProfileFieldForm form) {
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
        field.setSort(form.getFieldSort());
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest());
        field.setHistorize(form.isIncludeInHistory());
        field.setAllowedValues(form.getAllowedValues());

        if (admin.permissionAllowed(Permission.PROFILEFIELD_VISIBLE)) {
            field.setModeEdit(form.isFieldVisible() ? ProfileFieldMode.Editable : ProfileFieldMode.NotVisible);
        }

        return profileFieldService.updateField(field, admin);
    }

    private void validateField(Admin admin, ProfileFieldForm form, ProfileField profileField, Popups popups) {
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

        if (validationService.isInvalidIntegerField(form.getFieldType(), getDefaultValueToValidate(form, admin.getLocale()))) {
            popups.alert("error.profiledb.numeric.invalid");
        }

        if (validationService.isInvalidFloatField(form.getFieldType(), form.getFieldDefault(), admin.getLocale())) {
            popups.alert(INVALID_DEFAULT_VALUE_ERROR_MSG, getFloatFormatForAlertMessage(admin.getLocale()));
        }

        if (validationService.isInvalidVarcharField(form.getFieldType(), form.getFieldLength())) {
            popups.alert("error.profiledb.length");
        }

        if (form.isUseAllowedValues() && ArrayUtils.isEmpty(form.getAllowedValues())) {
            popups.field("allowedValues", "error.profiledb.missingFixedValues");
        }
        if (popups.hasAlertPopups()) {
            return;
        }

        if (profileField == null) {
            validateForCreating(companyId, form, popups, admin.getDateFormat(), admin.getLocale());
        } else {
            validationForUpdating(companyId, profileField, form, popups, admin.getDateFormat(), admin.getLocale());
        }
    }
    
    private String getFloatFormatForAlertMessage(Locale locale) {
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
    	String groupingSeparator = Character.toString(decimalFormatSymbols.getGroupingSeparator());
    	String decimalSeparator = Character.toString(decimalFormatSymbols.getDecimalSeparator());
        return "1" + groupingSeparator + "234" + groupingSeparator + "567" + decimalSeparator + "89"
                + " or " + "1234567" + decimalSeparator + "89";
    }

    private void validateForCreating(int companyId, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat, Locale locale) {
        if (!form.isFieldNull() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (validationService.isInvalidLengthForDbFieldName(form.getFieldname())) {
            popups.alert("error.profiledb.invalidFieldName", form.getFieldname());
        } else if (validationService.isShortnameInDB(companyId, form.getShortname())) {
            popups.alert("error.profiledb.exists");
        } else if (!validationService.isAllowedDefaultValue(form.getFieldType(), getDefaultValueToValidate(form, locale), dateFormat)) {
            popups.alert(INVALID_DEFAULT_VALUE_ERROR_MSG, dateFormat.toPattern());
        } else if (!validationService.isDefaultValueAllowedInDb(companyId, form.getFieldname(), form.getFieldDefault())) {
            popups.alert("error.profiledb.tooManyEntriesToChangeDefaultValue", configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId));
        } else if (containNotAllowedValue(form, form.getFieldType(), dateFormat)) {
            popups.alert("error.profiledb.invalidFixedValue");
        } else if (!validationService.mayAddNewColumn(companyId)) {
            popups.alert("error.profiledb.maxCount");
        }
    }

    private void validationForUpdating(int companyId, ProfileField field, ProfileFieldForm form, Popups popups,
                                       SimpleDateFormat dateFormat, Locale locale) {
        if (!field.getNullable() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (!validationService.isAllowedDefaultValue(field.getDataType(), getDefaultValueToValidate(form, locale), dateFormat)) {
            popups.alert(INVALID_DEFAULT_VALUE_ERROR_MSG, dateFormat.toPattern());
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
        alertIfUsedProfileFieldChanged(popups, companyId, field, form, locale);
    }

    private String getDefaultValueToValidate(ProfileFieldForm form, Locale locale) {
        String fieldDefault = form.getFieldDefault();
        switch (form.getFieldType()) {
            case "NUMBER":
            case "FLOAT":
            case "DOUBLE":
                return AgnUtils.getNormalizedDecimalNumber(fieldDefault, locale);
            case "INTEGER":
                if (AgnUtils.isValidNumberWithGroupingSeparator(fieldDefault, locale)) {
                    return AgnUtils.normalizeNumber(locale, fieldDefault);
                }
                return fieldDefault;
            default:
                return fieldDefault;
        }
    }

    private void alertIfUsedProfileFieldChanged(Popups popups, int companyId, ProfileField field, ProfileFieldForm form, Locale locale) {
        if (!StringUtils.equals(field.getShortname(), form.getShortname())) {
            List<ObjectUsage> usages = objectUsageService.listUsageOfProfileFieldByVisibleName(companyId, field.getShortname());
            if (!usages.isEmpty()) {
                ObjectUsagesToPopups.objectUsagesToPopups("GWUA.error.profileField.rename",
                        "GWUA.error.profileField.rename.withMore", new ObjectUsages(usages), popups, locale);
            }
        }
        if (!form.isIncludeInHistory() && configService.isRecipientProfileHistoryEnabled(companyId)) {
            String dependentWorkflow = profileFieldService.getTrackingDependentWorkflows(companyId, field.getColumn());
            if (StringUtils.isNotBlank(dependentWorkflow)) {
                popups.alert("warning.profilefield.inuse", dependentWorkflow);
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

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction, logger);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return ("allowedValues".equals(param) || "fieldDefault".equals(param))
                && admin.permissionAllowed(RECIPIENT_PROFILEFIELD_HTML_ALLOWED);
    }
}
