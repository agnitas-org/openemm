/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.company.service.ComCompanyService;
import com.agnitas.emm.core.mailing.service.MaildropStatusService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.emm.core.reminder.service.ComReminderService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;

public abstract class ServiceLookupFactory {
	abstract public ComBirtReportService getBeanBirtReportService();
	abstract public BirtStatisticsService getBeanBirtStatisticsService();
	abstract public ExtensibleUIDService getBeanExtensibleUIDService();
	abstract public ComDeviceService getBeanDeviceService();
	abstract public ClientService getBeanClientService();
	abstract public ComReminderService getBeanCalendarReminderService();
	abstract public ComOptimizationService getBeanOptimizationService();
	abstract public ComReminderService getBeanWorkflowStartStopReminderService();
	abstract public ComWorkflowService getBeanWorkflowService();
	abstract public JavaMailService getBeanJavaMailService();
	abstract public ConfigService getBeanConfigService();
	abstract public ComTargetService getBeanTargetService();
	abstract public SendActionbasedMailingService getBeanSendActionbasedMailingService();
	abstract public ComCompanyService getBeanCompanyService();
	abstract public AdminService getBeanAdminService();
	abstract public MailingService getBeanMailingService();
	abstract public MaildropStatusService getBeanMaildropStatusService();
}
