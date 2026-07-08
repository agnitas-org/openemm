/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE workflow_tbl DROP COLUMN bk_editor_position_left;
ALTER TABLE workflow_tbl DROP COLUMN bk_editor_position_top;
ALTER TABLE workflow_tbl DROP COLUMN bk_is_inner;
DROP TABLE bk_workflow_def_mailing_tbl;
DROP TABLE bk_workflow_reaction_cust_tbl;
DROP TABLE bk_workflow_reaction_log_tbl;
DROP TABLE bk_workflow_reaction_mailing_tbl;

ALTER TABLE recipients_report_tbl RENAME COLUMN type TO bk_type;

ALTER TABLE job_queue_tbl RENAME COLUMN runClass TO bk_runClass;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.01.313', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
