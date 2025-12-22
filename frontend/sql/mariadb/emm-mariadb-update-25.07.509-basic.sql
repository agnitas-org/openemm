/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 25.01.569
-- GWUA-6352
-- Step 1: Rename columns editor_position_left to bk_editor_position_left and editor_position_top to bk_editor_position_top and is_inner to bk_is_inner (to be done)
--         Rename tables workflow_def_mailing_tbl to bk_workflow_def_mailing_tbl, workflow_reaction_cust_tbl to bk_workflow_reaction_cust_tbl,
--                       workflow_reaction_log_tbl to bk_workflow_reaction_log_tbl, workflow_reaction_mailing_tbl to bk_workflow_reaction_mailing_tbl
ALTER TABLE workflow_tbl RENAME COLUMN editor_position_left TO bk_editor_position_left;
ALTER TABLE workflow_tbl RENAME COLUMN editor_position_top TO bk_editor_position_top;
ALTER TABLE workflow_tbl RENAME COLUMN is_inner TO bk_is_inner;

ALTER TABLE workflow_def_mailing_tbl RENAME TO bk_workflow_def_mailing_tbl;
ALTER TABLE workflow_reaction_cust_tbl RENAME TO bk_workflow_reaction_cust_tbl;
ALTER TABLE workflow_reaction_log_tbl RENAME TO bk_workflow_reaction_log_tbl;
ALTER TABLE workflow_reaction_mailing_tbl RENAME TO bk_workflow_reaction_mailing_tbl;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.07.509', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
