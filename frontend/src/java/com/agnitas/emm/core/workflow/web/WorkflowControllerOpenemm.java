package com.agnitas.emm.core.workflow.web;

import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowDataParser;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.core.workflow.service.WorkflowStatisticsService;
import com.agnitas.emm.core.workflow.service.WorkflowValidationService;
import com.agnitas.service.PdfService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/workflow")
@PermissionMapping("workflow")
public class WorkflowControllerOpenemm extends WorkflowController {

    public WorkflowControllerOpenemm(WorkflowService workflowService, WorkflowValidationService validationService,
                                     WorkflowActivationService workflowActivationService, WorkflowStatisticsService workflowStatisticsService,
                                     @Autowired(required = false) AutoImportService autoImportService, @Autowired(required = false) AutoExportService autoExportService, WorkflowDataParser workflowDataParser,
                                     CampaignDao campaignDao, MailingDeliveryStatService deliveryStatService, MailingComponentDao componentDao,
                                     PdfService pdfService, CompanyDao companyDao, ConfigService configService,
                                     MailinglistApprovalService mailinglistApprovalService, UserActivityLogService userActivityLogService,
                                     ConversionService conversionService, MailingService mailingService, AdminService adminService,
                                     TargetService targetService) {

        super(workflowService, validationService, workflowActivationService, workflowStatisticsService, autoImportService, autoExportService,
                workflowDataParser, campaignDao, deliveryStatService, componentDao, pdfService, companyDao, configService, mailinglistApprovalService,
                userActivityLogService, conversionService, mailingService, adminService, targetService);
    }
}
