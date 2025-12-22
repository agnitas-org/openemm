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
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.DeviceService;
import com.agnitas.emm.core.reminder.service.ReminderService;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.emm.core.commons.util.ConfigService;

public abstract class ServiceLookupFactory {
	abstract public BirtReportService getBeanBirtReportService();
	abstract public BirtStatisticsService getBeanBirtStatisticsService();
	abstract public ExtensibleUIDService getBeanExtensibleUIDService();
	abstract public DeviceService getBeanDeviceService();
	abstract public ClientService getBeanClientService();
	abstract public ReminderService getBeanCalendarReminderService();
	abstract public OptimizationService getBeanOptimizationService();
	abstract public ReminderService getBeanWorkflowStartStopReminderService();
	abstract public WorkflowService getBeanWorkflowService();
	abstract public WorkflowActivationService getBeanWorkflowActivationService();
	abstract public UserActivityLogService getBeanUserActivityLogService();
	abstract public JavaMailService getBeanJavaMailService();
	abstract public ConfigService getBeanConfigService();
	abstract public CompanyService getBeanCompanyService();
	abstract public AdminService getBeanAdminService();
	abstract public MailingService getBeanMailingService();
	abstract public MaildropService getBeanMaildropService();
    abstract public ServerStatusService getBeanServerStatusService();
}
