/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.form.MailinglistForm;
import com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm;
import com.agnitas.emm.core.mailinglist.form.MailinglistRecipientForm;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import net.sf.json.JSONArray;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.web.forms.BulkActionFrom;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mailinglist")
@PermissionMapping("mailinglist")
public class MailinglistController {
	private static final Logger logger = Logger.getLogger(MailinglistController.class);

	private static final String YEAR_LIST = "yearlist";
	private static final String MONTH_LIST = "monthList";
	private static final String BIRT_STATISTIC_URL_WITHOUT_FORMAT = "birtStatisticUrlWithoutFormat";

	private final ComMailinglistService mailinglistService;
	private final MailinglistApprovalService mailinglistApprovalService;
	private final UserActivityLogService userActivityLogService;
	private final ConversionService conversionService;
	private final BirtStatisticsService birtStatisticsService;
	private final WebStorage webStorage;
	private final AdminService adminService;
	private final ConfigService configService;

	public MailinglistController(ComMailinglistService mailinglistService, UserActivityLogService userActivityLogService,
								 ConversionService conversionService, BirtStatisticsService birtStatisticsService,
								 WebStorage webStorage, AdminService adminService, ConfigService configService, final MailinglistApprovalService mailinglistApprovalService) {
		this.mailinglistService = mailinglistService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.birtStatisticsService = birtStatisticsService;
		this.webStorage = webStorage;
		this.adminService = adminService;
		this.configService = configService;
		this.mailinglistApprovalService = mailinglistApprovalService;
	}

