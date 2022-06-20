package com.agnitas.emm.core.workflow.web;

import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.service.ComWorkflowActivationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowDataParser;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.ComWorkflowStatisticsService;
import com.agnitas.emm.core.workflow.service.ComWorkflowValidationService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/workflow")
@PermissionMapping("workflow")
public class WorkflowControllerOpenemm extends WorkflowController {
    
    public WorkflowControllerOpenemm(ComWorkflowService workflowService, ComWorkflowValidationService validationService,
                              ComWorkflowActivationService workflowActivationService, ComWorkflowStatisticsService workflowStatisticsService,
                              @Autowired(required = false) AutoImportService autoImportService, @Autowired(required = false) AutoExportService autoExportService, ComWorkflowDataParser workflowDataParser,
                              CampaignDao campaignDao, ComMailingDeliveryStatService deliveryStatService, ComMailingComponentDao componentDao,
                              GenerationPDFService generationPDFService, ComCompanyDao companyDao, ConfigService configService,
                              WebStorage webStorage, MailinglistApprovalService mailinglistApprovalService, UserActivityLogService userActivityLogService,
                              ConversionService conversionService, MailingService mailingService, ComOptimizationService optimizationService, AdminService adminService,
                              ComTargetService targetService) {
        super(workflowService, validationService, workflowActivationService, workflowStatisticsService, autoImportService, autoExportService, workflowDataParser, campaignDao, deliveryStatService, componentDao, generationPDFService, companyDao, configService, webStorage, mailinglistApprovalService, userActivityLogService, conversionService, mailingService, optimizationService, adminService, targetService);
    }
}
