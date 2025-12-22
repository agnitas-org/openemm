/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.web;

import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.updateForwardParameters;
import static com.agnitas.service.WebStorage.ARCHIVE_MAILINGS_OVERVIEW;
import static com.agnitas.service.WebStorage.ARCHIVE_OVERVIEW;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.UserActivityUtil.addChangedFieldLog;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.impl.CampaignImpl;
import com.agnitas.emm.core.archive.forms.MailingArchiveForm;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mailing/archive")
public class MailingArchiveController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingArchiveController.class);

    private final WebStorage webStorage;
    private final CampaignService campaignService;
    private final UserActivityLogService userActivityLogService;

    public MailingArchiveController(WebStorage webStorage, CampaignService campaignService, UserActivityLogService userActivityLogService) {
        this.webStorage = webStorage;
        this.campaignService = campaignService;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping(value = "/list.action")
    @RequiredPermission("campaign.show")
    public String list(Admin admin, Model model, @ModelAttribute("form") PaginationForm form, @RequestParam(required = false) Boolean restoreSort) {
        FormUtils.syncPaginationData(webStorage, ARCHIVE_OVERVIEW, form, restoreSort);
        model.addAttribute("campaignList", campaignService.getOverview(admin, form));

        return "archive_list";
    }

    @GetMapping("/{id:\\d+}/view.action")
    @RequiredPermission("campaign.show")
    public String view(@PathVariable(name = "id") int id, @ModelAttribute("form") MailingArchiveForm archiveForm, Admin admin, Model model, HttpServletRequest req) {
        updateForwardParameters(req, true);
        Campaign campaign = campaignService.getCampaign(id, admin.getCompanyID());

        if (campaign == null) {
            logger.warn("campaign view: could not load campaign with ID: {}", id);
            return "redirect:/mailing/archive/create.action";
        }

        archiveForm.setShortname(campaign.getShortname());
        archiveForm.setDescription(campaign.getDescription());

        FormUtils.syncNumberOfRows(webStorage, ARCHIVE_MAILINGS_OVERVIEW, archiveForm);
        model.addAttribute("mailingsList", campaignService.getCampaignMailings(id, archiveForm, admin));
        return "archive_view";
    }

    @GetMapping({"/create.action", "/0/view.action"})
    @RequiredPermission("campaign.change")
    public String create(@ModelAttribute("form") MailingArchiveForm archiveForm, HttpServletRequest req) {
        WorkflowUtils.updateForwardParameters(req);
        return "archive_view";
    }

    @RequestMapping("/save.action")
    @RequiredPermission("campaign.change")
    public Object save(Admin admin, @ModelAttribute MailingArchiveForm form, Popups popups, HttpServletRequest req, RedirectAttributes ra) {
        if (!validate(form, popups)) {
            return MESSAGES_VIEW;
        }

        Campaign oldArchive = campaignService.getCampaign(form.getId(), admin.getCompanyID());
        Campaign archive = new CampaignImpl(form.getId(), admin.getCompanyID(), form.getShortname(), form.getDescription());

        int campaignId = campaignService.save(archive);
        writeArchiveSaveLog(oldArchive, archive, admin);

        // when archive created from new workflow, then workflow id can equal to 0
        if (!WorkflowParametersHelper.isEmptyParams(req)) {
            return ResponseEntity.ok().body(Map.of("archiveId", campaignId, "archiveName", form.getShortname()));
        }

        popups.changesSaved();
        return "redirect:/mailing/archive/list.action?restoreSort=true";
    }

    @GetMapping(value = "/delete.action")
    @RequiredPermission("campaign.delete")
    public String confirmDelete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        validateDeletion(bulkIds);

        ServiceResult<List<Campaign>> result = campaignService.getAllowedForDeletion(bulkIds, admin);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(Campaign::getShortname).toList(),
                "campaign.Delete", "campaign.delete.question",
                "bulk.campaign.delete", "bulk.campaign.delete.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @RequiredPermission("campaign.delete")
    public String delete(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
        validateDeletion(bulkIds);
        ServiceResult<UserAction> result = campaignService.delete(bulkIds, admin);

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return "redirect:/mailing/archive/list.action?restoreSort=true";
    }

    private void validateDeletion(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }

    private boolean validate(MailingArchiveForm form, Popups popups) {
        if (form.getShortname().length() < 3) {
            popups.fieldError("shortname", "error.name.too.short");
        }

        return !popups.hasAlertPopups();
    }

    private void writeArchiveSaveLog(Campaign oldArchive, Campaign newArchive, Admin admin) {
        if (oldArchive == null) {
            writeUserActivityLog("create archive", getArchiveDescription(newArchive), admin);
        } else {
            writeArchiveChangeLog(oldArchive, newArchive, admin);
        }
    }

    private void writeArchiveChangeLog(Campaign oldArchive, Campaign newArchive, Admin admin) {
        StringBuilder builder = new StringBuilder();
        builder.append(addChangedFieldLog("shortname", newArchive.getShortname(), oldArchive.getShortname()))
                .append(addChangedFieldLog("description", newArchive.getDescription(), oldArchive.getDescription()));

        if (StringUtils.isNotBlank(builder.toString())) {
            builder.insert(0, ". ");
            builder.insert(0, getArchiveDescription(oldArchive));

            writeUserActivityLog("edit archive", builder.toString(), admin);
        }
    }

    private String getArchiveDescription(Campaign archive) {
        return format("%s (%d)", archive.getShortname(), archive.getId());
    }

    private void writeUserActivityLog(String action, String description, Admin admin) {
        userActivityLogService.writeUserActivityLog(admin, action, description, logger);
    }

}
