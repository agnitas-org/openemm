/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormForm;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.emm.core.action.bean.EmmAction;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.FormImportResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.UserFormImporter;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.forms.ActivenessPaginationForm;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.SimpleActionForm;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userform")
public class UserFormController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(UserFormController.class);
	private static final String REDIRECT_TO_OVERVIEW = "redirect:/webform/list.action";

	private final UserformService userformService;
	private final EmmActionService emmActionService;
	private final ConfigService configService;
	private final UserActivityLogService userActivityLogService;
	private final ExtendedConversionService conversionService;
	private final UserFormImporter userFormImporter;
	private final CompanyTokenService companyTokenService;

	public UserFormController(UserformService userformService, EmmActionService emmActionService,
							  ConfigService configService, UserActivityLogService userActivityLogService,
							  ExtendedConversionService conversionService, UserFormImporter userFormImporter,
							  CompanyTokenService companyTokenService) {
		this.userformService = userformService;
		this.emmActionService = emmActionService;
		this.configService = configService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.userFormImporter = userFormImporter;
		this.companyTokenService = Objects.requireNonNull(companyTokenService, "CompanyTokenService is null");
	}

	@RequestMapping("/list.action")
	public String list(Admin admin, Model model, Popups popups) {
		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

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
	// TODO: EMMGUI-714: Remove after remove of old design
	public @ResponseBody BooleanResponseDto saveActiveness(Admin admin, @ModelAttribute("form") ActivenessPaginationForm form, Popups popups) {
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

	@PostMapping("/changeActiveness.action")
	public Object changeActiveness(@RequestParam(required = false) Set<Integer> ids, Admin admin, Popups popups,
								   @RequestParam boolean activate, @RequestParam(defaultValue = "false") boolean fromOverview) {
		validateSelectedIds(ids);

		ServiceResult<List<UserForm>> result = userformService.setActiveness(ids, admin.getCompanyID(), activate);
		popups.addPopups(result);

		Collection<Integer> affectedIds = CollectionUtils.emptyIfNull(result.getResult())
				.stream()
				.map(UserForm::getId)
				.toList();

		if (result.isSuccess()) {
			writeChangeActivenessToUAL(affectedIds, activate, admin);
			if (popups.isEmpty()) {
				popups.success(CHANGES_SAVED_MSG);
			}
		}

		return fromOverview
				? ResponseEntity.ok(new DataResponseDto<>(affectedIds, popups, result.isSuccess()))
				: redirectToView(ids.iterator().next());
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
			if (admin.isRedesignedUiUsed()) {
				model.addAttribute("isActive", userForm.isActive());
			}

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

	@PostMapping("/{id:\\d+}/activate.action")
	public @ResponseBody BooleanResponseDto activate(@PathVariable int id, Admin admin, Popups popups) {
		boolean result = userformService.updateActiveness(admin.getCompanyID(), List.of(id), true) > 0;
		if (!result) {
			popups.alert(ERROR_MSG);
		}

		return new BooleanResponseDto(popups, result);
	}

	@PostMapping("/save.action")
	public String save(Admin admin, @ModelAttribute("form") UserFormForm form, Popups popups) {
		try {
            List<Message> errors = userformService.validateUserForm(admin, form);
            if (CollectionUtils.isNotEmpty(errors)) {
                errors.forEach(popups::alert);
                return MESSAGES_VIEW;
            }
            
            boolean update = form.getFormId() > 0;
            UserFormDto userFormDto = conversionService.convert(form, UserFormDto.class);
			if (admin.isRedesignedUiUsed()) {
				userFormDto.setActive(userformService.isActive(form.getFormId()));
			}
            ServiceResult<Integer> result = userformService.saveUserForm(admin, userFormDto);

            int formId = result.getResult();

            if (result.isSuccess()) {
                popups.success(CHANGES_SAVED_MSG);
            }

            if (result.hasErrorMessages()) {
                result.getErrorMessages().forEach(popups::alert);
            }
            if (CollectionUtils.isNotEmpty(result.getWarningMessages())) {
                result.getWarningMessages().forEach(popups::warning);
            }

            if (formId > 0) {
                writeUserActivityLog(admin, (update ? "edit user form" : "create user form"),
                        getFormDescr(userFormDto != null ? userFormDto.getName() : "", formId));

                return redirectToView(formId);
            }
		} catch (Exception e) {
			popups.alert(ERROR_MSG);
		}
		return MESSAGES_VIEW;
	}

    private static String redirectToView(int formId) {
        return String.format("redirect:/webform/%d/view.action", formId);
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
	@GetMapping("/import.action")
	@PermissionMapping("import")
	public String importFormView(Admin admin, @RequestParam(value = "importTemplate", required = false) boolean templateOverview) {
		return templateOverview ? "import_view" : "userform_import";
	}

	@PostMapping("/importUserForm.action")
	@PermissionMapping("import")
	public String importForm(@RequestParam MultipartFile uploadFile, Admin admin, Popups popups, @RequestHeader(value = "referer", required = false) String referer) {
        FormImportResult result = userFormImporter.importUserForm(uploadFile, admin.getLocale(), admin.getCompanyID());
        if (!result.isSuccess()) {
            result.getErrors().forEach(popups::alert);
			return StringUtils.isNotBlank(referer)
					? "redirect:" + AgnUtils.removeJsessionIdFromUrl(referer)
					: REDIRECT_TO_OVERVIEW;
		}
        writeUserActivityLog(admin, "import userform", getFormDescr(result.getUserFormName(), result.getUserFormID()));
        result.getWarnings().forEach(popups::warning);
        popups.success("userform.imported");
        return redirectToView(result.getUserFormID());
	}

    private static String getFormDescr(String name, int id) {
        return String.format("%s (%d)", name, id);
    }

	@GetMapping(value = "/{id:\\d+}/export.action")
	@PermissionMapping("export")
	public Object exportForm(Admin admin, @PathVariable int id, Popups popups) {
		int companyId = admin.getCompanyID();

		String userFormName = userformService.getUserFormName(id, companyId);
        File exportedFile = userformService.exportUserForm(admin, id, userFormName);

        if (exportedFile == null) {
            popups.alert("error.userform.export");
			return redirectToView(id);
        }

        String downloadFileName = String.format("UserForm_%s_%d_%d.json",
				StringUtils.replace(userFormName, "/", "_"), companyId, id);

        writeUserActivityLog(admin, new UserAction("export user form", getFormDescr(userFormName, id)));
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
				writeUserActivityLog(admin, "cloned user form", getFormDescr(formName, id));

				return redirectToView(cloneId);
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
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public String confirmBulkDelete(@ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
		if (CollectionUtils.isEmpty(form.getBulkIds())) {
			popups.alert("bulkAction.nothing.userform");
			return MESSAGES_VIEW;
		}
		return "userform_bulk_delete_ajax";
	}

	@RequestMapping(value = "/bulkDelete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
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
			return REDIRECT_TO_OVERVIEW;
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

	@GetMapping("/{id:\\d+}/confirmDelete.action")
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
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
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String delete(Admin admin, SimpleActionForm form, Popups popups) {
        if (userformService.deleteUserForm(form.getId(), admin.getCompanyID())) {
			writeUserActivityLog(admin, "delete user form", String.format("ID: %d", form.getId()));
			popups.success("default.selection.deleted");
			return REDIRECT_TO_OVERVIEW;
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

	@GetMapping(value = "/deleteRedesigned.action")
	@PermissionMapping("confirmDelete")
	public String confirmDeleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model) {
		validateSelectedIds(bulkIds);
		List<String> items = userformService.getUserFormNames(bulkIds, admin.getCompanyID());
		MvcUtils.addDeleteAttrs(model, items,
                "settings.form.delete", "settings.userform.delete.question",
                "bulkAction.delete.userform", "bulkAction.delete.userform.question");
		return DELETE_VIEW;
	}

	@RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
	@PermissionMapping("delete")
	public String deleteRedesigned(@RequestParam(required = false) List<Integer> bulkIds, Admin admin, Popups popups) {
		validateSelectedIds(bulkIds);

		List<UserFormDto> deletedUserForms = userformService.bulkDeleteUserForm(bulkIds, admin.getCompanyID());

		if (CollectionUtils.isNotEmpty(deletedUserForms)) {
			deletedUserForms.forEach(f -> {
				writeUserActivityLog(admin, "delete user form", String.format("%s(%d)", f.getName(), f.getId()));
			});

			popups.success(SELECTION_DELETED_MSG);
			return REDIRECT_TO_OVERVIEW;
		}

		popups.alert(ERROR_MSG);
		return MESSAGES_VIEW;
	}

	@PostMapping("/restore.action")
	@ResponseBody
	public BooleanResponseDto restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
		validateSelectedIds(bulkIds);
		userformService.restore(bulkIds, admin.getCompanyID());
		popups.success(CHANGES_SAVED_MSG);
		return new BooleanResponseDto(popups, true);
	}

	private void validateSelectedIds(Collection<Integer> bulkIds) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			throw new RequestErrorException(NOTHING_SELECTED_MSG);
		}
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
		profileFieldsForSelect.put(RecipientStandardField.Gender.getColumnName(), numericProfileFields.get(RecipientStandardField.Gender.getColumnName()));

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

	private void writeChangeActivenessToUAL(Collection<Integer> ids, boolean activeness, Admin admin) {
		String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
        writeUserActivityLog(admin, (activeness ? "activate" : "deactivate") + " user forms", "IDs: " + idsStr);
	}
}
