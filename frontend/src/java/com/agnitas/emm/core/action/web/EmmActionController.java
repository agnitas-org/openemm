/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.agnitas.actions.EmmAction;
import org.agnitas.actions.impl.EmmActionImpl;
import org.agnitas.beans.factory.ActionOperationFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
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

import com.agnitas.beans.ComAdmin;
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
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/action")
@PermissionMapping("action")
public class EmmActionController {

	/** The logger. */
	private static final Logger logger = LogManager.getLogger(EmmActionController.class);

    private WebStorage webStorage;
    private ComEmmActionService emmActionService;
    private MailingService mailingService;
    private ConfigService configService;
    private ComWorkflowService workflowService;
	private UserActivityLogService userActivityLogService;
	private ConversionService conversionService;
	private ActionOperationParametersParser actionOperationParametersParser;
	private EmmActionValidationServiceImpl validationService;
	private ActionOperationFactory actionOperationFactory;
	private ComUserformService userFormService;
	private MailinglistApprovalService mailinglistApprovalService;

	public EmmActionController(WebStorage webStorage, ComEmmActionService emmActionService, MailingService mailingService, ConfigService configService, ComWorkflowService workflowService, UserActivityLogService userActivityLogService, ConversionService conversionService, ActionOperationParametersParser actionOperationParametersParser, EmmActionValidationServiceImpl validationService, ActionOperationFactory actionOperationFactory, ComUserformService userFormService, MailinglistApprovalService mailinglistApprovalService) {
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
    }

	@RequestMapping("/list.action")
	public String list(ComAdmin admin, EmmActionsForm form, Model model, Popups popups) {
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
	public @ResponseBody BooleanResponseDto saveActiveness(ComAdmin admin, EmmActionsForm form, Popups popups) {
		List<UserAction> userActions = new ArrayList<>();
		boolean result = emmActionService.setActiveness(form.getActiveness(), admin.getCompanyID(), userActions);
		if (result) {
			for (UserAction action : userActions) {
				writeUserActivityLog(admin, action);
			}
			popups.success("default.changes_saved");
		} else {
			popups.alert("Error");
		}

		return new BooleanResponseDto(popups, result);
	}

	@PostMapping("/confirmBulkDelete.action")
	public String confirmBulkDelete(@ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
		if (CollectionUtils.isEmpty(form.getBulkIds())) {
			popups.alert("bulkAction.nothing.action");
			return "messages";
		}
		return "action_bulk_delete_ajax";
	}

	@RequestMapping(value = "/bulkDelete.action", method = { RequestMethod.POST, RequestMethod.DELETE})
    public String bulkDelete(ComAdmin admin, @ModelAttribute("bulkDeleteForm") BulkActionForm form, Popups popups) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.action");
            return "messages";
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

			popups.success("default.selection.deleted");
			return "redirect:/action/list.action";
		} catch (Exception e) {
			logger.error("Bulk deletion failed: " + e.getMessage(), e);
		}

