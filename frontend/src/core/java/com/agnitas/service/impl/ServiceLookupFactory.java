/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.LicenseParser;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.DeviceService;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.reminder.service.ReminderService;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.systemmessages.service.SystemMailMessageService;
import com.agnitas.emm.core.trackablelinks.service.ClickRankingService;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.UserActivityLogService;

public abstract class ServiceLookupFactory {

    public abstract BirtReportService getBeanBirtReportService();

    public abstract BirtStatisticsService getBeanBirtStatisticsService();

    public abstract ExtensibleUIDService getBeanExtensibleUIDService();

    public abstract DeviceService getBeanDeviceService();

    public abstract ClientService getBeanClientService();

    public abstract ReminderService getBeanCalendarReminderService();

    public abstract OptimizationService getBeanOptimizationService();

    public abstract ReminderService getBeanWorkflowStartStopReminderService();

    public abstract WorkflowService getBeanWorkflowService();

    public abstract WorkflowActivationService getBeanWorkflowActivationService();

    public abstract UserActivityLogService getBeanUserActivityLogService();

    public abstract JavaMailService getBeanJavaMailService();

    public abstract ConfigService getBeanConfigService();

    public abstract LicenseParser getBeanLicenseParser();

    public abstract CompanyService getBeanCompanyService();

    public abstract RecipientFieldService getRecipientFieldService();

    public abstract RecipientService getRecipientService();

    public abstract AdminService getBeanAdminService();

    public abstract MailingService getBeanMailingService();

    public abstract MaildropService getBeanMaildropService();

    public abstract ServerStatusService getBeanServerStatusService();

    public abstract SystemMailMessageService getSystemMailMessageService();

    public abstract ClickRankingService getClickRankingService();
}
