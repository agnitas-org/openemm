/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import java.util.List;
import java.util.Objects;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormForm;
import com.agnitas.emm.core.userform.form.UserFormsForm;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.actions.EmmAction;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.BulkActionFrom;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.WorkflowParameters;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userformNew")
@SuppressWarnings("all")
public class UserFormController {
	
	private static final Logger logger = Logger.getLogger(UserFormController.class);
	
	private WebStorage webStorage;
	private ComUserformService userformService;
	private ComEmmActionService emmActionService;
	private ConfigService configService;
	private UserActivityLogService userActivityLogService;
	private ExtendedConversionService conversionService;
	
	public UserFormController(WebStorage webStorage, ComUserformService userformService,
			ComEmmActionService emmActionService, ConfigService configService,
			UserActivityLogService userActivityLogService, ExtendedConversionService conversionService) {
		this.webStorage = webStorage;
		this.userformService = userformService;
		this.emmActionService = emmActionService;
		this.configService = configService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
	}
	
	@RequestMapping("/list.action")
	public String list(ComAdmin admin, @ModelAttribute("form") UserFormsForm form, Model model, Popups popups) {
		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
			FormUtils.syncNumberOfRows(webStorage, WebStorage.USERFORM_OVERVIEW, form);
			
			PaginatedListImpl<UserFormDto> userFormList = userformService
					.getUserFormsWithActionData(admin, form.getSort(), form.getOrder(), form.getPage(), form.getNumberOfRows(), form.getFilter());
			model.addAttribute("userFormList", userFormList);
			
			model.addAttribute("userFormURLPattern", AgnUtils.getUserFormUrlPattern(admin));
		} catch (Exception e) {
			logger.error("Getting user form list failed!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}
		
		return "userform_list_new";
	}
	
	@PostMapping("/saveActiveness.action")
	public String saveActiveness(ComAdmin admin, @ModelAttribute("form") UserFormsForm form, Popups popups) {
		UserAction userAction = userformService.setActiveness(admin.getCompanyID(), form.getActiveness());
		if (Objects.nonNull(userAction)) {
			writeUserActivityLog(admin, userAction, logger);
			popups.success("default.changes_saved");
		}
		
		return "redirect:/webform/list.action";
	}
	
	@GetMapping(value = {"/new.action", "/0/view.action"})
	public String create(ComAdmin admin, @ModelAttribute("form") UserFormForm form, Model model, WorkflowParameters workflowParams) {
		logger.error("Create new form");
		
		model.addAttribute("userFormURLPattern", AgnUtils.getUserFormUrlPattern(admin));
		List<EmmAction> emmActions = emmActionService.getEmmNotLinkActions(admin.getCompanyID(), false);
		model.addAttribute("emmActions", emmActions);
		
		return "userform_view_new";
	}
	
	@GetMapping("/{id:\\d+}/view.action")
	public String view(ComAdmin admin, @PathVariable int id,
			@ModelAttribute("form") UserFormForm form, Model model, Popups popups, WorkflowParameters workflowParams) {
		try {
			UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), id);
			model.addAttribute("form", conversionService.convert(userForm, UserFormForm.class));

			model.addAttribute("userFormURLPattern", AgnUtils.getUserFormUrlPattern(admin));
			
			List<EmmAction> emmActions = emmActionService.getEmmNotLinkActions(admin.getCompanyID(), false);
			model.addAttribute("emmActions", emmActions);
			
			writeUserActivityLog(admin, "view user form", String.format("%s (ID:%d)", userForm.getName(), userForm.getId()), logger);
		} catch (Exception e) {
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}
		return "userform_view_new";
	}
	
	@GetMapping("/import.action")
	public String importForm(ComAdmin admin) {
		logger.error("Import form");
		return "";
	}
	
	@GetMapping("/{id:\\d+}/export.action")
	public String exportForm(ComAdmin admin, @PathVariable int id) {
		logger.error("Import form");
		return "";
	}
	
	@GetMapping("/{id:\\d+}/clone.action")
	public String clone(ComAdmin admin, @PathVariable int id) {
		logger.error("Clone form");
		return "";
	}
	
	@PostMapping("/confirmBulkDelete.action")
	public String confirmBulkDelete(ComAdmin admin, BulkActionFrom form) {
		logger.error("Bulk confirm delete");
		return "";
	}
	
	@GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(ComAdmin admin, @PathVariable int id) {
		logger.error("Confirm delete userform with ID: " + id);
		return "";
	}
	
	private void writeUserActivityLog(ComAdmin admin, String action, String description, Logger logger) {
		writeUserActivityLog(admin, new UserAction(action, description), logger);
	}
	
	private void writeUserActivityLog(ComAdmin admin, UserAction userAction, Logger logger) {
		if (userActivityLogService != null) {
			userActivityLogService.writeUserActivityLog(admin, userAction, logger);
		} else {
			logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
			logger.info("Userlog: " + admin.getUsername() + " " + userAction.getAction() + " " +  userAction.getDescription());
		}
	}
}
