/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE recipients_report_tbl ADD COLUMN entity_type INT(10) UNSIGNED DEFAULT 0 COMMENT 'Type of entity. 0 - unknown, 1 - import, 2 - export';
ALTER TABLE recipients_report_tbl ADD COLUMN entity_execution INT(10) UNSIGNED DEFAULT 0 COMMENT 'Type of execution. 0 - unknown, 1 - manual, 2 - automatic';
ALTER TABLE recipients_report_tbl ADD COLUMN entity_data INT(10) UNSIGNED DEFAULT 0 COMMENT 'Determines whence or where the data is going. 0 - unknown, 1 - profile, 2 - reference table';
ALTER TABLE recipients_report_tbl ADD COLUMN entity_id INT(10) UNSIGNED COMMENT 'ID of entity according to type.';

UPDATE recipients_report_tbl SET entity_type = 1 WHERE type = 'IMPORT_REPORT';
UPDATE recipients_report_tbl SET entity_type = 2 WHERE type = 'EXPORT_REPORT';
UPDATE recipients_report_tbl SET entity_execution = 2, entity_id = autoimport_id WHERE entity_type = 1 AND autoimport_id > 0;
UPDATE recipients_report_tbl SET entity_execution = 1 WHERE entity_type = 1 AND autoimport_id <= 0;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.369', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