	@RequestMapping("/list.action")
	public String list(ComAdmin admin, @ModelAttribute("mailinglistsForm") PaginationForm form, Model model, Popups popups) {
		JSONArray mailingListsJson = new JSONArray();

		try {
			FormUtils.syncNumberOfRows(webStorage, ComWebStorage.MAILINGLIST_OVERVIEW, form);
			mailingListsJson = mailinglistService.getMailingListsJson(admin);
			userActivityLogService.writeUserActivityLog(admin, "mailing lists", "active tab - mailing lists");
		} catch (Exception e) {
			logger.error("Error occurred: " + e, e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		model.addAttribute("mailingListsJson", mailingListsJson);
		model.addAttribute("dateFormatPattern", admin.getDateFormat().toPattern());

		return "mailinglist_list";
	}

	@RequestMapping("/{id:\\d+}/view.action")
	public String view(ComAdmin admin, @PathVariable int id, ModelMap model) throws Exception {
		if (id == 0) {
			return "redirect:/mailinglist/create.action";
		}

		MailinglistForm form = null;
		if (model.containsKey("mailinglistForm")) {
			form = (MailinglistForm) model.get("mailinglistForm");
		} else {
			Mailinglist mailinglist = mailinglistService.getMailinglist(id, admin.getCompanyID());
			if (Objects.nonNull(mailinglist)) {
				form = conversionService.convert(mailinglist, MailinglistForm.class);
			}
		}

		if (form == null) {
			form = new MailinglistForm();
		} else {
			RecipientProgressStatisticDto statistic = null;
			if (model.containsAttribute("statisticParams")) {
				statistic = (RecipientProgressStatisticDto) model.get("statisticParams");
			} else {
				statistic = form.getStatistic();
			}

			loadStatistics(admin, statistic, form, model);
			userActivityLogService.writeUserActivityLog(admin, "mailing list view", getDescription(form));
		}

		model.addAttribute("mailinglistForm", form);

		return "mailinglist_view";
	}

	@RequestMapping(value = "/create.action", method = {RequestMethod.GET, RequestMethod.POST})
	public String create(MailinglistForm form) {
		return "mailinglist_view";
	}

	@PostMapping("/save.action")
	public String save(ComAdmin admin, @Validated MailinglistForm form, BindingResult result,
					   RedirectAttributes redirectAttributes, Popups popups) {
		int companyId = admin.getCompanyID();

		if (isValid(companyId, form, result, popups)) {
			int id = mailinglistService.saveMailinglist(companyId, conversionService.convert(form, MailinglistDto.class));
			logger.info("save Mailinglist with id: " + id);
			popups.success("default.changes_saved");
			userActivityLogService.writeUserActivityLog(admin,
					(form.getId() == id ? "edit " : "create ") + "mailing list",
					String.format("%s (%d)", form.getShortname(), id), logger);
			if(form.getStatistic() != null) {
				redirectAttributes.addFlashAttribute("statisticParams", form.getStatistic());
			}
			return "redirect:/mailinglist/" + id + "/view.action";
		} else {
			return "messages";
		}
	}
	
	@GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(ComAdmin admin, @PathVariable("id") int mailinglistId, MailinglistForm form, Model model, Popups popups) {
		int companyId = admin.getCompanyID();

		if (isMailinglistIndependent(mailinglistId, companyId, model)) {
			Mailinglist mailinglist = mailinglistService.getMailinglist(mailinglistId, companyId);
			
			if (mailinglist == null) {
				model.addAttribute("excludeDialog", true);
				popups.alert("Error");
			} else {
				form.setId(mailinglist.getId());
				form.setShortname(mailinglist.getShortname());
			}
		} else {
			model.addAttribute("excludeDialog", true);
		}

		return "mailinglist_delete";
	}

    @PostMapping("/confirmBulkDelete.action")
    public String confirmBulkDelete(ComAdmin admin, @ModelAttribute("bulkDeleteForm") BulkActionFrom form, Model model, Popups popups) {
        if (form.getBulkIds().size() == 0) {
            popups.alert("bulkAction.nothing.mailinglist");
        } else if (!isMailinglistsIndependent(form.getBulkIds(), admin.getCompanyID(), model)) {
			model.addAttribute("excludeDialog", true);
        }

        return "mailinglist_bulk_delete";
    }

	@RequestMapping("/{id:\\d+}/delete.action")
	public String delete(ComAdmin admin, @PathVariable("id") int mailinglistId, Popups popups) {
		if(mailinglistService.deleteMailinglist(mailinglistId, admin.getCompanyID())) {
			popups.success("default.selection.deleted");
			userActivityLogService.writeUserActivityLog(admin, "delete mailing list", getDescription(mailinglistId, admin.getCompanyID()));
		} else {
			popups.alert("error.mailinglist.cannot_delete");
		}
		
		return "redirect:/mailinglist/list.action";
	}
	
	@PostMapping("/bulkDelete.action")
	public String bulkDelete(ComAdmin admin, BulkActionFrom form, RedirectAttributes model, Popups popups) {
		int companyId = admin.getCompanyID();
		if(isMailinglistsIndependent(form.getBulkIds(), companyId, model)) {
			List<UserAction> userActions = form.getBulkIds().stream()
					.map(id -> getDescription(id, companyId))
					.map(description -> new UserAction("delete mailinglist", description))
					.collect(Collectors.toList());
			
			mailinglistService.bulkDelete(new HashSet<>(form.getBulkIds()), companyId);
			
			for (UserAction action: userActions) {
				userActivityLogService.writeUserActivityLog(admin, action, logger);
			}
			
			popups.success("default.selection.deleted");
		}
		
		return "redirect:/mailinglist/list.action";
	}
	
	@PostMapping("/{id:\\d+}/users.action")
	public String recipientList(ComAdmin admin, @PathVariable int id, Model model) {
		int companyId = admin.getCompanyID();
		
		model.addAttribute("recipientMap", adminService.getAdminsNamesMap(companyId));
		
		MailinglistRecipientForm form = new MailinglistRecipientForm();
		form.setMailinglistId(id);
		form.setMailinglistShortname(mailinglistService.getMailinglistName(id, companyId));
		form.setAllowedRecipientIds(mailinglistApprovalService.getAdminsAllowedToUseMailinglist(companyId, id));
		
		model.addAttribute("mailinglistRecipientForm", form);
		
		userActivityLogService.writeUserActivityLog(admin, "mailing list edit", "active tab - approval", logger);

		return "mailinglist_users_view";
	}
	
	@PostMapping("/saveUsers.action")
	public String recipientSave(ComAdmin admin, @ModelAttribute("form") MailinglistRecipientForm form, Popups popups) {
		List<UserAction> userActions = new ArrayList<>();
		if(mailinglistApprovalService.editUsersApprovalPermissions(admin.getCompanyID(), form.getMailinglistId(), form.getAllowedRecipientIds(), userActions)) {
			popups.success("default.changes_saved");
			for(UserAction action: userActions) {
				userActivityLogService.writeUserActivityLog(admin, action, logger);
			}
		} else {
			popups.alert("changes_not_saved");
		}
		
		return "redirect:/mailinglist/" + form.getMailinglistId() + "/view.action";
	}

	@GetMapping("/{id:\\d+}/usersDeleteSettings.action")
	public String recipientsDeleteSettings(ComAdmin admin, @PathVariable("id") int mailinglistId, @ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		form.setMailinglistId(mailinglistId);
		String shortname = mailinglistService.getMailinglistName(mailinglistId, admin.getCompanyID());
		form.setMailinglistShortname(shortname);
		form.setOnlyActiveUsers(false);
		form.setNoAdminAndTestUsers(false);
		return "mailinglist_recipients_delete_settings";
	}

	@PostMapping("/confirmRecipientsDelete.action")
	public String confirmRecipientsDelete(@ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		return "mailinglist_recipients_delete";
	}
	
	@PostMapping("/usersDelete.action")
	public String recipientsDelete(ComAdmin admin, @ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form, Popups popups) {
		int mailinglistId = form.getMailinglistId();
		mailinglistService.deleteMailinglistBindingRecipients(admin.getCompanyID(), mailinglistId, form.isOnlyActiveUsers(), form.isNoAdminAndTestUsers());
		
		popups.success("mailinglist.recipients.deleted");
		
		userActivityLogService.writeUserActivityLog(admin, "delete recipients from mailing list", getDescription(form.getMailinglistShortname(), form.getMailinglistId()));
		return "redirect:/mailinglist/" + mailinglistId + "/view.action";
	}
	
	private boolean isValid(int companyId, MailinglistForm form, BindingResult result, Popups popups) {
		if (result.hasErrors()) {
			return false;
		}
		
		String newShortname = form.getShortname();
		if (!mailinglistService.isShortnameUnique(newShortname, form.getId(), companyId)) {
			popups.field("shortname", "error.mailinglist.duplicate", newShortname);
			return false;
		}
		
		return true;
	}

	private void loadStatistics(ComAdmin admin, RecipientProgressStatisticDto statistic, MailinglistForm form, ModelMap model) throws Exception {
		if (statistic == null) {
			statistic = new RecipientProgressStatisticDto();
		}

		statistic.setMailinglistId(form.getId());

		if (statistic.getStartYear() == 0 && statistic.getStartMonth() == 0) {
			Calendar currentDate = Calendar.getInstance(AgnUtils.getTimeZone(admin));
			currentDate.set(Calendar.DAY_OF_MONTH, 1);

			statistic.setStartYear(currentDate.get(Calendar.YEAR));
			statistic.setStartMonth(currentDate.get(Calendar.MONTH));
		}

		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		String urlWithoutFormat = birtStatisticsService.getRecipientMonthlyStatisticsUrlWithoutFormat(admin, sessionId, statistic);

		model.addAttribute(YEAR_LIST, AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
		model.addAttribute(MONTH_LIST, AgnUtils.getMonthList());
		model.addAttribute(BIRT_STATISTIC_URL_WITHOUT_FORMAT, urlWithoutFormat);

		form.setStatistic(statistic);
	}

	private boolean isMailinglistsIndependent(List<Integer> bulkMailinglistIds, int companyId, Model model) {
		List<Mailing> dependedMailings = mailinglistService.getAllDependedMailing(new HashSet<>(bulkMailinglistIds), companyId);

		if (!dependedMailings.isEmpty()) {
			if(model instanceof RedirectAttributes) {
				RedirectAttributes attributes = (RedirectAttributes) model;
				attributes.addFlashAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
				attributes.addFlashAttribute("affectedMailingsMessageKey", "error.mailinglist.cannot_delete_mailinglists");
				attributes.addFlashAttribute("affectedMailings", dependedMailings);
			} else {
				model.addAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
				model.addAttribute("affectedMailingsMessageKey","error.mailinglist.cannot_delete_mailinglists");
				model.addAttribute("affectedMailings", dependedMailings);
			}
			return false;
		}

		return true;
	}

	private boolean isMailinglistIndependent(int mailinglistId, int companyId, Model model) {
		if (!isMailinglistsIndependent(Collections.singletonList(mailinglistId), companyId, model)) {
			return false;
		}

		List<ComLightweightBirtReport> affectedReports =  mailinglistService.getConnectedBirtReportList(mailinglistId, companyId);
		if (!affectedReports.isEmpty()) {
			model.addAttribute("affectedReportsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
			model.addAttribute("affectedReportsMessageKey", "warning.mailinglist.affectedBirtReports");
			model.addAttribute("affectedReports", affectedReports);
			
			return false;
		}

		return true;
	}
	
	private String getDescription(String shortname, int id) {
		return String.format("%s (%d)", shortname, id);
	}
	
	private String getDescription(MailinglistForm form) {
		return getDescription(form.getShortname(), form.getId());
	}
	
	private String getDescription(int mailinglistId, int companyId) {
		String shortname = mailinglistService.getMailinglistName(mailinglistId, companyId);
		return getDescription(shortname, mailinglistId);
	}
}
