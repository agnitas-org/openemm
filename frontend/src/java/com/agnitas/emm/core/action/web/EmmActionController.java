/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.web;

import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LightProfileField;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.factory.ActionOperationFactory;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.bean.EmmActionImpl;
import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.form.EmmActionForm;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.action.service.impl.EmmActionValidationServiceImpl;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.exception.ForbiddenException;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class EmmActionController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(EmmActionController.class);

    private static final String ACTIONS_VIEW = "actions_view";
    private static final String SHORTNAME = "shortname";
	private static final String REDIRECT_TO_OVERVIEW = "redirect:/action/list.action";

    private final EmmActionService emmActionService;
    private final MailingService mailingService;
    private final ConfigService configService;
    private final WorkflowService workflowService;
	private final UserActivityLogService userActivityLogService;
	private final ConversionService conversionService;
	private final ActionOperationParametersParser actionOperationParametersParser;
	private final EmmActionValidationServiceImpl validationService;
	private final ActionOperationFactory actionOperationFactory;
	private final MailinglistApprovalService mailinglistApprovalService;
	private final ColumnInfoService columnInfoService;

	public EmmActionController(EmmActionService emmActionService, MailingService mailingService, ConfigService configService,
							   WorkflowService workflowService, UserActivityLogService userActivityLogService, ConversionService conversionService,
							   ActionOperationParametersParser actionOperationParametersParser, EmmActionValidationServiceImpl validationService,
							   ActionOperationFactory actionOperationFactory, MailinglistApprovalService mailinglistApprovalService,
							   ColumnInfoService columnInfoService) {

		this.emmActionService = emmActionService;
		this.mailingService = mailingService;
		this.configService = configService;
		this.workflowService = workflowService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.actionOperationParametersParser = actionOperationParametersParser;
		this.validationService = validationService;
		this.actionOperationFactory = actionOperationFactory;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.columnInfoService = Objects.requireNonNull(columnInfoService, "columnInfoService");
    }

	@RequestMapping("/list.action")
	@RequiredPermission("actions.show")
	public String list(Admin admin, Model model, Popups popups) {
		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

			model.addAttribute("actionListJson", emmActionService.getEmmActionsJson(admin));
		} catch (Exception e) {
			logger.error("Getting emm action list failed!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "actions_list";
	}

	@PostMapping("/changeActiveness.action")
	@RequiredPermission("actions.change")
	public Object changeActiveness(@RequestParam(required = false) Set<Integer> ids, Admin admin, Popups popups,
								   @RequestParam boolean activate, @RequestParam(defaultValue = "false") boolean fromOverview) {
		validateSelectedIds(ids);

		ServiceResult<List<EmmAction>> result = emmActionService.setActiveness(ids, admin.getCompanyID(), activate);
		popups.addPopups(result);

		Collection<Integer> affectedIds = CollectionUtils.emptyIfNull(result.getResult())
				.stream()
				.map(EmmAction::getId)
				.toList();

		if (result.isSuccess()) {
			writeChangeActivenessToUAL(affectedIds, activate, admin);
			if (popups.isEmpty()) {
				popups.changesSaved();
			}
		}

		return fromOverview
				? ResponseEntity.ok(new DataResponseDto<>(affectedIds, popups, result.isSuccess()))
				: redirectToView(ids.iterator().next());
	}

	@GetMapping(value = "/delete.action")
	@RequiredPermission("actions.delete")
	public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model) {
		validateSelectedIds(bulkIds);
		List<String> items = emmActionService.getActionsNames(bulkIds, admin.getCompanyID());
		MvcUtils.addDeleteAttrs(model, items,
                "action.delete", "action.delete.question",
                "bulkAction.delete.action", "bulkAction.delete.action.question");
		return DELETE_VIEW;
	}

	@RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
	@RequiredPermission("actions.delete")
	public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
		validateSelectedIds(bulkIds);

		Map<Integer, String> descriptions = bulkIds.stream()
				.collect(Collectors.toMap(Function.identity(), id -> emmActionService.getEmmActionName(id, admin.getCompanyID())));

		emmActionService.bulkDelete(bulkIds, admin.getCompanyID());
		bulkIds.forEach(id -> writeUserActivityLog(admin, "delete action", descriptions.get(id)));

		popups.selectionDeleted();
		return REDIRECT_TO_OVERVIEW;
	}

	@PostMapping("/restore.action")
	@RequiredPermission("actions.change")
	public ResponseEntity<BooleanResponseDto> restore(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
		validateSelectedIds(bulkIds);
		emmActionService.restore(bulkIds, admin.getCompanyID());
		popups.changesSaved();
		return ResponseEntity.ok(new BooleanResponseDto(popups, true));
	}

	private void validateSelectedIds(Set<Integer> bulkIds) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			throw new BadRequestException(NOTHING_SELECTED_MSG);
		}
	}

	@GetMapping(value = {"/new.action", "/0/view.action"})
	@RequiredPermission("actions.show")
	public String create(Admin admin, @ModelAttribute("form") EmmActionForm form, Model model) {
		loadViewData(admin, model, 0);

		return ACTIONS_VIEW;
	}

	@GetMapping("/{id:\\d+}/view.action")
	@RequiredPermission("actions.show")
	public String view(Admin admin, @PathVariable int id, @ModelAttribute("form") EmmActionForm form, Model model) {
		EmmAction emmAction = emmActionService.getEmmAction(id, admin.getCompanyID());
		if (emmAction == null) {
			logger.warn("loadAction: could not load action {}", id);
			return "redirect:/action/new.action";
		}

        emmAction.getActionOperations().forEach(operation
                -> operation.setReadonly(emmActionService.isReadonlyOperation(operation, admin)));

		model.addAttribute("form", conversionService.convert(emmAction, EmmActionForm.class));
		loadViewData(admin, model, id);
		model.addAttribute("isActive", emmAction.getIsActive());

		return ACTIONS_VIEW;
	}

    @PostMapping("/save.action")
	@RequiredPermission("actions.change")
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
				action.setIsActive(emmActionService.isActive(form.getId()));
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

				popups.changesSaved();

				return redirectToView(actionId);
			}
		} catch (Exception e) {
			logger.error("Saving action data failed: {}", e.getMessage(), e);
			popups.defaultError();
		}

		return MESSAGES_VIEW;
	}

	private static String redirectToView(int actionId) {
		return "redirect:/action/" + actionId + "/view.action";
	}

	private boolean isValidAction(Admin admin, EmmActionForm form, List<AbstractActionOperationParameters> params, Popups popups) {
        if (emmActionService.containsReadonlyOperations(form.getId(), admin)) {
            throw new ForbiddenException();
        }

		String shortname = form.getShortname();

		if (StringUtils.trimToNull(shortname) == null) {
			popups.fieldError(SHORTNAME, "error.name.is.empty");
		} else if (StringUtils.trimToNull(shortname).length() < 3) {
			popups.fieldError(SHORTNAME, "error.name.too.short");
		} else if (StringUtils.length(shortname) > 50) {
			popups.fieldError(SHORTNAME, "error.action.nameTooLong");
		}
		
		if (CollectionUtils.isNotEmpty(params)) {
			for (ActionOperationParameters action : params) {
				SimpleServiceResult result = validationService.validate(admin, action);
				if (!result.isSuccess()) {
					result.getErrorMessages().forEach(popups::alert);
				}
			}
		}

    	return popups.isEmpty();
    }

	@GetMapping("/{id:\\d+}/clone.action")
	@RequiredPermission("actions.change")
	public String clone(Admin admin, @PathVariable("id") int originId, Model model) {
		EmmActionDto copyOfAction = emmActionService.getCopyOfAction(admin, originId);

		EmmActionForm form = conversionService.convert(copyOfAction, EmmActionForm.class);
		model.addAttribute("form", form);

		loadViewData(admin, model, originId);

		return ACTIONS_VIEW;
	}

	protected void loadViewData(Admin admin, Model model, int actionId) {
		model.addAttribute("operationList", actionOperationFactory.getTypesList());
	    model.addAttribute("allowedMailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));

		// Some deserialized Actions need the mailings to show their configuration data
		model.addAttribute("eventBasedMailings", mailingService.getMailingsByStatusE(admin.getCompanyID()));
		model.addAttribute("archives", workflowService.getCampaignList(admin.getCompanyID(), SHORTNAME, 1));
		model.addAttribute("isForceSendingEnabled", configService.getBooleanValue(ConfigValue.ForceSending, admin.getCompanyID()));
		
		try {
            Map<Boolean, List<ProfileField>> profileFields = columnInfoService.getComColumnInfos(admin.getCompanyID(), admin.getAdminID(), false)
					.stream()
                    .collect(Collectors.partitioningBy(emmActionService::isReadonlyOperationRecipientField));

            model.addAttribute("AVAILABLE_PROFILE_FIELDS", profileFields.get(false));
            model.addAttribute("HIDDEN_PROFILE_FIELDS", getHiddenFields(profileFields));
		} catch(final Exception e) {
			model.addAttribute("AVAILABLE_PROFILE_FIELDS", Collections.emptyList());
            model.addAttribute("HIDDEN_PROFILE_FIELDS", Collections.emptyList());
		}
		
		model.addAttribute("ACTION_READONLY", emmActionService.containsReadonlyOperations(actionId, admin));

		if (actionId > 0) {
			model.addAttribute("dependencies", emmActionService.getDependencies(actionId, admin.getCompanyID()));
		}
	}

    private Map<String, String> getHiddenFields(Map<Boolean, List<ProfileField>> profileFields) {
        return profileFields.get(true).stream()
                .collect(Collectors.toMap(LightProfileField::getColumn, LightProfileField::getShortname));
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
                && ("modules[].script".equals(param) || "modulesSchema".equals(param) || "modules[].htmlMail".equals(param));
    }

	private void writeChangeActivenessToUAL(Collection<Integer> ids, boolean activeness, Admin admin) {
		String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
		writeUserActivityLog(admin, (activeness ? "activate" : "deactivate") + " actions", "IDs: " + idsStr);
	}

}
