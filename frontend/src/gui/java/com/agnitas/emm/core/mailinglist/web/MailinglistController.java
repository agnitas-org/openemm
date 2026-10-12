/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.recipient.PeriodProgressUiRow;
import com.agnitas.emm.core.mailinglist.MailinglistUiService;
import com.agnitas.emm.core.mailinglist.form.MailinglistForm;
import com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm;
import com.agnitas.emm.core.mailinglist.mapper.MailinglistUiMapper;
import com.agnitas.emm.core.mailinglist.model.MailinglistRecipientsDeletionResult;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mailinglist.validation.MailinglistConstraints;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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
import org.springframework.web.bind.annotation.ResponseBody;

public class MailinglistController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(MailinglistController.class);

	private static final String YEAR_LIST = "yearlist";
	private static final String MONTH_LIST = "monthList";

	protected final MailinglistService mailinglistService;
	protected final UserActivityLogService userActivityLogService;
	private final MailinglistUiMapper mailinglistMapper;
	protected final AdminService adminService;
	private final MailinglistUiService mailinglistUiService;

	public MailinglistController(
			MailinglistService mailinglistService,
			UserActivityLogService userActivityLogService,
			MailinglistUiMapper mailinglistMapper,
			AdminService adminService,
			MailinglistUiService mailinglistUiService
	) {
		this.mailinglistService = mailinglistService;
		this.userActivityLogService = userActivityLogService;
		this.mailinglistMapper = mailinglistMapper;
		this.adminService = adminService;
		this.mailinglistUiService = mailinglistUiService;
	}

	@RequestMapping("/list.action")
	@RequiredPermission("mailinglist.show")
	public String list(Admin admin, Model model) {
		JSONArray mailingListsJson = mailinglistService.getMailingListsJson(admin);
		userActivityLogService.writeUserActivityLog(admin, "mailing lists", "active tab - mailing lists");

		model.addAttribute("mailingListsJson", mailingListsJson);
		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

		return "mailinglist_list";
	}

	@RequestMapping("/{id:\\d+}/view.action")
	@RequiredPermission("mailinglist.show")
	public String view(Admin admin, @PathVariable int id, ModelMap model) {
		if (id == 0) {
			return "redirect:/mailinglist/create.action";
		}

		MailinglistForm form = null;

		Mailinglist mailinglist = mailinglistService.getOrNull(id, admin.getCompanyID());
		if (Objects.nonNull(mailinglist)) {
			form = mailinglistMapper.toForm(mailinglist);
			model.addAttribute("isRestrictedForSomeAdmins", mailinglist.isRestrictedForSomeAdmins());
		}

		if (form == null) {
			form = new MailinglistForm();
		} else {
			model.addAttribute(YEAR_LIST, AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
			model.addAttribute(MONTH_LIST, AgnUtils.getMonthList());
			model.addAttribute("currentMonth", LocalDate.now(admin.getZoneId()).getMonthValue());
			model.addAttribute("currentYear", LocalDate.now(admin.getZoneId()).getYear());
			userActivityLogService.writeUserActivityLog(admin, "mailing list view", getDescription(form));
		}

		model.addAttribute("mailinglistForm", form);
		prepareAttributesForViewPage(model, admin);

		return "mailinglist_view";
	}

	@GetMapping("/{mailinglistId:\\d+}/stats.action")
	@RequiredPermission("mailinglist.show")
	@ResponseBody
	public List<PeriodProgressUiRow> getStats(
			@PathVariable int mailinglistId,
			@RequestParam int month,
			@RequestParam int year,
			Admin admin
	) {
		return mailinglistUiService.getStats(mailinglistId, month, year, admin);
	}

	@RequestMapping(value = "/create.action", method = {RequestMethod.GET, RequestMethod.POST})
	@RequiredPermission("mailinglist.change")
	public String create(MailinglistForm form, ModelMap model, Admin admin) {
		prepareAttributesForViewPage(model, admin);
		return "mailinglist_view";
	}

	protected void prepareAttributesForViewPage(ModelMap model, Admin admin) {
		// empty
	}

	@PostMapping("/save.action")
	@RequiredPermission("mailinglist.change")
	public String save(Admin admin, MailinglistForm form, Popups popups) {
		int companyId = admin.getCompanyID();

		if (!isValid(companyId, form, popups)) {
			return MESSAGES_VIEW;
		}

		int id = mailinglistService.saveMailinglist(companyId, mailinglistMapper.toDto(form));
		logger.info("save Mailinglist with id: {}", id);
		popups.changesSaved();
		userActivityLogService.writeUserActivityLog(admin,
				(form.getId() == id ? "edit " : "create ") + "mailing list",
				String.format("%s (%d)", form.getShortname(), id), logger);
		return redirectToView(id);
	}
	
	@GetMapping("/{id:\\d+}/confirmDelete.action")
	@RequiredPermission("mailinglist.delete")
	public String confirmDelete(Admin admin, @PathVariable("id") int mailinglistId, Model model, Popups popups) {
        ServiceResult<List<Mailinglist>> result = mailinglistService.getAllowedForDeletion(Set.of(mailinglistId), admin);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

		int companyId = admin.getCompanyID();
		Mailinglist mailinglist = result.getResult().getFirst();

        model.addAttribute("mailinglistId", mailinglist.getId());
        model.addAttribute("mailinglistShortname", mailinglist.getShortname());
        model.addAttribute("sentMailingsCount", mailinglistService.getSentMailingsCount(mailinglistId, companyId));
        model.addAttribute("affectedReportsCount", mailinglistService.getAffectedReportsCount(mailinglistId, companyId));
        return "mailinglist_delete";
    }

    @RequestMapping("/confirmBulkDelete.action")
	@RequiredPermission("mailinglist.delete")
	public String confirmBulkDelete(Admin admin, @ModelAttribute("bulkDeleteForm") BulkActionForm form, Model model, Popups popups) {
        Set<Integer> bulkIds = new HashSet<>(form.getBulkIds());
        if (bulkIds.isEmpty() || bulkIds.stream().anyMatch(id -> id <= 0)) {
            popups.alert("bulkAction.nothing.mailinglist");
            return MESSAGES_VIEW;
        }

		ServiceResult<List<Mailinglist>> result = mailinglistService.getAllowedForDeletion(bulkIds, admin);
		popups.addPopups(result);

		if (!result.isSuccess()) {
			return MESSAGES_VIEW;
		}

		model.addAttribute("items", result.getResult().stream().map(Mailinglist::getShortname).toList());

        return "mailinglist_bulk_delete";
    }

	@RequestMapping("/{id:\\d+}/delete.action")
	@RequiredPermission("mailinglist.delete")
	public String delete(Admin admin, @PathVariable("id") int mailinglistId, Popups popups) {
		List<Integer> ids = mailinglistService.delete(Set.of(mailinglistId), admin);

		writeDeleteUAL(admin, ids);
		popups.selectionDeleted();

        return "redirect:/mailinglist/list.action";
    }

	@PostMapping("/bulkDelete.action")
	@RequiredPermission("mailinglist.delete")
	public Object bulkDelete(Admin admin, BulkActionForm form, Popups popups) {
		List<Integer> ids = mailinglistService.delete(Set.copyOf(form.getBulkIds()), admin);

		writeDeleteUAL(admin, ids);
		popups.selectionDeleted();

		return ResponseEntity.ok(new DataResponseDto<>(ids, popups));
    }

	private void writeDeleteUAL(Admin admin, List<Integer> ids) {
		userActivityLogService.writeUserActivityLog(
				admin,
				"delete mailinglists",
				"deleted mailinglists with following ids: " + StringUtils.join(ids, ", ")
		);
	}

	@PostMapping("/recipientsDelete.action")
	@RequiredPermission("mailinglist.recipients.delete")
	public String recipientsDelete(Admin admin, MailinglistRecipientDeleteForm form, Popups popups) {
		MailinglistRecipientsDeletionResult result = mailinglistService.deleteMailinglistBindingRecipients(
				admin.getCompanyID(),
                form.getId(),
				form.isOnlyActiveUsers(),
				form.isNoAdminAndTestUsers()
		);

		popups.success("mailinglist.recipients.deleted");

		if (result.hasDeletions()) {
			String ualDescription = "Deleted %d recipients and %d bindings from mailing list '%s'".formatted(
					result.recipientsCount(),
					result.bindingsCount(),
					getDescription(form.getShortname(), form.getId())
			);

			userActivityLogService.writeUserActivityLog(admin, "delete recipients from mailing list", ualDescription);
		}

		return redirectToView(form.getId());
	}

	protected String redirectToView(int mailinglistId) {
		return "redirect:/mailinglist/%d/view.action".formatted(mailinglistId);
	}

	@GetMapping("/{id:\\d+}/recipientsDeleteSettings.action")
	@RequiredPermission("mailinglist.recipients.delete")
	public String recipientsDeleteSettings(Admin admin, @PathVariable("id") int mailinglistId, @ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		form.setId(mailinglistId);
		String shortname = mailinglistService.getMailinglistName(mailinglistId, admin.getCompanyID());
		form.setShortname(shortname);
		form.setOnlyActiveUsers(false);
		form.setNoAdminAndTestUsers(false);
		return "mailinglist_recipients_delete_settings";
	}

	@PostMapping("/confirmRecipientsDelete.action")
	@RequiredPermission("mailinglist.recipients.delete")
	public String confirmRecipientsDelete(@ModelAttribute("deleteForm") MailinglistRecipientDeleteForm form) {
		return "mailinglist_recipients_delete";
	}

	private boolean isValid(int companyId, MailinglistForm form, Popups popups) {
		final String shortname = form.getShortname();
		if (StringUtils.isBlank(shortname)) {
			popups.fieldError("shortname",  "error.name.is.empty");
			return false;
		}

		if (shortname.length() < MailinglistConstraints.NAME_MIN_LENGTH) {
			popups.fieldError("shortname",  "error.name.too.short");
			return false;
		}
		if (!mailinglistService.isShortnameUnique(shortname, form.getId(), companyId)) {
			popups.fieldError("shortname", "error.mailinglist.duplicate", shortname);
			return false;
		}

		return true;
	}

	protected String getDescription(String shortname, int id) {
		return String.format("%s (%d)", shortname, id);
	}
	
	protected String getDescription(MailinglistForm form) {
		return getDescription(form.getShortname(), form.getId());
	}

}
