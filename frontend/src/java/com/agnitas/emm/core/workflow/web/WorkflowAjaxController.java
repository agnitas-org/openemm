/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.CampaignDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/workflow/ajax")
@PermissionMapping("workflow")
public class WorkflowAjaxController implements XssCheckAware {

    private final ComWorkflowService workflowService;
    private final AutoImportService autoImportService;
    private final AutoExportService autoExportService;
    private final CampaignDao campaignDao;
    private final MailinglistApprovalService mailinglistApprovalService;

    public WorkflowAjaxController(ComWorkflowService workflowService,
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
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getMailingNames(Admin admin) {
        return workflowService.getAllMailings(admin).stream()
                .collect(Collectors.toMap(LightweightMailing::getMailingID, LightweightMailing::getShortname));
    }

    @GetMapping("/getAutoExportNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getAutoExportNames(Admin admin) {
        if (autoExportService == null) {
            return new HashMap<>();
        }

        return autoExportService.getAutoExportsOverview(admin).stream()
                .collect(Collectors.toMap(AutoExport::getAutoExportId, AutoExport::getShortname));
    }

    @GetMapping("/getAutoImportNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getAutoImportNames(Admin admin) {
        if (autoImportService == null) {
            return new HashMap<>();
        }

        return autoImportService.listAutoImports(admin.getCompanyID()).stream()
                .collect(Collectors.toMap(AutoImportLight::getAutoImportId, AutoImportLight::getShortname));
    }

    @GetMapping("/getMailinglistNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getMailinglistNames(Admin admin) {
        return mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin).stream()
                .collect(Collectors.toMap(Mailinglist::getId, Mailinglist::getShortname));
    }

    @GetMapping("/getArchiveNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getArchiveNames(Admin admin) {
        return campaignDao.getCampaignList(admin.getCompanyID(), "lower(shortname)", 1).stream()
                .collect(Collectors.toMap(Campaign::getId, Campaign::getShortname));
    }

    @GetMapping("/getTargetNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getTargetNames(Admin admin) {
        return workflowService.getAllTargets(admin.getCompanyID()).stream()
                .collect(Collectors.toMap(TargetLight::getId, TargetLight::getTargetName));
    }

    @GetMapping("/getReportNames.action")
    @PermissionMapping("view")
    public @ResponseBody
    Map<Integer, String> getReportNames(Admin admin) {
        return workflowService.getAllReports(admin.getCompanyID());
    }
}
