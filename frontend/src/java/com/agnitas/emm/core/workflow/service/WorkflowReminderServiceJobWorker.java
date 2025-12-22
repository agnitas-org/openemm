/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import com.agnitas.emm.core.reminder.service.ReminderService;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;

/**
 * This worker is responsible for processing scheduled workflow reminders configured in start and stop icons.
 * <p>
 * Its primary role is to monitor and trigger these reminders at the
 * configured time, ensuring that EMM users receive email notifications about workflow events.
 * <p>
 * Example Insert in DB:
 * INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
 * VALUES ('WorkflowReminderService', CURRENT_TIMESTAMP, NULL, 0, 'OK', 1, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.ComWorkflowReminderServiceJobWorker', 0, 5);
 */
@JobWorker("WorkflowReminderService")
public class WorkflowReminderServiceJobWorker extends JobWorkerBase {

    @Override
    public String runJob() {
        ReminderService reminderService = serviceLookupFactory.getBeanWorkflowStartStopReminderService();
        reminderService.send(getCompaniesConstraints());
		
		return null;
    }
}