        popups.alert("Error");
        return "messages";
    }

    @GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(ComAdmin admin, @PathVariable int id, @ModelAttribute("simpleActionForm") SimpleActionForm form, Popups popups) {
		EmmAction emmAction = emmActionService.getEmmAction(id, admin.getCompanyID());
		if (emmAction != null) {
			form.setId(emmAction.getId());
			form.setShortname(emmAction.getShortname());

			return "action_delete_ajax";
		}

		popups.alert("Error");
		return "messages";
	}

	@RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(ComAdmin admin, SimpleActionForm form, Popups popups) {
        if (emmActionService.deleteEmmAction(form.getId(), admin.getCompanyID())) {
			writeUserActivityLog(admin, "delete action", String.format("%s (ID: %d)", form.getShortname(), form.getId()));
			popups.success("default.selection.deleted");
			return "redirect:/action/list.action";
		}

        popups.alert("Error");
        return "messages";
    }

	@GetMapping(value = {"/new.action", "/0/view.action"})
	public String create(ComAdmin admin, @ModelAttribute("form") EmmActionForm form, Model model) {
		loadViewData(admin, model);

		return "actions_view";
	}

	@GetMapping("/{id:\\d+}/view.action")
	public String view(ComAdmin admin, @PathVariable int id, @ModelAttribute("form") EmmActionForm form, Model model) {
		EmmAction emmAction = emmActionService.getEmmAction(id, admin.getCompanyID());
		if (emmAction == null) {
			logger.warn("loadAction: could not load action " + id);
			return "redirect:/action/new.action";
		}

		model.addAttribute("form", conversionService.convert(emmAction, EmmActionForm.class));
		loadViewData(admin, model);

		return "actions_view";
	}

	@PostMapping("/save.action")
	public String save(ComAdmin admin, @ModelAttribute("form") EmmActionForm form, Popups popups) {
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

				if (parameters == null) {
					parameters = new ArrayList<>();
				}

				action.setActionOperations(parameters);

				List<UserAction> userActions = new ArrayList<>();
				int actionId = emmActionService.saveEmmAction(admin.getCompanyID(), action, userActions);

				for (UserAction userAction : userActions) {
					writeUserActivityLog(admin, userAction);
				}

				popups.success("default.changes_saved");

				return "redirect:/action/" + actionId + "/view.action";
			}
		} catch (Exception e) {
			logger.error("Saving action data failed: " + e.getMessage(), e);
			popups.alert("Error");
		}

		return "messages";
	}

	private boolean isValidAction(ComAdmin admin, EmmActionForm form, List<AbstractActionOperationParameters> params, Popups popups) {
		String shortname = form.getShortname();

		if (StringUtils.trimToNull(shortname) == null) {
			popups.field("shortname", "error.name.is.empty");
		} else if (StringUtils.trimToNull(shortname).length() < 3) {
			popups.field("shortname", "error.name.too.short");
		} else if (StringUtils.length(shortname) > 50) {
			popups.field("shortname", "error.action.nameTooLong");
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
				logger.error("Action operation validation failed: " + e.getMessage(), e);
				popups.alert("Error");
			}
		}

    	return popups.isEmpty();
    }

	@GetMapping("/{id:\\d+}/clone.action")
	public String clone(ComAdmin admin, @PathVariable("id") int originId, Model model) {
		EmmActionDto copyOfAction = emmActionService.getCopyOfAction(admin, originId);

		EmmActionForm form = conversionService.convert(copyOfAction, EmmActionForm.class);
		model.addAttribute("form", form);

		loadViewData(admin, model);

		return "actions_view";
	}

	@GetMapping("/{id:\\d+}/usage.action")
	public String webformsView(ComAdmin admin, @PathVariable int id, Model model) {
		model.addAttribute("actionId", id);
		model.addAttribute("shortname", emmActionService.getEmmActionName(id, admin.getCompanyID()));
		model.addAttribute("webFormsByActionId", userFormService.getUserFormNamesByActionID(admin.getCompanyID(), id));
		return "actions_view_forms";
	}

	private void loadViewData(ComAdmin admin, Model model) {
		model.addAttribute("operationList", actionOperationFactory.getTypesList());
	    model.addAttribute("isUnsubscribeExtended", true);
	    model.addAttribute("allowedMailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));

		// Some deserialized Actions need the mailings to show their configuration data
		model.addAttribute("eventBasedMailings", mailingService.getMailingsByStatusE(admin.getCompanyID()));
		model.addAttribute("archives", workflowService.getCampaignList(admin.getCompanyID(), "shortname", 1));
		model.addAttribute("isForceSendingEnabled", configService.getBooleanValue(ConfigValue.ForceSending, admin.getCompanyID()));
	}

	private void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
		if (userActivityLogService != null) {
			userActivityLogService.writeUserActivityLog(admin, userAction, logger);
		} else {
			logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
			logger.info("Userlog: " + admin.getUsername() + " " + userAction.getAction() + " " +  userAction.getDescription());
		}
	}

	private void writeUserActivityLog(ComAdmin admin, String action, String description) {
		if (userActivityLogService != null) {
			userActivityLogService.writeUserActivityLog(admin, action, description, logger);
		} else {
			logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
			logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
		}
	}
}
