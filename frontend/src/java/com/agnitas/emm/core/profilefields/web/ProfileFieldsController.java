/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.web;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToPopups;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.form.ProfileFieldFormSearchParams;
import com.agnitas.emm.core.profilefields.form.ProfileFieldStatForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.dataset.ProfileFieldEvaluationDataSet;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class ProfileFieldsController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(ProfileFieldsController.class);

    private static final String INVALID_DEFAULT_VALUE_ERROR_MSG = "error.profiledb.invalidDefaultValue";

    protected final ConfigService configService;
    private final RecipientFieldService recipientFieldService;
    private final WebStorage webStorage;
    private final ProfileFieldValidationService validationService;
    private final UserActivityLogService userActivityLogService;
    private final ObjectUsageService objectUsageService;
    private final WorkflowService workflowService;
    private final TargetService targetService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final BirtStatisticsService birtStatisticsService;
    private final ProfileFieldEvaluationDataSet evaluationDataSet;

    public ProfileFieldsController(RecipientFieldService recipientFieldService, WebStorage webStorage, ConfigService configService,
                                   ProfileFieldValidationService validationService, UserActivityLogService userActivityLogService, ObjectUsageService objectUsageService,
                                   WorkflowService workflowService, TargetService targetService, MailinglistApprovalService mailinglistApprovalService, BirtStatisticsService birtStatisticsService,
                                   ProfileFieldEvaluationDataSet evaluationDataSet) {
        this.recipientFieldService = recipientFieldService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.validationService = validationService;
        this.userActivityLogService = userActivityLogService;
        this.objectUsageService = Objects.requireNonNull(objectUsageService, "ObjectUsageService is null");
        this.workflowService = workflowService;
        this.targetService = targetService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.birtStatisticsService = birtStatisticsService;
        this.evaluationDataSet = evaluationDataSet;
    }

    @ModelAttribute
    public ProfileFieldFormSearchParams getSearchParams() {
        return new ProfileFieldFormSearchParams();
    }

    @RequestMapping("/profiledb.action")
    public String list(@ModelAttribute("profileForm") ProfileFieldForm profileForm, @ModelAttribute ProfileFieldFormSearchParams searchParams,
                       Admin admin, Model model, Popups popups, @RequestParam(required = false) boolean restoreSort) {
        if (admin.isRedesignedUiUsed()) {
            FormUtils.syncSearchParams(searchParams, profileForm, true);
        }
        FormUtils.syncPaginationData(webStorage, WebStorage.PROFILE_FIELD_OVERVIEW, profileForm, restoreSort);

    	int companyId = admin.getCompanyID();

        List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(profileForm, companyId);

        Map<String, Comparator<RecipientFieldDescription>> sortableColumns = new HashMap<>();

        sortableColumns.put("shortname", Comparator.comparing(pf -> pf.getShortName() == null ? "" : pf.getShortName().toLowerCase()));
        sortableColumns.put("column", Comparator.comparing(pf -> pf.getColumnName().toLowerCase()));
        sortableColumns.put("dataType", Comparator.comparing(pf -> pf.getDatabaseDataType().toLowerCase()));
        sortableColumns.put("dataTypeLength", Comparator.comparing(RecipientFieldDescription::getCharacterLength));
        sortableColumns.put("defaultValue", Comparator.comparing(pf -> pf.getDefaultValue() == null ? "" : pf.getDefaultValue().toLowerCase()));
        sortableColumns.put("modeEdit", Comparator.comparing(pf -> pf.getDefaultPermission().getStorageCode()));
        if (admin.isRedesignedUiUsed()) {
            sortableColumns.put("description", Comparator.comparing(pf -> pf.getDescription() == null ? "" : pf.getDescription().toLowerCase()));
        } else {
            sortableColumns.put("sort", Comparator.comparing(RecipientFieldDescription::getSortOrder));
        }

        if (StringUtils.isNotBlank(profileForm.getSort())) {
            if (sortableColumns.containsKey(profileForm.getSort())) {
                Comparator<RecipientFieldDescription> comparator = sortableColumns.get(profileForm.getSort());
                if ("asc".equalsIgnoreCase(profileForm.getDir()) && !"ascending".equalsIgnoreCase(profileForm.getDir())) {
                    comparator = comparator.reversed();
                }
                recipientFields.sort(comparator);
            } else {
                logger.error("Profile field comparator was not found for property '{}'!", profileForm.getSort());
            }
        }

        int startIndex = Math.min(recipientFields.size(), (profileForm.getPage() - 1) * profileForm.getNumberOfRows());
        int endIndex = Math.min(recipientFields.size(), startIndex + profileForm.getNumberOfRows());
        List<RecipientFieldDescription> partialFieldsList = recipientFields.subList(startIndex, endIndex);

        PaginatedListImpl<RecipientFieldDescription> paginatedFieldsList = new PaginatedListImpl<>(partialFieldsList, recipientFields.size(), profileForm.getNumberOfRows(), profileForm.getPage(), profileForm.getSort(), profileForm.getDir());
        if (profileForm.isUiFiltersSet()) {
            paginatedFieldsList.setNotFilteredFullListSize(recipientFieldService.getCountForOverview(companyId));
        }

        model.addAttribute("profileFields", paginatedFieldsList);
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
        }

        if (!validationService.mayAddNewColumn(companyId)) {
            popups.alert("error.profiledb.maxCount");
        }

        int maxFields;
        int systemMaxFields = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfProfileFields, companyId);
        int companyMaxFields = configService.getIntegerValue(ConfigValue.MaxFields, companyId);
        if (companyMaxFields >= 0 && (companyMaxFields < systemMaxFields || systemMaxFields < 0)) {
            maxFields = companyMaxFields;
        } else {
            maxFields = systemMaxFields;
        }

        List<RecipientFieldDescription> companySpecificFields = recipientFields.stream().filter(x -> !RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(x.getColumnName())).collect(Collectors.toList());
        int currentFieldCount = companySpecificFields.size();

        boolean isNearLimit = maxFields - 5 <= currentFieldCount && currentFieldCount < maxFields;
        if (isNearLimit) {
            popups.warning("error.profiledb.nearLimit", currentFieldCount, maxFields);
        }

        int gracefulExtension = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfProfileFields_Graceful, companyId);
        boolean isWithinGracefulLimit = maxFields < currentFieldCount && currentFieldCount < maxFields + gracefulExtension;
        if (isWithinGracefulLimit) {
            popups.warning("error.numberOfProfileFieldsExceeded.graceful", maxFields, currentFieldCount, gracefulExtension);
        }

        return "settings_profile_field_list";
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute ProfileFieldForm form, @ModelAttribute ProfileFieldFormSearchParams searchParams) {
        FormUtils.syncSearchParams(searchParams, form, false);
        return "redirect:/profiledb/profiledb.action?restoreSort=true";
    }

    @RequestMapping("/{fieldname}/view.action")
    public String view(Admin admin, @PathVariable("fieldname") String fieldName, ModelMap model) {
        final int companyId = admin.getCompanyID();
        List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);

        RecipientFieldDescription field = recipientFieldService.getRecipientField(companyId, fieldName);

        if (!model.containsAttribute("profileForm")) {
            if (field == null) {
                return "redirect:/profiledb/new.action";
            }

            ProfileFieldForm form = new ProfileFieldForm();

            form.setFieldname(field.getColumnName());
            form.setShortname(field.getShortName());
            form.setDescription(field.getDescription());
            form.setFieldType(field.getSimpleDataType().getMessageKey().replace("settings.fieldType.", ""));
            form.setFieldLength(field.getCharacterLength());
            form.setFieldDefault(field.getDefaultValue());
            form.setFieldNull(field.isNullable());
            form.setFieldSort(field.getSortOrder());
            form.setLine(field.getLine() > 0);
            form.setInterest(field.getInterest() > 0);
            form.setFieldVisible(field.getDefaultPermission() != ProfileFieldMode.NotVisible);
            form.setFieldMode(field.getDefaultPermission());
            form.setIncludeInHistory(field.isHistorized());
            form.setAllowedValues(field.getAllowedValues() == null ? null : field.getAllowedValues().toArray(new String[0]));
            form.setUseAllowedValues(form.getAllowedValues() != null && form.getAllowedValues().length > 0);

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
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("userDefinedHistoryFieldsLimit", configService.getMaximumNumberOfUserDefinedHistoryProfileFields(companyId));
        }
        model.addAttribute("creationDate", creationDate);
        model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).toList());
        model.addAttribute("changeDate", changeDate);
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("dependents", findDependents(fieldName, companyId));
        }

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

    @RequestMapping("/{fieldname}/dependents.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String dependents(Admin admin, @PathVariable("fieldname") String fieldName, Model model, Popups popups) {
        int companyId = admin.getCompanyID();

        RecipientFieldDescription field = recipientFieldService.getRecipientField(companyId, fieldName);
        if (field == null) {
            popups.alert(ERROR_MSG);
            return "redirect:/profiledb/profiledb.action";
        }

        model.addAttribute("fieldname", fieldName);
        model.addAttribute("dependents", findDependents(fieldName, companyId));

        return "settings_profile_field_dependents_list";
    }

    private List<Dependent<ProfileFieldDependentType>> findDependents(String fieldName, int companyId) {
        List<Dependent<ProfileFieldDependentType>> dependents = new ArrayList<>();

        for (Workflow workflow : workflowService.getActiveWorkflowsDependentOnProfileField(fieldName, companyId)) {
            dependents.add(ProfileFieldDependentType.WORKFLOW.forId(workflow.getWorkflowId(), workflow.getShortname()));
        }

        for (TargetLight target : targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId)) {
            dependents.add(ProfileFieldDependentType.TARGET_GROUP.forId(target.getId(), target.getTargetName()));
        }

        for (ImportProfile importProfile : validationService.getImportsContainingProfileField(companyId, fieldName)) {
            dependents.add(ProfileFieldDependentType.IMPORT_PROFILE.forId(importProfile.getId(), importProfile.getName()));
        }

        for (ExportPredef exportProfile : validationService.getExportsContainingProfileField(companyId, fieldName)) {
            dependents.add(ProfileFieldDependentType.EXPORT_PROFILE.forId(exportProfile.getId(), exportProfile.getShortname()));
        }

        for (EmmAction emmAction : validationService.getDependentActions(fieldName, companyId)) {
            dependents.add(ProfileFieldDependentType.TRIGGER.forId(emmAction.getId(), emmAction.getShortname()));
        }

        return dependents;
    }

    @PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, @RequestParam(required = false) String targetUrl, Popups popups, HttpSession session) {
        if (StringUtils.isBlank(profileForm.getFieldname())) {
            popups.alert("error.profiledb.invalid_fieldname", "''");
        } else if (recipientFieldService.isReservedKeyWord(profileForm.getFieldname())) {
            popups.alert("error.name.reserved", profileForm.getFieldname());
        } else {
            RecipientFieldDescription field = recipientFieldService.getRecipientField(admin.getCompanyID(), profileForm.getFieldname());

            boolean isNewField = Objects.isNull(field);

            if (existAllowedValues(profileForm)) {
                clearAllowedValues(profileForm);
            }

            validateField(admin, profileForm, field, popups, session);

            if (!popups.hasAlertPopups()) {
                if (isNewField ? createField(admin, profileForm, session) : updateField(admin, field, profileForm)) {
                    popups.success(CHANGES_SAVED_MSG);
                    return StringUtils.isBlank(targetUrl)
                            ? "redirect:/profiledb/profiledb.action?restoreSort=true"
                            : "redirect:" + targetUrl;
                } else {
                    popups.alert(ERROR_MSG);
                }
            }
        }

        return MESSAGES_VIEW;
    }

    @RequestMapping("/{column}/confirmDelete.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String confirmDelete(Admin admin, @PathVariable String column, final Popups popups) {
        SimpleServiceResult validationResult = validationService.isValidToDelete(column, admin);
        popups.addPopups(validationResult);

        if (!validationResult.isSuccess()) {
        	return MESSAGES_VIEW;
        }
        return "settings_profile_field_delete_ajax";
    }

    @RequestMapping(value = "/{fieldname}/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @PathVariable("fieldname") String fieldName, Popups popups) {
    	try {
            SimpleServiceResult validationResult = validationService.isValidToDelete(fieldName, admin);
            popups.addPopups(validationResult);

            if (validationResult.isSuccess()) {
                recipientFieldService.deleteRecipientField(admin.getCompanyID(), fieldName);
	            popups.success(SELECTION_DELETED_MSG);
	            writeUserActivityLog(admin, new UserAction("delete profile field", String.format("Profile field %s removed", fieldName)));
	        }
		} catch (Exception e) {
			logger.error("Cannot delete profile field: {}", fieldName, e);
			popups.alert(ERROR_MSG);
		}

        return "redirect:/profiledb/profiledb.action?restoreSort=true";
    }

    @GetMapping(value = "/delete.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@RequestParam(required = false) Set<String> columns, Admin admin, Model model, Popups popups) {
        validateSelectedColumns(columns);

        Map<String, SimpleServiceResult> validationResults = getDeleteValidationResults(columns, admin);
        ServiceResult<List<String>> result = recipientFieldService.filterAllowedForDelete(validationResults, admin);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult(),
                "settings.profile.ProfileDelete", "settings.profile.field.delete.question",
                "bulkAction.delete.profile", "bulkAction.delete.profile.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@RequestParam(required = false) Set<String> columns, Admin admin, Popups popups) {
        validateSelectedColumns(columns);

        Map<String, SimpleServiceResult> validationResults = getDeleteValidationResults(columns, admin);
        ServiceResult<UserAction> result = recipientFieldService.delete(validationResults, admin);

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return "redirect:/profiledb/profiledb.action?restoreSort=true";
    }

    private void validateSelectedColumns(Set<String> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    private Map<String, SimpleServiceResult> getDeleteValidationResults(Set<String> columns, Admin admin) {
        return columns.stream()
                .collect(Collectors.toMap(Function.identity(), c -> validationService.isValidToDelete(c, admin)));
    }

    @RequestMapping("/new.action")
    public String newField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model,
                           @RequestParam(name = "postalField", required = false) String postalField, Popups popups,
                           HttpSession session) {
        handlePostalField(postalField, admin, model, session);

        final int companyId = admin.getCompanyID();
        List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);

        profileForm.setFieldType(DbColumnType.GENERIC_TYPE_VARCHAR);
        profileForm.setFieldLength(100);

        model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
        model.addAttribute("isNewField", true);
        model.addAttribute("HISTORY_FEATURE_ENABLED", configService.isRecipientProfileHistoryEnabled(admin.getCompanyID()));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("userDefinedHistoryFieldsLimit", configService.getMaximumNumberOfUserDefinedHistoryProfileFields(companyId));
        }

        return "settings_profile_field_view";
    }

    protected void handlePostalField(String field, Admin admin, Model model, HttpSession session) {
        // nothing to do
    }

    protected boolean createField(Admin admin, ProfileFieldForm form, HttpSession session) {
    	RecipientFieldDescription field = new RecipientFieldDescription();

        field.setColumnName(form.getFieldname());
        field.setDatabaseDataType(form.getFieldType());
        field.setCharacterLength(form.getFieldLength());
        field.setDescription(form.getDescription());
        field.setShortName(form.getShortname());
        field.setDefaultValue(form.getFieldDefault());
        field.setSortOrder(form.getFieldSort());
        if (admin.isRedesignedUiUsed()) {
            field.setDefaultPermission(form.getFieldMode());
        } else {
            field.setDefaultPermission(form.isFieldVisible() ? ProfileFieldMode.Editable : ProfileFieldMode.NotVisible);
        }
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest() ? 1 : 0);
        field.setHistorized(form.isIncludeInHistory());
        field.setNullable(form.isFieldNull());
        
        if (("NUMBER".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"FLOAT".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"DOUBLE".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"INTEGER".equalsIgnoreCase(field.getDatabaseDataType())) &&
				StringUtils.isNotBlank(field.getDefaultValue())) {
			field.setDefaultValue(AgnUtils.normalizeNumber(admin.getLocale(), field.getDefaultValue()));
		}

        if (form.isUseAllowedValues()) {
            field.setAllowedValues(Arrays.asList(form.getAllowedValues()));
        }

        writeUserActivityLog(admin, new UserAction("create profile field", "Profile field " + form.getShortname() + " created"));

        try {
			recipientFieldService.saveRecipientField(admin.getCompanyID(), field);
			return true;
		} catch (RequestErrorException e) {
            throw e;
		} catch (Exception e) {
			logger.error("Cannot create field: {}", form.getFieldname(), e);
			return false;
		}
    }

    private boolean updateField(Admin admin, RecipientFieldDescription field, ProfileFieldForm form) {
        UserAction openEmmChangeLog = getOpenEmmChangeLog(field, form);
        UserAction emmChangeLog = getEmmChangeLog(field, form, admin);

        if (Objects.nonNull(openEmmChangeLog)) {
            writeUserActivityLog(admin, openEmmChangeLog);
        }

        if (Objects.nonNull(emmChangeLog)) {
            writeUserActivityLog(admin, emmChangeLog);
        }

        field.setDatabaseDataType(form.getFieldType());
        field.setCharacterLength(form.getFieldLength());
        field.setDescription(form.getDescription());
        field.setShortName(form.getShortname());
        field.setSortOrder(form.getFieldSort());
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest() ? 1 : 0);
        field.setHistorized(form.isIncludeInHistory());
        field.setAllowedValues(form.getAllowedValues() == null ? null : Arrays.asList(form.getAllowedValues()));

        if (admin.permissionAllowed(Permission.PROFILEFIELD_VISIBLE)) {
            if (admin.isRedesignedUiUsed()) {
                field.setDefaultPermission(form.getFieldMode());
            } else {
                field.setDefaultPermission(form.isFieldVisible() ? ProfileFieldMode.Editable : ProfileFieldMode.NotVisible);
            }
        }
        
        if (("NUMBER".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"FLOAT".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"DOUBLE".equalsIgnoreCase(field.getDatabaseDataType()) ||
				"INTEGER".equalsIgnoreCase(field.getDatabaseDataType())) &&
				StringUtils.isNotBlank(field.getDefaultValue())) {
			field.setDefaultValue(AgnUtils.normalizeNumber(admin.getLocale(), field.getDefaultValue()));
		}
        
        try {
	        recipientFieldService.saveRecipientField(admin.getCompanyID(), field);
	        return true;
        } catch (RequestErrorException e) {
            throw e;
        } catch (Exception e) {
        	logger.error("Cannot update field", e);
			return false;
		}
    }

    private void validateField(Admin admin, ProfileFieldForm form, RecipientFieldDescription profileField, Popups popups, HttpSession session) {
        final int companyId = admin.getCompanyID();

        if (validationService.isDbFieldNameContainsSpaces(form.getFieldname())) {
            popups.fieldError("fieldname", "error.profilefield.spaces", form.getFieldname());
        } else if (!validationService.isValidDbFieldName(form.getFieldname())) {
            popups.fieldError("fieldname", "error.profilefield.name.invalid", form.getFieldname());
        }

        if (StringUtils.length(StringUtils.trim(form.getShortname())) < 3) {
            popups.fieldError("shortname", "error.profilefield.shortname.short");
        } else if (!validationService.isValidShortname(companyId, form.getShortname(), form.getFieldname())) {
            popups.fieldError("shortname", "error.profiledb.invalidShortname");
        }

        if (StringUtils.length(StringUtils.trim(form.getDescription())) < 3) {
            popups.fieldError("description", "error.profilefield.description.short");
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
            popups.fieldError("allowedValues", "error.profiledb.missingFixedValues");
        }
        if (popups.hasAlertPopups()) {
            return;
        }

        if (profileField == null) {
            validateForCreating(companyId, form, popups, admin.getDateFormat(), admin.getLocale(), session);
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

    private void validateForCreating(int companyId, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat, Locale locale, HttpSession session) {
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
        } else if (!mayAddNewColumn(companyId, session)) {
            popups.alert("error.profiledb.maxCount");
        }
    }

    protected boolean mayAddNewColumn(int companyId, HttpSession session) {
        return validationService.mayAddNewColumn(companyId);
    }

    private void validationForUpdating(int companyId, RecipientFieldDescription field, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat, Locale locale) {
        if (!field.isNullable() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (!validationService.isAllowedDefaultValue(field.getDatabaseDataType(), getDefaultValueToValidate(form, locale), dateFormat)) {
            popups.alert(INVALID_DEFAULT_VALUE_ERROR_MSG, dateFormat.toPattern());
        } else if (containNotAllowedValue(form, field.getDatabaseDataType(), dateFormat)) {
            popups.alert("error.profiledb.invalidFixedValue");
        } else if (form.isIncludeInHistory()) {
            validateHistorizedFieldsCount(companyId, form, popups);
        }
        alertIfUsedProfileFieldChanged(popups, companyId, field, form, locale);
    }

    private void validateHistorizedFieldsCount(int companyId, ProfileFieldForm form, Popups popups) {
        int maximumHistoryFields = configService.getMaximumNumberOfUserDefinedHistoryProfileFields(companyId);
        if (maximumHistoryFields < 0) {
            return; // unlimited
        }
        int count = recipientFieldService.getHistorizedCustomFields(companyId).size();
        String fieldNameLc = form.getFieldname().toLowerCase();
        if (!RecipientStandardField.getHistorizedRecipientStandardFieldColumnNames().contains(fieldNameLc)) {
            count++;
        }
        if (count > maximumHistoryFields) {
            popups.alert("error.profileHistory.tooManyFields");
        }
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

    private void alertIfUsedProfileFieldChanged(Popups popups, int companyId, RecipientFieldDescription field, ProfileFieldForm form, Locale locale) {
        if (!StringUtils.equals(field.getShortName(), form.getShortname())) {
            List<ObjectUsage> usages = objectUsageService.listUsageOfProfileFieldByVisibleName(companyId, field.getShortName());
            if (!usages.isEmpty()) {
                ObjectUsagesToPopups.objectUsagesToPopups("error.profileField.rename",
                        "error.profileField.rename.withMore", new ObjectUsages(usages), popups, locale);
            }
        }
        if (!form.isIncludeInHistory() && configService.isRecipientProfileHistoryEnabled(companyId)) {
            List<Workflow> workflows = workflowService.getActiveWorkflowsTrackingProfileField(field.getColumnName(), companyId);
            if (!workflows.isEmpty()) {
                popups.alert("warning.profilefield.inuse", workflows.get(0).getShortname());
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

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description, logger);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return ("allowedValues".equals(param) || "fieldDefault".equals(param))
        	&& configService.allowHtmlInReferenceAndProfileFields(admin.getCompanyID());
    }

    public UserAction getOpenEmmChangeLog(RecipientFieldDescription field, ProfileFieldForm form) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingDescription = field.getDescription() == null ? "" : field.getDescription().trim();
        String newDescription = form.getDescription().trim();
        String existingDefaultValue = field.getDefaultValue() == null ? "" : field.getDefaultValue().trim();
        String newDefaultValue = form.getFieldDefault().trim();
        String existingShortName = field.getShortName();
        String newShortName = form.getShortname();

        //log field name changes
        if (!existingShortName.equals(newShortName)) {
            changes.add(String.format("name changed from %s to %s", existingShortName, newShortName));
        }

        //log description name changes
        if (!existingDescription.equals(newDescription)) {
            if (existingDescription.length() <= 0 && newDescription.length() > 0) {
                changes.add("description added");
            }
            if (existingDescription.length() > 0 && newDescription.length() <= 0) {
                changes.add("description removed");
            }
            if (existingDescription.length() > 0 && newDescription.length() > 0) {
                changes.add("description changed");
            }
        }

        //log default value changes
        if (!existingDefaultValue.equals(newDefaultValue)) {
            if (existingDefaultValue.length() <= 0 && newDefaultValue.length() > 0) {
                changes.add(String.format("default value %s added", newDefaultValue));
            }
            if (existingDefaultValue.length() > 0 && newDefaultValue.length() <= 0) {
                changes.add(String.format("default value %s removed", existingDefaultValue));
            }
            if (existingDefaultValue.length() > 0 && newDefaultValue.length() > 0) {
                changes.add(String.format("default value changed from %s to %s", existingDefaultValue, newDefaultValue));
            }
        }

        List<String> deletedFixedValues = detectDeletedFixedValues(field, form);
        if (!deletedFixedValues.isEmpty()) {
            changes.add("fixed values deleted: [" + String.join(", ", deletedFixedValues) + "]");
        }

        List<String> addedFixedValues = detectNewAddedFixedValues(field, form);
        if (!addedFixedValues.isEmpty()) {
            changes.add("fixed values added: [" + String.join(", ", addedFixedValues) + "]");
        }

        if (!changes.isEmpty()) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s. Profile field:%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    private List<String> detectNewAddedFixedValues(RecipientFieldDescription field, ProfileFieldForm form) {
        if (form.getAllowedValues() == null) {
            return Collections.emptyList();
        }

        if (field.getAllowedValues() == null) {
            return List.of(form.getAllowedValues());
        }

        List<String> addedValues = new ArrayList<>();

        for (String newValue : form.getAllowedValues()) {
            if (!field.getAllowedValues().contains(newValue)) {
                addedValues.add(newValue);
            }
        }

        return addedValues;
    }

    private List<String> detectDeletedFixedValues(RecipientFieldDescription field, ProfileFieldForm form) {
        if (field.getAllowedValues() == null) {
            return Collections.emptyList();
        }

        if (form.getAllowedValues() == null) {
            return field.getAllowedValues();
        }

        List<String> deletedValues = new ArrayList<>();
        List<String> formValues = List.of(form.getAllowedValues());

        for (String fieldValue : field.getAllowedValues()) {
            if (!formValues.contains(fieldValue)) {
                deletedValues.add(fieldValue);
            }
        }

        return deletedValues;
    }

    public UserAction getEmmChangeLog(RecipientFieldDescription field, ProfileFieldForm form, Admin admin) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingShortName = field.getShortName();

        if (admin.isRedesignedUiUsed()) {
            if (!field.getDefaultPermission().equals(form.getFieldMode())) {
                changes.add(String.format(
                        "Visibility changed from '%s' to '%s'",
                        getTranslatedMode(field.getDefaultPermission()),
                        getTranslatedMode(form.getFieldMode())
                ));
            }
        } else {
            //log if "Field visible" checkbox changed
            if ((field.getDefaultPermission() == ProfileFieldMode.NotVisible) && (form.isFieldVisible())) {
                changes.add("Field visible checked");
            }
            if ((field.getDefaultPermission() == ProfileFieldMode.Editable) && (!form.isFieldVisible())) {
                changes.add("Field visible unchecked");
            }
        }
        //log if "Line after this field" checkbox changed
        if ((field.getLine() == 0) && (form.getLine())) {
            changes.add("Line after this field checked");
        }
        if ((field.getLine() == 2) && (!form.getLine())) {
            changes.add("Line after this field unchecked");
        }
        //log if "Value defines grade of interest" checkbox changed
        if ((field.getInterest() == 0) && (form.isInterest())) {
            changes.add("Value defines grade of interest checked");
        }
        if ((field.getInterest() == 2) && (!form.isInterest())) {
            changes.add("Value defines grade of interest unchecked");
        }
        //log if "Sort" changed
        if (field.getSortOrder() != form.getFieldSort()) {
            changes.add("Sort changed");
        }
        if (!field.isHistorized() && form.isIncludeInHistory()) {
            changes.add("'Include field in history' is activated");
        }

        if (!changes.isEmpty()) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s.%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    @GetMapping("/statistic.action")
    public String stat(ProfileFieldStatForm form, Model model, Admin admin, HttpSession session) {
        if (admin.isRedesignedUiUsed()) {
            addAdminPropsToStatForm(form, admin);
        }
        List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(admin.getCompanyID());

        if (!admin.isRedesignedUiUsed()) {
            if (StringUtils.isNotBlank(form.getColName()) && isInvalidColumn(recipientFields, form.getColName())) { // guard
                throw new IllegalArgumentException("Invalid column name: " + form.getColName());
            }
        }

        model
            .addAttribute("profileFields", recipientFields)
            .addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin))
            .addAttribute("targets", targetService.getTargetLights(admin));

        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("stat", evaluationDataSet.collect(form));
        } else {
            model.addAttribute("statUrl", birtStatisticsService.getProfileFieldEvalStatUrl(admin, session.getId(), form));
        }

        writeUserActivityLog(admin, "profile field statistics", "active submenu - evaluate profile fields");
        return "profile_field_evaluation_stat";
    }

    @GetMapping("/statisticCsv.action")
    public ResponseEntity<InputStreamResource> statCsv(ProfileFieldStatForm form, Admin admin) throws Exception {
        addAdminPropsToStatForm(form, admin);
        writeUserActivityLog(admin, "profile field statistics", "csv export");
        return MvcUtils.csvFileResponse(evaluationDataSet.csv(form), "profiledb_evaluation.csv");
    }

    private boolean isInvalidColumn(List<RecipientFieldDescription> fields, String colName) {
        return fields.stream().noneMatch(field -> colName.equals(field.getColumnName()));
    }

    protected void addAdminPropsToStatForm(ProfileFieldStatForm form, Admin admin) {
        form.setCompanyId(admin.getCompanyID());
        form.setLocale(admin.getLocale());
    }

    private static String getTranslatedMode(ProfileFieldMode mode) {
        return I18nString.getLocaleString(mode.getMessageKey(), Locale.UK);
    }
}
