/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.archive.web;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.CampaignImpl;
import com.agnitas.emm.core.archive.forms.MailingArchiveForm;
import com.agnitas.emm.core.archive.forms.MailingArchiveSimpleActionForm;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.dao.MailingDao;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.agnitas.service.WebStorage.ARCHIVE_OVERVIEW;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

@Controller
@RequestMapping("/mailing/archive")
@PermissionMapping("mailing.archive")
public class MailingArchiveController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingArchiveController.class);

    private final WebStorage webStorage;
    private final MailingDao mailingDao;
    private final CampaignService campaignService;

    public MailingArchiveController(MailingDao mailingDao, WebStorage webStorage, CampaignService campaignService) {
        this.mailingDao = mailingDao;
        this.webStorage = webStorage;
        this.campaignService = campaignService;
    }

    @RequestMapping(value = "/list.action")
    public String list(Admin admin, Model model, @ModelAttribute("form") PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, ARCHIVE_OVERVIEW, form);

        model.addAttribute("campaignList", campaignService.getPaginatedList(admin, form));

        return "archive_list";
    }

    @GetMapping("/{id:\\d+}/view.action")
    public String view(Admin admin, Model model, @PathVariable(name = "id") int id, @ModelAttribute("form") MailingArchiveForm archiveForm) {
        Campaign campaign = campaignService.getCampaign(id, admin.getCompanyID());

        if (campaign == null) {
            logger.warn("campaign view: could not load campaign with ID: {}", id);
            return "redirect:/mailing/archive/create.action";
        }

        archiveForm.setId(id);
        archiveForm.setShortname(campaign.getShortname());
        archiveForm.setDescription(campaign.getDescription());
        model.addAttribute("mailingsList", campaignService.getCampaignMailings(id, admin));

        return "archive_view";
    }

    @GetMapping("/create.action")
    public String create(@ModelAttribute("form") MailingArchiveForm archiveForm, HttpServletRequest req) {
        WorkflowUtils.updateForwardParameters(req);
        return "archive_view";
    }

    @RequestMapping("/save.action")
    public String save(Admin admin, @ModelAttribute("form") MailingArchiveForm archiveForm, Popups popups, HttpSession session, RedirectAttributes redirectAttributes) {
        if (validate(archiveForm, popups)) {
            Campaign campaign = campaignService.getCampaign(archiveForm.getId(), admin.getCompanyID());

            if (campaign == null) {
                campaign = new CampaignImpl();
                campaign.setCompanyID(admin.getCompanyID());
            }

            campaign.setShortname(archiveForm.getShortname());
            campaign.setDescription(archiveForm.getDescription());

            int campaignId = campaignService.save(campaign);

            popups.success("default.changes_saved");

            int workflowId = WorkflowParametersHelper.getWorkflowIdFromSession(session);
            if (workflowId != 0) {
                WorkflowParametersHelper.addEditedElemRedirectAttrs(redirectAttributes, session, campaignId);
                return String.format("redirect:/workflow/%d/view.action", workflowId);
            }

            return String.format("redirect:/mailing/archive/%d/view.action", campaignId);
        }

        return "messages";
    }

    @RequestMapping("/{campaignId:\\d+}/mailing/{mailingId:\\d+}/confirmDelete.action")
    public String confirmMailingDelete(Admin admin, @PathVariable(name = "mailingId") int mailingID, @PathVariable(name = "campaignId") int campaignID,
                                       @ModelAttribute("form") MailingArchiveSimpleActionForm form) {
        Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());

        form.setMailingId(mailingID);
        form.setCampaignId(campaignID);
        form.setIsCampaign(false);
        form.setShortname(mailing.getShortname());
        form.setTemplate(mailing.isIsTemplate());

        return "archive_delete_ajax";
    }

    @RequestMapping("/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(Admin admin, @PathVariable(name = "id") int id, @ModelAttribute("form") MailingArchiveSimpleActionForm form, Popups popups) {
        if (cantBeDeleted(admin, id, popups)) {
            return MESSAGES_VIEW;
        }

        Campaign campaign = campaignService.getCampaign(id, admin.getCompanyID());

        form.setCampaignId(id);
        form.setShortname(campaign.getShortname());
        form.setIsCampaign(true);

        return "archive_delete_ajax";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.DELETE, RequestMethod.POST})
    public String delete(Admin admin, @ModelAttribute MailingArchiveSimpleActionForm form, Popups popups) {
        if (form.isIsCampaign()) {
            if (form.getCampaignId() > 0) {
                Campaign campaign = campaignService.getCampaign(form.getCampaignId(), admin.getCompanyID());
                campaignService.delete(campaign);

                popups.success("default.selection.deleted");
            }
        } else {
            if (form.getMailingId() > 0) {
                mailingDao.deleteMailing(form.getMailingId(), admin.getCompanyID());

                popups.success("default.selection.deleted");

                return String.format("redirect:/mailing/archive/%d/view.action", form.getCampaignId());
            }
        }

        return "redirect:/mailing/archive/list.action";
    }

    private boolean cantBeDeleted(Admin admin, int id, Popups popups) {
        if (campaignService.isContainMailings(id, admin)) {
            popups.alert("GWUA.campaign.Delete.mailing.assigned");
        }
        if (campaignService.isDefinedForAutoOptimization(id, admin)) {
            popups.alert("GWUA.campaign.Delete.autoopt.defined");
        }
        return popups.hasAlertPopups();
    }

    private boolean validate(MailingArchiveForm form, Popups popups) {
        if (form.getShortname().length() < 3) {
            popups.alert("error.name.too.short");
        }

        return !popups.hasAlertPopups();
    }
}
