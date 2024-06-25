/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- GWUA-5483 (EMM-10270)
RENAME TABLE workflow_report_schedule_tbl TO bk_workflow_report_schedule_tbl;
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE description = 'WorkflowReportsSender');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE description = 'WorkflowReportsSender');
DELETE FROM job_queue_tbl WHERE description = 'WorkflowReportsSender';

RENAME TABLE undo_id_seq TO bk_undo_id_seq;

-- EMM-9966 (EMM-10271)
ALTER TABLE http_response_headers_tbl RENAME COLUMN resp_mimetype_pattern TO bk_resp_mimetype_pattern;

-- EMM-10046
DELETE FROM admin_permission_tbl WHERE permission_name = 'recipient.tracking.veto';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'recipient.tracking.veto';
DELETE FROM company_permission_tbl WHERE permission_name = 'recipient.tracking.veto';
DELETE FROM permission_tbl WHERE permission_name = 'recipient.tracking.veto';

-- EMMGUI-722
UPDATE recipients_report_tbl SET entity_type = 1 WHERE type = 'IMPORT_REPORT';
UPDATE recipients_report_tbl SET entity_type = 2 WHERE type = 'EXPORT_REPORT';
UPDATE recipients_report_tbl SET entity_execution = 2, entity_id = autoimport_id WHERE entity_type = 1 AND autoimport_id > 0;
UPDATE recipients_report_tbl SET entity_execution = 1 WHERE entity_type = 1 AND autoimport_id <= 0;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.10.171', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
