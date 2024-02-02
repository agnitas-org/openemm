/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormForm;
import com.agnitas.emm.core.userform.form.UserFormsForm;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.scriptvalidator.IllegalVelocityDirectiveException;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import org.agnitas.emm.core.velocity.scriptvalidator.VelocityDirectiveScriptValidator;
import org.agnitas.service.FormImportResult;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.UserFormImporter;
import com.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.UserActivityUtil;
import org.agnitas.web.forms.BulkActionForm;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.SimpleActionForm;
import org.agnitas.web.forms.WorkflowParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userform")
public class UserFormController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(UserFormController.class);

	private final WebStorage webStorage;
	private final ComUserformService userformService;
	private final ComEmmActionService emmActionService;
	private final ConfigService configService;
	private final UserActivityLogService userActivityLogService;
	private final ExtendedConversionService conversionService;
    private final LinkService linkService;
	private final VelocityDirectiveScriptValidator velocityValidator;
	private final UserFormImporter userFormImporter;
	private final CompanyTokenService companyTokenService;

	public UserFormController(WebStorage webStorage, ComUserformService userformService, ComEmmActionService emmActionService,
							  ConfigService configService, UserActivityLogService userActivityLogService,
							  ExtendedConversionService conversionService, LinkService linkService,
							  VelocityDirectiveScriptValidator velocityValidator, UserFormImporter userFormImporter,
							  CompanyTokenService companyTokenService) {
		this.webStorage = webStorage;
		this.userformService = userformService;
		this.emmActionService = emmActionService;
		this.configService = configService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.linkService = linkService;
		this.velocityValidator = velocityValidator;
		this.userFormImporter = userFormImporter;
		this.companyTokenService = Objects.requireNonNull(companyTokenService, "CompanyTokenService is null");
	}

	@RequestMapping("/list.action")
	public String list(Admin admin, @ModelAttribute("form") UserFormsForm form, Model model, Popups popups) {
		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
			FormUtils.syncNumberOfRows(webStorage, WebStorage.USERFORM_OVERVIEW, form);

			model.addAttribute("webformListJson", userformService.getUserFormsJson(admin));
			model.addAttribute("companyToken", companyTokenForAdmin(admin).orElse(null));
		} catch (Exception e) {
			logger.error("Getting user form list failed!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "userform_list";
	}

	private void loadUserFormUrlPatterns(Admin admin, final String formName, Model model, final Optional<String> companyToken) {
		model.addAttribute("userFormURLPattern", userformService.getUserFormUrlPattern(admin, formName, false, companyToken));
		model.addAttribute("userFormFullURLPattern", userformService.getUserFormUrlPattern(admin, formName, true, companyToken));
	}

	private void loadUserformUrlPatternsAllTestRecipients(Admin admin, final String formName, Model model, final Optional<String> companyToken) {
		model.addAttribute("userFormURLPattern", userformService.getUserFormUrlPattern(admin, formName, false, companyToken));
		model.addAttribute("userFormFullURLPatterns", userformService.getUserFormUrlForAllAdminAndTestRecipients(admin, formName, companyToken));
		model.addAttribute("userFormFullURLPatternNoUid", userformService.getUserFormUrlWithoutUID(admin, formName, companyToken));
	}

	@PostMapping("/saveActiveness.action")
	public @ResponseBody BooleanResponseDto saveActiveness(Admin admin, @ModelAttribute("form") UserFormsForm form, Popups popups) {
		UserAction userAction = userformService.setActiveness(admin.getCompanyID(), form.getActiveness());
		boolean result = false;
		if (Objects.nonNull(userAction)) {
			writeUserActivityLog(admin, userAction);
			popups.success("default.changes_saved");
			result = true;
		} else {
			popups.alert(ERROR_MSG);
		}

		return new BooleanResponseDto(popups, result);
	}

	@GetMapping(value = {"/new.action", "/0/view.action"})
	public String create(Admin admin, @ModelAttribute("form") UserFormForm form, Model model, WorkflowParameters workflowParams) {
		int forwardedId = workflowParams.getTargetItemId();
        if(forwardedId > 0) {
            return "redirect:/webform/" + forwardedId + "/view.action";
        }

		final Optional<String> companyToken = companyTokenForAdmin(admin);

		loadUserFormUrlPatterns(admin, "", model, companyToken);
		List<EmmAction> emmActions = emmActionService.getEmmNotLinkActions(admin.getCompanyID(), false);
		model.addAttribute("emmActions", emmActions);

		loadFormBuilderData(admin, model);

		return "userform_view";
	}

	@GetMapping("/{id:\\d+}/view.action")
	public String view(Admin admin, @PathVariable int id,
			@ModelAttribute("form") UserFormForm form, Model model, Popups popups, WorkflowParameters workflowParams) {
		try {
			if (id == 0 && workflowParams.getTargetItemId() > 0) {
         	   id = workflowParams.getTargetItemId();
        	}
			final Optional<String> companyToken = companyTokenForAdmin(admin);

			UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), id);
			model.addAttribute("form", conversionService.convert(userForm, UserFormForm.class));

			loadUserformUrlPatternsAllTestRecipients(admin, userForm.getName(), model, companyToken);

			List<EmmAction> emmActions = emmActionService.getEmmNotLinkActions(admin.getCompanyID(), false);
			model.addAttribute("emmActions", emmActions);

			loadFormBuilderData(admin, model);

			writeUserActivityLog(admin, "view user form", String.format("%s (ID:%d)", userForm.getName(), userForm.getId()));
		} catch (Exception e) {
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}
		return "userform_view";
	}

	@PostMapping("/save.action")
	public String save(Admin admin, @ModelAttribute("form") UserFormForm form, Model model, Popups popups, WorkflowParameters workflowParams) {
		try {
			if (validate(admin, form, popups)) {
				boolean update = form.getFormId() > 0;
				UserFormDto userFormDto = conversionService.convert(form, UserFormDto.class);
				ServiceResult<Integer> result = userformService.saveUserForm(admin, userFormDto);

				int formId = result.getResult();

				if (result.isSuccess()) {
					popups.success("default.changes_saved");
				}

				if (result.hasErrorMessages()) {
					result.getErrorMessages().forEach(popups::alert);
				}
				if(CollectionUtils.isNotEmpty(result.getWarningMessages())) {
					result.getWarningMessages().forEach(popups::warning);
				}

				if (formId > 0) {
					writeUserActivityLog(admin, (update ? "edit user form" : "create user form"),
							String.format("%s (%d)", userFormDto.getName(), formId));

					return String.format("redirect:/webform/%d/view.action", formId);
				}
			}
		} catch (Exception e) {
			popups.alert(ERROR_MSG);
		}
		return MESSAGES_VIEW;
	}

	private List<Message> validateSettings(ResultSettings settings) throws ScriptValidationException {
        String type = settings.isSuccess() ? "SUCCESS" : "ERROR";
        List<Message> errors = new ArrayList<>();
        try {
			velocityValidator.validateScript(settings.getTemplate());
		} catch (IllegalVelocityDirectiveException e) {
            errors.add(new Message("error.form.illegal_directive", new Object[]{e.getDirective()}));
            return errors;
		}

        int invalidLineNumber = linkService.getLineNumberOfFirstInvalidLink(settings.getTemplate());
        if (invalidLineNumber != -1) {
            errors.add(new Message("error.invalid_link", new Object[]{type, invalidLineNumber}));
        }

        return errors;
    }

	private boolean validate(Admin admin, UserFormForm form, Popups popups) throws Exception {
		if (!userformService.isValidFormName(form.getFormName())) {
			popups.alert("error.form.invalid_name");
		} else if (!userformService.isFormNameUnique(form.getFormName(), form.getFormId(), admin.getCompanyID())) {
			popups.alert("error.form.name_in_use");
		}

		List<Message> errors = validateSettings(form.getSuccessSettings());
        errors.addAll(validateSettings(form.getErrorSettings()));

		return !popups.hasAlertPopups();
	}

	@GetMapping("/import.action")
	@PermissionMapping("import")
	public String importFormView(Admin admin, @RequestParam(value = "importTemplate", required = false) boolean templateOverview) {
		return templateOverview ? "import_view" : "userform_import";
	}

	@PostMapping("/importUserForm.action")
	@PermissionMapping("import")
	public String importForm(Admin admin, @RequestParam(value = "uploadFile") MultipartFile uploadFile, Popups popups) {
		if (uploadFile.isEmpty()) {
        	popups.alert("error.file.missingOrEmpty");
			return MESSAGES_VIEW;
		}

        FormImportResult result;
        try (InputStream input = uploadFile.getInputStream()) {
            // Import userform data from upload file
            result = userFormImporter.importUserForm(admin.getCompanyID(), input, admin.getLocale(), null);

            if (result.isSuccess()) {
            	popups.success("userform.imported");
				for (Map.Entry<String, Object[]> warningEntry : result.getWarnings().entrySet()) {
					popups.warning(warningEntry.getKey(), warningEntry.getValue());
				}

            	writeUserActivityLog(admin, "import userform", String.format("%s (%d)", result.getUserFormName(), result.getUserFormID()));
				return String.format("redirect:/webform/%d/view.action", result.getUserFormID());
			} else {
            	for (Map.Entry<String, Object[]> errorEntry : result.getErrors().entrySet()) {
					popups.alert(errorEntry.getKey(), errorEntry.getValue());
				}
			}
        } catch (Exception e) {
            logger.error("Mailing import failed", e);
            popups.alert("error.userform.import");
        }

		return MESSAGES_VIEW;
	}

	@GetMapping(value = "/{id:\\d+}/export.action")
	@PermissionMapping("export")
	public Object exportForm(Admin admin, @PathVariable int id, Popups popups) {
		int companyId = admin.getCompanyID();

		String userFormName = userformService.getUserFormName(id, companyId);
        File exportedFile = userformService.exportUserForm(admin, id, userFormName);

        if (exportedFile == null) {
            popups.alert("error.userform.export");
			return String.format("redirect:/webform/%d/view.action", id);
        }

        String downloadFileName = String.format("UserForm_%s_%d_%d.json",
				StringUtils.replace(userFormName, "/", "_"), companyId, id);

        writeUserActivityLog(admin, new UserAction("export user form", String.format("%s (%d)", userFormName, id)));
        return ResponseEntity.ok()
                .contentLength(exportedFile.length())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(downloadFileName))
                .body(new DeleteFileAfterSuccessReadResource(exportedFile));
	}

	@GetMapping("/{id:\\d+}/clone.action")
	public String clone(Admin admin, @PathVariable int id, Popups popups) {
		try {
			ServiceResult<Integer> result = userformService.cloneUserForm(admin, id);
			if (result.hasErrorMessages()) {
				result.getErrorMessages().forEach(popups::alert);
			}
			if(CollectionUtils.isNotEmpty(result.getWarningMessages())) {
				result.getWarningMessages().forEach(popups::warning);
			}

			int cloneId = result.getResult();
			if (cloneId > 0) {
				String formName = userformService.getUserFormName(id, admin.getCompanyID());
				writeUserActivityLog(admin, "cloned user form", String.format("%s (%d)", formName, id));

				return String.format("redirect:/webform/%d/view.action", cloneId);
			} else {
				logger.error("Result clone ID is wrong");
			}
		} catch (Exception e) {
			logger.error("Could not clone web form ID:{}", + id, e);
		}

		popups.alert(ERROR_MSG);
		return MESSAGES_VIEW;
	}

	@PostMapping("/confirmBulkDelete.action")
	public String confirmBulkDelete(@ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
		if (CollectionUtils.isEmpty(form.getBulkIds())) {
			popups.alert("bulkAction.nothing.userform");
			return MESSAGES_VIEW;
		}
		return "userform_bulk_delete_ajax";
	}

	@RequestMapping(value = "/bulkDelete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String bulkDelete(Admin admin, @ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
        if(form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.userform");
            return MESSAGES_VIEW;
        }

        List<UserFormDto> deletedUserForms = userformService.bulkDeleteUserForm(form.getBulkIds(), admin.getCompanyID());

        if (CollectionUtils.isNotEmpty(deletedUserForms)) {
			for (UserFormDto userForm : deletedUserForms) {
				writeUserActivityLog(admin, "delete user form",
						String.format("%s(%d)", userForm.getName(), userForm.getId()));
			}

			popups.success("default.selection.deleted");
			return "redirect:/webform/list.action";
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

	@GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(Admin admin, @PathVariable int id, @ModelAttribute("simpleActionForm") SimpleActionForm form, Popups popups) {
		UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), id);
		if (userForm != null) {
			form.setId(userForm.getId());
			form.setShortname(userForm.getName());

			return "userform_delete_ajax_new";
		}

		popups.alert(ERROR_MSG);
		return MESSAGES_VIEW;
	}

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, SimpleActionForm form, Popups popups) {
        if (userformService.deleteUserForm(form.getId(), admin.getCompanyID())) {
			writeUserActivityLog(admin, "delete user form", String.format("ID: %d", form.getId()));
			popups.success("default.selection.deleted");
			return "redirect:/webform/list.action";
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

    private Optional<String> companyTokenForAdmin(final Admin admin) {
    	try {
    		return this.companyTokenService.getCompanyToken(admin.getCompanyID());
    	} catch(final UnknownCompanyIdException e) {
    		return Optional.empty();
    	}
    }

	private void writeUserActivityLog(Admin admin, String action, String description) {
		writeUserActivityLog(admin, new UserAction(action, description));
	}

	private void writeUserActivityLog(Admin admin, UserAction userAction) {
		UserActivityUtil.log(userActivityLogService, admin, userAction, logger);
	}

	private void loadFormBuilderData(final Admin admin, final Model model) {
		model.addAttribute("companyToken", companyTokenForAdmin(admin).orElse(""));
		model.addAttribute("names", userformService.getUserFormNames(admin.getCompanyID()));
		model.addAttribute("mediapoolImages", userformService.getMediapoolImages(admin));
		model.addAttribute("formCssLocation", configService.getValue(ConfigValue.UserFormCssLocation));

		Map<String, String> textProfileFields = userformService.getProfileFields(admin, DbColumnType.SimpleDataType.Characters);
		Map<String, String> numericProfileFields = userformService.getProfileFields(admin, DbColumnType.SimpleDataType.Numeric, DbColumnType.SimpleDataType.Float);

		Map<String, String> profileFieldsForSelect = new HashMap<>(textProfileFields);
		profileFieldsForSelect.put(RecipientUtils.COLUMN_GENDER, numericProfileFields.get(RecipientUtils.COLUMN_GENDER));

		model.addAttribute("textProfileFields", textProfileFields);
		model.addAttribute("profileFieldsForSelect", profileFieldsForSelect);
		model.addAttribute("numberProfileFields", numericProfileFields);
		model.addAttribute("dateProfileFields", userformService.getProfileFields(admin, DbColumnType.SimpleDataType.Date, DbColumnType.SimpleDataType.DateTime));
	}

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return ("successSettings.template".equals(param) || "errorSettings.template".equals(param))
                && "save".equals(controllerMethodName);
    }
}
