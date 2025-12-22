/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.core.auto_import.bean.AutoImportLight;
import com.agnitas.emm.core.auto_import.service.AutoImportService;
import com.agnitas.emm.core.autoexport.beans.AutoExport;
import com.agnitas.emm.core.autoexport.service.AutoExportService;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/workflow/ajax")
public class WorkflowAjaxController implements XssCheckAware {

    private final WorkflowService workflowService;
    private final AutoImportService autoImportService;
    private final AutoExportService autoExportService;
    private final CampaignDao campaignDao;
    private final MailinglistApprovalService mailinglistApprovalService;

    public WorkflowAjaxController(WorkflowService workflowService,
                                  @Autowired(required = false) AutoImportService autoImportService,
                                  @Autowired(required = false) AutoExportService autoExportService,
                                  CampaignDao campaignDao,
                                  MailinglistApprovalService mailinglistApprovalService) {
        this.workflowService = workflowService;
        this.autoImportService = autoImportService;
        this.autoExportService = autoExportService;
        this.campaignDao = campaignDao;
        this.mailinglistApprovalService = mailinglistApprovalService;
    }

    @GetMapping("/getMailingNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getMailingNames(Admin admin) {
        return workflowService.getAllMailings(admin).stream()
                .collect(Collectors.toMap(LightweightMailing::getMailingID, LightweightMailing::getShortname));
    }

    @GetMapping("/getAutoExportNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getAutoExportNames(Admin admin) {
        if (autoExportService == null) {
            return new HashMap<>();
        }

        return autoExportService.getAutoExports(admin).stream()
                .collect(Collectors.toMap(AutoExport::getAutoExportId, AutoExport::getShortname));
    }

    @GetMapping("/getAutoImportNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getAutoImportNames(Admin admin) {
        if (autoImportService == null) {
            return new HashMap<>();
        }

        return autoImportService.listAutoImports(admin.getCompanyID()).stream()
                .collect(Collectors.toMap(AutoImportLight::getAutoImportId, AutoImportLight::getShortname));
    }

    @GetMapping("/getMailinglistNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getMailinglistNames(Admin admin) {
        return mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin).stream()
                .collect(Collectors.toMap(Mailinglist::getId, Mailinglist::getShortname));
    }

    @GetMapping("/getArchiveNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getArchiveNames(Admin admin) {
        return campaignDao.getCampaignList(admin.getCompanyID(), "lower(shortname)", 1).stream()
                .collect(Collectors.toMap(Campaign::getId, Campaign::getShortname));
    }

    @GetMapping("/getTargetNames.action")
    @RequiredPermission("workflow.show")
    public @ResponseBody
    Map<Integer, String> getTargetNames(Admin admin) {
        return workflowService.getAllTargets(admin.getCompanyID()).stream()
                .collect(Collectors.toMap(TargetLight::getId, TargetLight::getTargetName));
    }

}
