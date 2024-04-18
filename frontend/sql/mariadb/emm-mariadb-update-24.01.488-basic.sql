/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 23.07.097
-- EMM-9864
ALTER TABLE customer_field_tbl DROP COLUMN bk_default_value;

-- Execute changes after version is available on all systems: 23.07.097
-- EMM-9874
ALTER TABLE customer_field_tbl DROP COLUMN bk_field_group;

-- Execute changes after version is available on all systems: 23.07.097
-- GWUA-5514
ALTER TABLE workflow_reaction_tbl DROP COLUMN bk_is_legacy_mode;

-- Execute changes after version is available on all systems: 23.04.001. And delete java class ComWorkflowReportSendJobWorker
-- GWUA-5483
DROP TABLE bk_workflow_report_schedule_tbl;

-- Execute after WorkflowStateTransitionJobWorker is available on all systems: 24.04
-- Replace table undo_id_seq in MariaDB by using AutoIncrement
DROP TABLE bk_undo_id_seq;

-- Execute after 23.04.360 is deployed on all systems
-- EMM-9966
ALTER TABLE http_response_headers_tbl DROP COLUMN bk_resp_mimetype_pattern;

-- Execute changes after version is available on all systems: 23.07.182
-- EMM-10134
DELETE FROM company_permission_tbl WHERE permission_name = 'recipient.profileField.html.allowed';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'recipient.profileField.html.allowed';
DELETE FROM admin_permission_tbl WHERE permission_name = 'recipient.profileField.html.allowed';
DELETE FROM permission_tbl WHERE permission_name = 'recipient.profileField.html.allowed';

-- Execute changes after version is available on all systems: 23.07.321. And delete java class AssignMediapoolImagesDimensionsJobWorker and related code
-- EMMGUI-731
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE description = 'AssignMediapoolImagesDimensions');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE description = 'AssignMediapoolImagesDimensions');
DELETE FROM job_queue_tbl WHERE description = 'AssignMediapoolImagesDimensions';

-- PROJ-1707
-- Remote config key "litmus.GuidPrefix" after 23.07.523 is on production stage for a certain period of time
DELETE FROM config_tbl WHERE class = 'litmus' AND name = 'GuidPrefix';
DELETE FROM company_info_tbl WHERE cname = 'litmus.GuidPrefix';

-- EMM-7037
-- Drop obsolete Configvalues after EMM version 23.10.209 is available
DELETE FROM company_info_tbl WHERE cname = 'UseFixedTrackableLinksRead';
DELETE FROM company_info_tbl WHERE cname = 'UseFixedGetContentElementsForTemplate';
DELETE FROM company_info_tbl WHERE cname = 'UseFixedGetSaveContentMapBatchParams';

-- EMM-9163
-- Execute when 23.10.270 is deployed on all systems
DELETE FROM company_info_tbl WHERE cname = 'development.mailingStop.changeStatusOnCancelAndCopy';
DELETE FROM config_tbl WHERE class='development' AND name = 'mailingStop.changeStatusOnCancelAndCopy';

-- EMM-10306
-- Execute when 23.10.478 is deployed on all systems
DELETE FROM company_info_tbl WHERE cname = 'UseImprovedGenericImport';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.488', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
