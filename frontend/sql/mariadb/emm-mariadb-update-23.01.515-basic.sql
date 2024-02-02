/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE undo_workflow_tbl (
    undo_id                    INT(11) NOT NULL AUTO_INCREMENT              COMMENT 'unique id',
    admin_id                   INT(11) NOT NULL                             COMMENT 'admin who made changes to workflow and caused undo entry creation',
	workflow_id                INT(11) NOT NULL                             COMMENT 'id of the workflow',
	company_id                 INT(11) UNSIGNED NOT NULL                    COMMENT 'tenant - ID (company_tbl)',
	workflow_schema            TEXT                                         COMMENT 'JSON representation of workflow structure',
    creation_date              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'entry creation time',
    PRIMARY KEY (undo_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci COMMENT 'stores workflow_schema to provide undo function (for example, to autorestore the workflow scheme after an expired pause)';

ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl(company_id);
ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl(admin_id);
ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$workflowid$fk FOREIGN KEY (workflow_id) REFERENCES workflow_tbl(workflow_id);

ALTER TABLE workflow_tbl ADD pause_undo_id INT(11) DEFAULT NULL COMMENT 'id of the undo_workflow_tbl(undo_id) entry that used to store data while pausing';

ALTER TABLE workflow_tbl ADD CONSTRAINT workflow$pause$fk FOREIGN KEY (pause_undo_id) REFERENCES undo_workflow_tbl(undo_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.515', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
