/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.mailing.autooptimization.service.OptimizationJobWorker'
WHERE runclass = 'com.agnitas.mailing.autooptimization.service.ComOptimizationJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.emm.core.workflow.service.WorkflowReminderServiceJobWorker'
WHERE runclass = 'com.agnitas.emm.core.workflow.service.ComWorkflowReminderServiceJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.emm.core.birtreport.service.BirtReportJobWorker'
WHERE runclass = 'com.agnitas.emm.core.birtreport.service.ComBirtReportJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.emm.core.calendar.service.CalendarCommentMailingServiceJobWorker'
WHERE runclass = 'com.agnitas.emm.core.calendar.service.ComCalendarCommentMailingServiceJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowReactionJobWorker'
WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReactionJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.334', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
