/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.common.exceptions.ShortnameTooShortException;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.form.MailinglistForm;
import com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import org.json.JSONArray;
import com.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.GuiConstants;
import com.agnitas.web.forms.BulkActionForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

public class MailinglistController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(MailinglistController.class);

	private static final String YEAR_LIST = "yearlist";
	private static final String MONTH_LIST = "monthList";
	private static final String BIRT_STATISTIC_URL_WITHOUT_FORMAT = "birtStatisticUrlWithoutFormat";

	protected final MailinglistService mailinglistService;
	protected final MailinglistApprovalService mailinglistApprovalService;
	protected final UserActivityLogService userActivityLogService;
	private final ConversionService conversionService;
	private final BirtStatisticsService birtStatisticsService;
	protected final AdminService adminService;
	protected final ConfigService configService;

	public MailinglistController(MailinglistService mailinglistService, UserActivityLogService userActivityLogService, ConversionService conversionService,
								 BirtStatisticsService birtStatisticsService, AdminService adminService, ConfigService configService,
								 MailinglistApprovalService mailinglistApprovalService) {
		this.mailinglistService = mailinglistService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.birtStatisticsService = birtStatisticsService;
		this.adminService = adminService;
		this.configService = configService;
		this.mailinglistApprovalService = mailinglistApprovalService;
	}

	@RequestMapping("/list.action")
	public String list(Admin admin, Model model, Popups popups) {
		JSONArray mailingListsJson = new JSONArray();

		try {
			mailingListsJson = mailinglistService.getMailingListsJson(admin);
			userActivityLogService.writeUserActivityLog(admin, "mailing lists", "active tab - mailing lists");
		} catch (Exception e) {
			logger.error("Error occurred: " + e, e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		model.addAttribute("mailingListsJson", mailingListsJson);
		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

		return "mailinglist_list";
	}

	@RequestMapping("/{id:\\d+}/view.action")
	public String view(Admin admin, @PathVariable int id, ModelMap model) {
		if (id == 0) {
			return "redirect:/mailinglist/create.action";
		}

		MailinglistForm form = null;

		if (admin.isRedesignedUiUsed()) {
			Mailinglist mailinglist = mailinglistService.getMailinglist(id, admin.getCompanyID());
			if (Objects.nonNull(mailinglist)) {
				form = conversionService.convert(mailinglist, MailinglistForm.class);
				model.addAttribute("isRestrictedForSomeAdmins", mailinglist.isRestrictedForSomeAdmins());
			}
		} else {
			if (model.containsKey("mailinglistForm")) {
				form = (MailinglistForm) model.get("mailinglistForm");
			} else {
				Mailinglist mailinglist = mailinglistService.getMailinglist(id, admin.getCompanyID());
				if (Objects.nonNull(mailinglist)) {
					form = conversionService.convert(mailinglist, MailinglistForm.class);
				}
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
		prepareAttributesForViewPage(model, admin);

		return "mailinglist_view";
	}

	@RequestMapping(value = "/create.action", method = {RequestMethod.GET, RequestMethod.POST})
	public String create(MailinglistForm form, ModelMap model, Admin admin) {
		prepareAttributesForViewPage(model, admin);
		return "mailinglist_view";
	}

	protected void prepareAttributesForViewPage(ModelMap model, Admin admin) {
		// empty
	}

	@PostMapping("/save.action")
	public String save(Admin admin, MailinglistForm form, RedirectAttributes redirectAttributes, Popups popups) {
		int companyId = admin.getCompanyID();

		if (!isValid(companyId, form, popups)) {
			return MESSAGES_VIEW;
		}

		try {
			int id = mailinglistService.saveMailinglist(companyId, conversionService.convert(form, MailinglistDto.class));
			logger.info("save Mailinglist with id: " + id);
			popups.success(CHANGES_SAVED_MSG);
			userActivityLogService.writeUserActivityLog(admin,
					(form.getId() == id ? "edit " : "create ") + "mailing list",
					String.format("%s (%d)", form.getShortname(), id), logger);
			if(form.getStatistic() != null) {
				redirectAttributes.addFlashAttribute("statisticParams", form.getStatistic());
			}
			return "redirect:/mailinglist/" + id + "/view.action";
		} catch(final ShortnameTooShortException e) {
			popups.fieldError("shortname",  "error.name.too.short");

			return MESSAGES_VIEW;
		}
	}
	
	@GetMapping("/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(Admin admin, @PathVariable("id") int mailinglistId, Model model, Popups popups) {
        int companyId = admin.getCompanyID();
        final Mailinglist mailinglist;

		if (admin.isRedesignedUiUsed()) {
			ServiceResult<List<Mailinglist>> result
					= mailinglistService.getAllowedForDeletion(Set.of(mailinglistId), admin);
			popups.addPopups(result);

			if (!result.isSuccess()) {
				return MESSAGES_VIEW;
			}

			mailinglist = result.getResult().get(0);
		} else {
			mailinglist = mailinglistService.getMailinglist(mailinglistId, companyId);
			if (mailinglist == null) {
				popups.alert(ERROR_MSG);
				return MESSAGES_VIEW;
			}

			if (isMailinglistsDependent(Collections.singleton(mailinglistId), admin, model)) {
				return MESSAGES_VIEW;
			}
		}

        model.addAttribute("mailinglistId", mailinglist.getId());
        model.addAttribute("mailinglistShortname", mailinglist.getShortname());
        model.addAttribute("sentMailingsCount", mailinglistService.getSentMailingsCount(mailinglistId, companyId));
        model.addAttribute("affectedReportsCount", mailinglistService.getAffectedReportsCount(mailinglistId, companyId));
        return "mailinglist_delete";
    }

    @RequestMapping("/confirmBulkDelete.action")
    public String confirmBulkDelete(Admin admin, @ModelAttribute("bulkDeleteForm") BulkActionForm form, Model model, Popups popups) {
        Set<Integer> bulkIds = new HashSet<>(form.getBulkIds());
        if (bulkIds.isEmpty() || bulkIds.stream().anyMatch(id -> id <= 0)) {
            popups.alert("bulkAction.nothing.mailinglist");
            return MESSAGES_VIEW;
        }

		if (admin.isRedesignedUiUsed()) {
			ServiceResult<List<Mailinglist>> result = mailinglistService.getAllowedForDeletion(bulkIds, admin);
			popups.addPopups(result);

			if (!result.isSuccess()) {
				return MESSAGES_VIEW;
			}

			model.addAttribute("items", result.getResult().stream().map(Mailinglist::getShortname).toList());
        } else {
			if (isMailinglistsDependent(bulkIds, admin, model)) {
				return MESSAGES_VIEW;
			}
		}

        return "mailinglist_bulk_delete";
    }

	@RequestMapping("/{id:\\d+}/delete.action")
	public String delete(Admin admin, @PathVariable("id") int mailinglistId, Model model, Popups popups) {
        int companyId = admin.getCompanyID();

		if (admin.isRedesignedUiUsed()) {
			List<Integer> ids = mailinglistService.delete(Set.of(mailinglistId), admin);

			writeDeleteUAL(admin, ids);
			popups.success(SELECTION_DELETED_MSG);
		} else {
			if (isMailinglistsDependent(Collections.singleton(mailinglistId), admin, model)) {
				return MESSAGES_VIEW;
			}
			if (mailinglistService.deleteMailinglist(mailinglistId, companyId)) {
				popups.success("default.selection.deleted");
				userActivityLogService.writeUserActivityLog(admin, "delete mailing list", getDescription(mailinglistId, companyId));
			} else {
				popups.alert("error.mailinglist.delete.last");
				return MESSAGES_VIEW;
			}
		}

        return "redirect:/mailinglist/list.action";
    }

	@PostMapping("/bulkDelete.action")
	public Object bulkDelete(Admin admin, BulkActionForm form, Model model, Popups popups) {
        Set<Integer> bulkIds = new HashSet<>(form.getBulkIds());

		if (admin.isRedesignedUiUsed()) {
			List<Integer> ids = mailinglistService.delete(bulkIds, admin);

			writeDeleteUAL(admin, ids);
			popups.success(SELECTION_DELETED_MSG);

			return ResponseEntity.ok(new DataResponseDto<>(ids, popups));
		} else {
			int companyId = admin.getCompanyID();
			if (isMailinglistsDependent(bulkIds, admin, model)) {
				return MESSAGES_VIEW;
			}
			mailinglistService.bulkDelete(bulkIds, companyId);

			bulkIds.stream()
					.map(id -> getDescription(id, companyId))
					.map(description -> new UserAction("delete mailinglist", description))
					.forEach(action -> userActivityLogService.writeUserActivityLog(admin, action, logger));

			popups.success(SELECTION_DELETED_MSG);
		}

        return "redirect:/mailinglist/list.action";
    }

	private void writeDeleteUAL(Admin admin, List<Integer> ids) {
		userActivityLogService.writeUserActivityLog(
				admin,
				"delete mailinglists",
				"deleted mailinglists with following ids: " + StringUtils.join(ids, ", ")
		);
	}

	@PostMapping("/recipientsDelete.action")
	public String recipientsDelete(Admin admin, @ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form, Popups popups) {
		int mailinglistId = form.getId();
		mailinglistService.deleteMailinglistBindingRecipients(admin.getCompanyID(), mailinglistId, form.isOnlyActiveUsers(), form.isNoAdminAndTestUsers());

		popups.success("mailinglist.recipients.deleted");

		userActivityLogService.writeUserActivityLog(admin, "delete recipients from mailing list", getDescription(form.getShortname(), form.getId()));
		return "redirect:/mailinglist/" + mailinglistId + "/view.action";
	}

	@GetMapping("/{id:\\d+}/recipientsDeleteSettings.action")
	public String recipientsDeleteSettings(Admin admin, @PathVariable("id") int mailinglistId, @ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		form.setId(mailinglistId);
		String shortname = mailinglistService.getMailinglistName(mailinglistId, admin.getCompanyID());
		form.setShortname(shortname);
		form.setOnlyActiveUsers(false);
		form.setNoAdminAndTestUsers(false);
		return "mailinglist_recipients_delete_settings";
	}

	@PostMapping("/confirmRecipientsDelete.action")
	public String confirmRecipientsDelete(@ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		return "mailinglist_recipients_delete";
	}

	private boolean isValid(int companyId, MailinglistForm form, Popups popups) {
		final String shortname = form.getShortname();
		if(StringUtils.isBlank(shortname)) {
			popups.fieldError("shortname",  "error.name.is.empty");
			return false;
		}
		if(shortname.length() < 3) {
			popups.fieldError("shortname",  "error.name.too.short");
			return false;
		}
		if (!mailinglistService.isShortnameUnique(shortname, form.getId(), companyId)) {
			popups.fieldError("shortname", "error.mailinglist.duplicate", shortname);
			return false;
		}

		return true;
	}

	private void loadStatistics(Admin admin, RecipientProgressStatisticDto statistic, MailinglistForm form, ModelMap model) {
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

	// TODO: EMMGUI-714: remove after remove of old design
	private boolean isMailinglistsDependent(Set<Integer> mailinglistIds, Admin admin, Model model) {
        List<Mailing> affectedMailings = mailinglistService.getUsedMailings(mailinglistIds, admin.getCompanyID());
        if (!affectedMailings.isEmpty()) {
			addAffectedMailingsToModel(model, affectedMailings);
            return true;
        }
        return false;
    }

	// TODO: EMMGUI-714: remove after remove of old design
	private void addAffectedMailingsToModel(Model model, List<Mailing> affectedMailings) {
        if (model instanceof RedirectAttributes attributes) {
            attributes.addFlashAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
            attributes.addFlashAttribute("affectedMailingsMessageKey", "error.mailinglist.cannot_delete_mailinglists");
            attributes.addFlashAttribute("affectedMailings", affectedMailings);
        } else {
            model.addAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_ALERT);
            model.addAttribute("affectedMailingsMessageKey","error.mailinglist.cannot_delete_mailinglists");
            model.addAttribute("affectedMailings", affectedMailings);
        }
    }

	protected String getDescription(String shortname, int id) {
		return String.format("%s (%d)", shortname, id);
	}
	
	protected String getDescription(MailinglistForm form) {
		return getDescription(form.getShortname(), form.getId());
	}
	
	protected String getDescription(int mailinglistId, int companyId) {
		String shortname = mailinglistService.getMailinglistName(mailinglistId, companyId);
		return getDescription(shortname, mailinglistId);
	}
}
