/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.actions.EmmAction;
import org.agnitas.actions.impl.EmmActionImpl;
import org.agnitas.beans.factory.ActionOperationFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.UserActivityUtil;
import org.agnitas.web.forms.BulkActionForm;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.form.EmmActionForm;
import com.agnitas.emm.core.action.form.EmmActionsForm;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.action.service.impl.EmmActionValidationServiceImpl;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

@Controller
@RequestMapping("/action")
@PermissionMapping("action")
public class EmmActionController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(EmmActionController.class);

    private static final String ACTIONS_VIEW = "actions_view";
    private static final String SHORTNAME = "shortname";
	
    private final WebStorage webStorage;
    private final ComEmmActionService emmActionService;
    private final MailingService mailingService;
    private final ConfigService configService;
    private final ComWorkflowService workflowService;
	private final UserActivityLogService userActivityLogService;
	private final ConversionService conversionService;
	private final ActionOperationParametersParser actionOperationParametersParser;
	private final EmmActionValidationServiceImpl validationService;
	private final ActionOperationFactory actionOperationFactory;
	private final ComUserformService userFormService;
	private final MailinglistApprovalService mailinglistApprovalService;
	private final ColumnInfoService columnInfoService;

    public EmmActionController(WebStorage webStorage, ComEmmActionService emmActionService, MailingService mailingService, ConfigService configService, ComWorkflowService workflowService, UserActivityLogService userActivityLogService, ConversionService conversionService, ActionOperationParametersParser actionOperationParametersParser, EmmActionValidationServiceImpl validationService, ActionOperationFactory actionOperationFactory, ComUserformService userFormService, MailinglistApprovalService mailinglistApprovalService, final ColumnInfoService columnInfoService) {
		this.webStorage = webStorage;
		this.emmActionService = emmActionService;
		this.mailingService = mailingService;
		this.configService = configService;
		this.workflowService = workflowService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.actionOperationParametersParser = actionOperationParametersParser;
		this.validationService = validationService;
		this.actionOperationFactory = actionOperationFactory;
		this.userFormService = userFormService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.columnInfoService = Objects.requireNonNull(columnInfoService, "columnInfoService");
    }

	@RequestMapping("/list.action")
	public String list(Admin admin, EmmActionsForm form, Model model, Popups popups) {
		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
			FormUtils.syncNumberOfRows(webStorage, WebStorage.ACTION_OVERVIEW, form);

			model.addAttribute("actionListJson", emmActionService.getEmmActionsJson(admin));
		} catch (Exception e) {
			logger.error("Getting emm action list failed!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "actions_list";
	}

	@PostMapping("/saveActiveness.action")
	public @ResponseBody BooleanResponseDto saveActiveness(Admin admin, EmmActionsForm form, Popups popups) {
		List<UserAction> userActions = new ArrayList<>();
		boolean result = emmActionService.setActiveness(form.getActiveness(), admin.getCompanyID(), userActions);
		if (result) {
			for (UserAction action : userActions) {
				writeUserActivityLog(admin, action);
			}
			popups.success(CHANGES_SAVED_MSG);
		} else {
			popups.alert(ERROR_MSG);
		}

		return new BooleanResponseDto(popups, result);
	}

	@PostMapping("/confirmBulkDelete.action")
	public String confirmBulkDelete(@ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
		if (CollectionUtils.isEmpty(form.getBulkIds())) {
			popups.alert("bulkAction.nothing.action");
			return MESSAGES_VIEW;
		}
		return "action_bulk_delete_ajax";
	}

	@RequestMapping(value = "/bulkDelete.action", method = { RequestMethod.POST, RequestMethod.DELETE})
    public String bulkDelete(Admin admin, @ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.action");
            return MESSAGES_VIEW;
        }

		try {
			Map<Integer, String> descriptions = new HashMap<>();
			for (int actionId : form.getBulkIds()) {
				descriptions.put(actionId, emmActionService.getEmmActionName(actionId, admin.getCompanyID()));
			}

			emmActionService.bulkDelete(new HashSet<>(form.getBulkIds()), admin.getCompanyID());

			for (int actionId : form.getBulkIds()) {
				writeUserActivityLog(admin, "delete action", descriptions.get(actionId));
			}

			popups.success(SELECTION_DELETED_MSG);
			return "redirect:/action/list.action";
		} catch (Exception e) {
			logger.error("Bulk deletion failed: {}", e.getMessage(), e);
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

    @GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(Admin admin, @PathVariable int id, @ModelAttribute("simpleActionForm") SimpleActionForm form, Popups popups) {
		EmmAction emmAction = emmActionService.getEmmAction(id, admin.getCompanyID());
		if (emmAction != null) {
			form.setId(emmAction.getId());
			form.setShortname(emmAction.getShortname());

			return "action_delete_ajax";
		}

		popups.alert(ERROR_MSG);
		return MESSAGES_VIEW;
	}

	@RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, SimpleActionForm form, Popups popups) {
        if (emmActionService.deleteEmmAction(form.getId(), admin.getCompanyID())) {
			writeUserActivityLog(admin, "delete action", String.format("%s (ID: %d)", form.getShortname(), form.getId()));
			popups.success(SELECTION_DELETED_MSG);
			return "redirect:/action/list.action";
		}

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

	@GetMapping(value = {"/new.action", "/0/view.action"})
	public String create(Admin admin, @ModelAttribute("form") EmmActionForm form, Model model) {
		loadViewData(admin, model, 0);

		return ACTIONS_VIEW;
	}

	@GetMapping("/{id:\\d+}/view.action")
	public String view(Admin admin, @PathVariable int id, @ModelAttribute("form") EmmActionForm form, Model model) {
		EmmAction emmAction = emmActionService.getEmmAction(id, admin.getCompanyID());
		if (emmAction == null) {
			logger.warn("loadAction: could not load action {}", id);
			return "redirect:/action/new.action";
		}

		model.addAttribute("form", conversionService.convert(emmAction, EmmActionForm.class));
		loadViewData(admin, model, id);

		return ACTIONS_VIEW;
	}

	@PostMapping("/save.action")
	public String save(Admin admin, @ModelAttribute("form") EmmActionForm form, Popups popups) {
		try {
			List<AbstractActionOperationParameters> parameters = actionOperationParametersParser.deSerializeActionModulesList(form.getModulesSchema());
			if (isValidAction(admin, form, parameters, popups)) {

				EmmAction action = new EmmActionImpl();
				action.setCompanyID(admin.getCompanyID());
				action.setId(form.getId());
				action.setType(form.getType());
				action.setShortname(form.getShortname());
				action.setDescription(form.getDescription());
				action.setIsActive(form.isActive());
				action.setAdvertising(form.isAdvertising());

				if (parameters == null) {
					parameters = new ArrayList<>();
				}

				action.setActionOperations(parameters);

				List<UserAction> userActions = new ArrayList<>();
				int actionId = emmActionService.saveEmmAction(admin.getCompanyID(), action, userActions);

				for (UserAction userAction : userActions) {
					writeUserActivityLog(admin, userAction);
				}

				popups.success(CHANGES_SAVED_MSG);

				return "redirect:/action/" + actionId + "/view.action";
			}
		} catch (Exception e) {
			logger.error("Saving action data failed: {}", e.getMessage(), e);
			popups.alert(ERROR_MSG);
		}

		return MESSAGES_VIEW;
	}

	private boolean isValidAction(Admin admin, EmmActionForm form, List<AbstractActionOperationParameters> params, Popups popups) {
		String shortname = form.getShortname();

		if (StringUtils.trimToNull(shortname) == null) {
			popups.field(SHORTNAME, "error.name.is.empty");
		} else if (StringUtils.trimToNull(shortname).length() < 3) {
			popups.field(SHORTNAME, "error.name.too.short");
		} else if (StringUtils.length(shortname) > 50) {
			popups.field(SHORTNAME, "error.action.nameTooLong");
		}
		
		if (CollectionUtils.isNotEmpty(params)) {
			try {
				for (ActionOperationParameters action : params) {
					SimpleServiceResult result = validationService.validate(admin, action);
					if (!result.isSuccess()) {
						result.getErrorMessages().forEach(popups::alert);
					}
				}
			} catch (Exception e) {
				logger.error("Action operation validation failed: {}", e.getMessage(), e);
				popups.alert(ERROR_MSG);
			}
		}

    	return popups.isEmpty();
    }

	@GetMapping("/{id:\\d+}/clone.action")
	public String clone(Admin admin, @PathVariable("id") int originId, Model model) {
		EmmActionDto copyOfAction = emmActionService.getCopyOfAction(admin, originId);

		EmmActionForm form = conversionService.convert(copyOfAction, EmmActionForm.class);
		model.addAttribute("form", form);

		loadViewData(admin, model, originId);

		return ACTIONS_VIEW;
	}

	@GetMapping("/{id:\\d+}/usage.action")
	public String usagesView(Admin admin, @PathVariable int id, Model model) {
		model.addAttribute("actionId", id);
		model.addAttribute(SHORTNAME, emmActionService.getEmmActionName(id, admin.getCompanyID()));
		model.addAttribute("webFormsByActionId", userFormService.getUserFormNamesByActionID(admin.getCompanyID(), id));
		model.addAttribute("dependentMailings", mailingService.getMailingsUsingEmmAction(id, admin.getCompanyID()));

		return "actions_view_forms";
	}

	private void loadViewData(Admin admin, Model model, int actionId) {
		model.addAttribute("operationList", actionOperationFactory.getTypesList());
	    model.addAttribute("isUnsubscribeExtended", true);
	    model.addAttribute("allowedMailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));

		// Some deserialized Actions need the mailings to show their configuration data
		model.addAttribute("eventBasedMailings", mailingService.getMailingsByStatusE(admin.getCompanyID()));
		model.addAttribute("archives", workflowService.getCampaignList(admin.getCompanyID(), SHORTNAME, 1));
		model.addAttribute("isForceSendingEnabled", configService.getBooleanValue(ConfigValue.ForceSending, admin.getCompanyID()));
		
		try {
			final List<ProfileField> profileFields = columnInfoService.getComColumnInfos(admin.getCompanyID(), admin.getAdminID(), false)
					.stream()
					.filter(field -> field.getModeEdit() != ProfileFieldMode.ReadOnly)
					.filter(field -> field.getModeEdit() != ProfileFieldMode.NotVisible)
					.collect(Collectors.toList());
			
			model.addAttribute("AVAILABLE_PROFILE_FIELDS", profileFields);
		} catch(final Exception e) {
			model.addAttribute("AVAILABLE_PROFILE_FIELDS", Collections.EMPTY_LIST);
		}
		
		model.addAttribute("ACTION_READONLY", !this.emmActionService.canUserSaveAction(admin, actionId));
	}

	private void writeUserActivityLog(Admin admin, UserAction userAction) {
        UserActivityUtil.log(userActivityLogService, admin, userAction, logger);
	}

	private void writeUserActivityLog(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, logger);
	}

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return "save".equals(controllerMethodName)
                && ("modules[].script".equals(param) || "modulesSchema".equals(param));
    }
}
