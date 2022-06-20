/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.campaign.web;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.factory.impl.CampaignFactory;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.core.campaign.forms.CampaignSimpleActionForm;
import com.agnitas.emm.core.campaign.forms.CampaignForm;
import com.agnitas.emm.core.recipient.web.RecipientController;
import com.agnitas.web.mvc.Popups;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.impl.PaginatedListImpl;
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

import java.util.List;

import static org.agnitas.service.WebStorage.ARCHIVE_OVERVIEW;

@Controller
@RequestMapping("/campaign")
public class MailingCampaignController {

	/** The logger. */
    private static final Logger logger = LogManager.getLogger(RecipientController.class);

    protected final WebStorage webStorage;
    private CampaignDao campaignDao;
    protected MailingDao mailingDao;
    protected CampaignFactory campaignFactory;

    public MailingCampaignController(CampaignDao campaignDao, MailingDao mailingDao, CampaignFactory campaignFactory, WebStorage webStorage) {
        this.campaignDao = campaignDao;
        this.mailingDao = mailingDao;
        this.campaignFactory = campaignFactory;
        this.webStorage = webStorage;
    }

    @RequestMapping(value = "/list.action")
    public String list(ComAdmin admin, Model model, @ModelAttribute("form") PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, ARCHIVE_OVERVIEW, form);

        List<Campaign> campaignList = campaignDao.getCampaignList(admin.getCompanyID(), form.getSort(), form.getOrder().equals("desc") ? 2 : 1);
        PaginatedListImpl<Campaign> campaignPaginatedList = new PaginatedListImpl<>(campaignList, campaignList.size(), form.getNumberOfRows(), form.getPage(), form.getSort(), form.getOrder());

        model.addAttribute("campaignList", campaignPaginatedList);

        return "campaign_list_new";
    }

    @GetMapping("/{id:\\d+}/view.action")
    public String view(ComAdmin admin, Model model, @PathVariable(name = "id") int id, @ModelAttribute("form") CampaignForm campaignForm) {
        Campaign campaign = campaignDao.getCampaign(id, admin.getCompanyID());

        if (campaign == null) {
            logger.warn("campaign view: could not load campaign with ID: " + id);
            return "recirect:/campaign/create.action";
        }

        campaignForm.setId(id);
        campaignForm.setShortname(campaign.getShortname());
        campaignForm.setDescription(campaign.getDescription());
        model.addAttribute("mailinglist", campaignDao.getCampaignMailings(id, admin.getCompanyID()));

        return "campaign_view_new";
    }

    @GetMapping("/create.action")
    public String create(@ModelAttribute("form") CampaignForm campaignForm) {
        return "campaign_view_new";
    }

    @RequestMapping("/save.action")
    public String save(ComAdmin admin, @ModelAttribute("form") CampaignForm campaignForm, Popups popups, HttpSession session, RedirectAttributes redirectAttributes) {
        if (validate(campaignForm, popups)) {
            Campaign campaign = campaignDao.getCampaign(campaignForm.getId(), admin.getCompanyID());

            if (campaign == null) {
                campaign = campaignFactory.newCampaign();
                campaign.setCompanyID(admin.getCompanyID());
            }

            campaign.setShortname(campaignForm.getShortname());
            campaign.setDescription(campaignForm.getDescription());

            popups.success("default.changes_saved");

            int campaignId = campaignDao.save(campaign);

            int workflowId = getWorkflowId(session);
            if (workflowId != 0) {
                redirectAttributes.addAttribute("forwardParams", session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS).toString()
                        + ";elementValue=" + campaignForm.getId());

                return String.format("redirect:/workflow/%d/view.action", workflowId);
            }

            return String.format("redirect:/campaign/%d/view.action", campaignId);
        }

        return "messages";
    }

    @RequestMapping("/mailing/{mailingId:\\d+}/{campaignId:\\d+}/confirmDelete.action")
    public String confirmMailingDelete(ComAdmin admin, @PathVariable(name = "mailingId") int mailingID, @PathVariable(name = "campaignId") int campaignID,
                                       @ModelAttribute("form") CampaignSimpleActionForm form) {
        Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());

        form.setMailingId(mailingID);
        form.setCampaignId(campaignID);
        form.setIsCampaign(false);
        form.setShortname(mailing.getShortname());
        form.setTemplate(mailing.isIsTemplate());

        return "campaign_delete_ajax_new";
    }

    @RequestMapping("/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(ComAdmin admin, @PathVariable(name = "id") int id, @ModelAttribute("form") CampaignSimpleActionForm form) {
        Campaign campaign = campaignDao.getCampaign(id, admin.getCompanyID());

        form.setCampaignId(id);
        form.setShortname(campaign.getShortname());
        form.setIsCampaign(true);

        return "campaign_delete_ajax_new";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.DELETE, RequestMethod.POST})
    public String delete(ComAdmin admin, @ModelAttribute("form") CampaignSimpleActionForm form, Popups popups) {
        if (form.isIsCampaign()) {
            if (form.getCampaignId() > 0) {
                Campaign campaign = campaignDao.getCampaign(form.getCampaignId(), admin.getCompanyID());
                campaignDao.delete(campaign);

                popups.success("default.selection.deleted");
                return "redirect:/campaign/list.action";
            }
        } else {
            if (form.getMailingId() > 0) {
                mailingDao.deleteMailing(form.getMailingId(), admin.getCompanyID());

                popups.success("default.selection.deleted");

                return String.format("redirect:/campaign/%d/view.action", form.getCampaignId());
            }
        }

        return "redirect:/campaign/list.action";
    }

    private boolean validate(CampaignForm form, Popups popups) {
        if (form.getShortname().length() < 3) {
            popups.alert("error.name.too.short");
        }

        return !popups.hasAlertPopups();
    }

    private int getWorkflowId(HttpSession session) {
        Integer workflowId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
        return workflowId == null ? 0 : workflowId;
    }
}
