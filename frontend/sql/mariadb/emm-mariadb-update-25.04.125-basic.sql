/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE job_queue_tbl SET criticality = 5, startAfterError = 1 WHERE description = 'AutoOptimization';
UPDATE job_queue_tbl SET criticality = 5, startAfterError = 1 WHERE description = 'WorkflowReactionHandler';
UPDATE job_queue_tbl SET criticality = 5, startAfterError = 1 WHERE description = 'WorkflowStateHandler';
UPDATE job_queue_tbl SET criticality = 5, startAfterError = 1 WHERE description = 'WebhookMessageDelivery';
UPDATE job_queue_tbl SET criticality = 5, startAfterError = 1 WHERE description = 'WebhookBackendDataMessageGenerator';

UPDATE job_queue_tbl SET criticality = 4, startAfterError = 1 WHERE description = 'BirtReports';
UPDATE job_queue_tbl SET criticality = 4, startAfterError = 1 WHERE description = 'MigrationJobWorker';
UPDATE job_queue_tbl SET criticality = 4, startAfterError = 1 WHERE description = 'WorkflowReminderService';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.04.125', CURRENT_USER, CURRENT_TIMESTAMP);
COMMIT;
