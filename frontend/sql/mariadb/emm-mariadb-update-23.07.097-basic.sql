/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 23.01.263
-- EMM-9864
ALTER TABLE customer_field_tbl RENAME COLUMN default_value TO bk_default_value;

-- Execute changes after version is available on all systems: 23.01.374
-- EMM-9874
ALTER TABLE customer_field_tbl RENAME COLUMN field_group TO bk_field_group;

-- Execute changes after version is available on all systems: 23.01.402
-- GWUA-5514
ALTER TABLE workflow_reaction_tbl RENAME COLUMN is_legacy_mode TO bk_is_legacy_mode;

-- Execute changes after version is available on all systems: 23.04.001. And delete java class ComWorkflowReportSendJobWorker
-- GWUA-5483
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker');
DELETE from job_queue_tbl where runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker';

-- Execute changes after version is available on all systems: 23.04.069. And delete java class DeleteGridTemplateWorkCopiesJobWorker
-- GWUA-5577
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.grid.grid.service.DeleteGridTemplateWorkCopiesJobWorker');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.grid.grid.service.DeleteGridTemplateWorkCopiesJobWorker');
DELETE from job_queue_tbl where runclass = 'com.agnitas.emm.grid.grid.service.DeleteGridTemplateWorkCopiesJobWorker';

-- Execute after WorkflowStateTransitionJobWorker is available on all systems
-- GWUA-4560
UPDATE job_queue_tbl SET description = 'WorkflowStateHandler', runclass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker'
	WHERE description = 'WorkflowStopHandler' AND runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowStopJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.07.097', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
