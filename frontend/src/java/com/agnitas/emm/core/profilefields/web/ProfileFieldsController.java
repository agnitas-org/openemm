/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.web;

import static com.agnitas.emm.core.Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.emm.core.objectusage.common.ObjectUsage;
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
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToPopups;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryUtil;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/profiledb")
@PermissionMapping("profiledb")
public class ProfileFieldsController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(ProfileFieldsController.class);

    private static final String INVALID_DEFAULT_VALUE_ERROR_MSG = "error.profiledb.invalidDefaultValue";

    private final RecipientFieldService recipientFieldService;
    private final WebStorage webStorage;
    private final ConfigService configService;
    private final ProfileFieldValidationService validationService;
    private final UserActivityLogService userActivityLogService;
    private final ObjectUsageService objectUsageService;
    private final ComWorkflowService workflowService;
    private final ComTargetService targetService;

    public ProfileFieldsController(RecipientFieldService recipientFieldService, WebStorage webStorage, ConfigService configService,
                                   ProfileFieldValidationService validationService, UserActivityLogService userActivityLogService, ObjectUsageService objectUsageService,
                                   ComWorkflowService workflowService, ComTargetService targetService) {
        this.recipientFieldService = recipientFieldService;
        this.webStorage = webStorage;
        this.configService = configService;
        this.validationService = validationService;
        this.userActivityLogService = userActivityLogService;
        this.objectUsageService = Objects.requireNonNull(objectUsageService, "ObjectUsageService is null");
        this.workflowService = workflowService;
        this.targetService = targetService;
    }

    @RequestMapping("/profiledb.action")
    public String list(@ModelAttribute("profileForm") ProfileFieldForm profileForm, Admin admin, Model model, Popups popups,
                       @RequestParam(value = "syncSorting", required = false) boolean syncSorting) {
    	FormUtils.syncNumberOfRows(webStorage, ComWebStorage.PROFILE_FIELD_OVERVIEW, profileForm);

    	if (syncSorting) {
            FormUtils.syncSortingParams(webStorage, ComWebStorage.PROFILE_FIELD_OVERVIEW, profileForm);
        }

    	int companyId = admin.getCompanyID();

    	try {
    		List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);

	        Map<String, Comparator<RecipientFieldDescription>> sortableColumns = new HashMap<>();
	
	        sortableColumns.put("shortname", Comparator.comparing(pf -> pf.getShortName() == null ? "" : pf.getShortName().toLowerCase()));
	        sortableColumns.put("column", Comparator.comparing(pf -> pf.getColumnName().toLowerCase()));
	        sortableColumns.put("dataType", Comparator.comparing(pf -> pf.getDatabaseDataType().toLowerCase()));
	        sortableColumns.put("dataTypeLength", Comparator.comparing(RecipientFieldDescription::getCharacterLength));
	        sortableColumns.put("defaultValue", Comparator.comparing(pf -> pf.getDefaultValue() == null ? "" : pf.getDefaultValue().toLowerCase()));
	        sortableColumns.put("modeEdit", Comparator.comparing(pf -> pf.getDefaultPermission().getStorageCode()));
	        sortableColumns.put("sort", Comparator.comparing(RecipientFieldDescription::getSortOrder));
	
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
	    	
	        model.addAttribute("profileFields", paginatedFieldsList);
	        model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
	
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
			
			List<RecipientFieldDescription> companySpecificFields = recipientFields.stream().filter(x -> !RecipientFieldService.RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(x.getColumnName())).collect(Collectors.toList());
			int currentFieldCount = companySpecificFields.size();
			
			boolean isNearLimit = maxFields - 5 <= currentFieldCount && currentFieldCount < maxFields;
	        if (isNearLimit) {
	            popups.warning("error.profiledb.nearLimit", currentFieldCount, maxFields);
	        }
	
			boolean isWithinGracefulLimit = maxFields < currentFieldCount && currentFieldCount < maxFields + ConfigValue.System_License_MaximumNumberOfProfileFields.getGracefulExtension();
	        if (isWithinGracefulLimit) {
	            popups.warning("error.numberOfProfileFieldsExceeded.graceful", maxFields, currentFieldCount, ConfigValue.System_License_MaximumNumberOfProfileFields.getGracefulExtension());
	        }
    	} catch (Exception e) {
    		logger.error("Cannot read profile fields", e);
    		popups.alert(ERROR_MSG);
		}

        return "settings_profile_field_list";
    }

    @RequestMapping("/{fieldname}/view.action")
    public String view(Admin admin, @PathVariable("fieldname") String fieldName, ModelMap model, Popups popups) {
    	try {
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
	            form.setIncludeInHistory(field.isHistorized());
	            form.setAllowedValues(field.getAllowedValues() == null ? null : field.getAllowedValues().toArray(new String[0]));
	            form.setUseAllowedValues(form.getAllowedValues() != null);
	            
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
	        model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
	        model.addAttribute("changeDate", changeDate);
    	} catch (Exception e) {
    		logger.error("Cannot read profile field: {}", fieldName, e);
    		popups.alert(ERROR_MSG);
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
    public String dependents(Admin admin, @PathVariable("fieldname") String fieldName, Model model, Popups popups) {
    	try {
	        int companyId = admin.getCompanyID();
	
	        RecipientFieldDescription field = recipientFieldService.getRecipientField(companyId, fieldName);
	        if (field == null) {
	            popups.alert(ERROR_MSG);
	            return "redirect:/profiledb/profiledb.action";
	        }
	
	        model.addAttribute("fieldname", fieldName);
	        
	        List<Workflow> dependentWorkflows = workflowService.getActiveWorkflowsDependentOnProfileField(fieldName, companyId);
	        List<TargetLight> dependentTargets = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId);
	
	        List<Dependent<ProfileFieldDependentType>> dependents = new ArrayList<>(dependentWorkflows.size() + dependentTargets.size());
	
	        for (Workflow workflow : dependentWorkflows) {
	            dependents.add(ProfileFieldDependentType.WORKFLOW.forId(workflow.getWorkflowId(), workflow.getShortname()));
	        }
	
	        for (TargetLight target : dependentTargets) {
	            dependents.add(ProfileFieldDependentType.TARGET_GROUP.forId(target.getId(), target.getTargetName()));
	        }
	        
	        model.addAttribute("dependents", dependents);
    	} catch (Exception e) {
    		popups.alert("Cannot read dependents", e);
		}

        return "settings_profile_field_dependents_list";
    }

    @PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Popups popups) {
    	try {
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
	
	            validateField(admin, profileForm, field, popups);
	
	            if (!popups.hasAlertPopups()) {
	                if (isNewField ? createField(admin, profileForm) : updateField(admin, field, profileForm)) {
	                    popups.success(CHANGES_SAVED_MSG);
	                    return "redirect:/profiledb/profiledb.action?syncSorting=true";
	                } else {
	                    popups.alert(ERROR_MSG);
	                }
	            }
	        }
    	} catch (Exception e) {
    		popups.alert("Cannot save recipient field: " + profileForm.getFieldname(), e);
		}

        return MESSAGES_VIEW;
    }

    @RequestMapping("/saveWizardField.action")
    public String saveWizardField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, RedirectAttributes model, Popups popups) {
        String tile = save(admin, profileForm, popups);

        if (tile.equals(MESSAGES_VIEW)) {
            return MESSAGES_VIEW;
        }

        model.addFlashAttribute("profileForm", profileForm);

        return "redirect:/profiledb/newWizardField.action";
    }

    @RequestMapping("/{column}/confirmDelete.action")
    public String confirmDelete(Admin admin, @PathVariable String column, final Popups popups) {
        ObjectUsages usages = objectUsageService.listUsageOfProfileFieldByDatabaseName(admin.getCompanyID(), column);
        if (!usages.isEmpty()) {
        	ObjectUsagesToPopups.objectUsagesToPopups(
        	        "error.profilefield.used", "error.profilefield.used.withMore", usages, popups, admin.getLocale());
        	return MESSAGES_VIEW;
        }
        return "settings_profile_field_delete_ajax";
    }

    @RequestMapping(value = "/{fieldname}/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @PathVariable("fieldname") String fieldName, Popups popups) {
    	try {
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
	            recipientFieldService.deleteRecipientField(companyId, fieldName);
	
	            popups.success("default.selection.deleted");
	            writeUserActivityLog(admin, new UserAction("delete profile field", String.format("Profile field %s removed", fieldName)));
	        }
		} catch (Exception e) {
			logger.error("Cannot delete profile field: {}", fieldName, e);
			popups.alert(ERROR_MSG);
		}

        return "redirect:/profiledb/profiledb.action";
    }

    @RequestMapping("/new.action")
    public String newField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model, Popups popups) {
    	try {
	        final int companyId = admin.getCompanyID();
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);
			
	        profileForm.setFieldType(DbColumnType.GENERIC_TYPE_VARCHAR);
	        profileForm.setFieldLength(100);
	
	        model.addAttribute("fieldsWithIndividualSortOrder", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
	        model.addAttribute("isNewField", true);
	        model.addAttribute("HISTORY_FEATURE_ENABLED", configService.isRecipientProfileHistoryEnabled(admin.getCompanyID()));
		} catch (Exception e) {
			logger.error("Cannot create new profile field", e);
			popups.alert(ERROR_MSG);
		}
		
        return "settings_profile_field_view";
    }

    @RequestMapping("/newWizardField.action")
    public String newWizardField(Admin admin, @ModelAttribute("profileForm") ProfileFieldForm profileForm, Model model, Popups popups) {
    	try {
	        model.addAttribute("isNewField", isNewField(admin, profileForm.getFieldname()));
	        
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(admin.getCompanyID());    	
	        model.addAttribute("customerFields", recipientFields.stream().filter(x -> x.getSortOrder() > 0 && x.getSortOrder() < 1000).collect(Collectors.toList()));
    	} catch (Exception e) {
			logger.error("Cannot prepare new profile field", e);
			popups.alert(ERROR_MSG);
		}
    	
        return "mailing_wizard_new_field";
    }

    @RequestMapping("/backToTarget.action")
    public String backToTarget() {
        return "mailing_wizard_new_target";
    }

    private boolean isNewField(Admin admin, String fieldName) throws Exception {
        if (StringUtils.isBlank(fieldName)) {
            return true;
        }

        if (validationService.isValidDbFieldName(fieldName)) {
            return recipientFieldService.getRecipientField(admin.getCompanyID(), fieldName) == null;
        } else {
            return true;
        }
    }

    private boolean createField(Admin admin, ProfileFieldForm form) {
    	RecipientFieldDescription field = new RecipientFieldDescription();

        field.setColumnName(form.getFieldname());
        field.setDatabaseDataType(form.getFieldType());
        field.setCharacterLength(form.getFieldLength());
        field.setDescription(form.getDescription());
        field.setShortName(form.getShortname());
        field.setDefaultValue(form.getFieldDefault());
        field.setSortOrder(form.getFieldSort());
        field.setDefaultPermission(form.isFieldVisible() ? ProfileFieldMode.Editable : ProfileFieldMode.NotVisible);
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest() ? 1 : 0);
        field.setHistorized(form.isIncludeInHistory());
        field.setNullable(form.isFieldNull());

        if (form.isUseAllowedValues()) {
            field.setAllowedValues(Arrays.asList(form.getAllowedValues()));
        }

        writeUserActivityLog(admin, new UserAction("create profile field", "Profile field " + form.getShortname() + " created"));

        try {
			recipientFieldService.saveRecipientField(admin.getCompanyID(), field);
			return true;
		} catch (Exception e) {
			logger.error("Cannot create field: {}", form.getFieldname(), e);
			return false;
		}
    }

    private boolean updateField(Admin admin, RecipientFieldDescription field, ProfileFieldForm form) {
        UserAction openEmmChangeLog = getOpenEmmChangeLog(field, form);
        UserAction emmChangeLog = getEmmChangeLog(field, form);

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
            field.setDefaultPermission(form.isFieldVisible() ? ProfileFieldMode.Editable : ProfileFieldMode.NotVisible);
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
        } catch (Exception e) {
        	logger.error("Cannot update field", e);
			return false;
		}
    }

    private void validateField(Admin admin, ProfileFieldForm form, RecipientFieldDescription profileField, Popups popups) throws Exception {
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

    private void validationForUpdating(int companyId, RecipientFieldDescription field, ProfileFieldForm form, Popups popups, SimpleDateFormat dateFormat, Locale locale) throws Exception {
        if (!field.isNullable() && StringUtils.isEmpty(form.getFieldDefault())) {
            popups.alert("error.profiledb.empty");
        }

        if (!validationService.isAllowedDefaultValue(field.getDatabaseDataType(), getDefaultValueToValidate(form, locale), dateFormat)) {
            popups.alert(INVALID_DEFAULT_VALUE_ERROR_MSG, dateFormat.toPattern());
        } else if (containNotAllowedValue(form, field.getDatabaseDataType(), dateFormat)) {
            popups.alert("error.profiledb.invalidFixedValue");
        } else if (form.isIncludeInHistory()) {
            final int maximumHistoryFields = configService.getMaximumNumberOfUserDefinedHistoryProfileFields(companyId);
            final Set<String> columnSet = recipientFieldService.getRecipientFields(companyId).stream().filter(x -> x.isHistorized()).map(x -> x.getColumnName()).collect(Collectors.toSet());

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

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return ("allowedValues".equals(param) || "fieldDefault".equals(param))
                && admin.permissionAllowed(RECIPIENT_PROFILEFIELD_HTML_ALLOWED);
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

        if (changes.size() > 0) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s. Profile field:%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    public UserAction getEmmChangeLog(RecipientFieldDescription field, ProfileFieldForm form) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingShortName = field.getShortName();

        //log if "Field visible" checkbox changed
        if ((field.getDefaultPermission() == ProfileFieldMode.NotVisible) && (form.isFieldVisible())) {
            changes.add("Field visible checked");
        }
        if ((field.getDefaultPermission() == ProfileFieldMode.Editable) && (!form.isFieldVisible())) {
            changes.add("Field visible unchecked");
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

        if (changes.size() > 0) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s.%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }
}
