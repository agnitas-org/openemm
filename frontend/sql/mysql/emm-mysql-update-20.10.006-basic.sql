/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE ref_import_action_tbl (
	ref_importaction_id        INTEGER NOT NULL AUTO_INCREMENT COMMENT 'Unique ID of pre import action',
	company_id                 INTEGER NOT NULL COMMENT 'ClientID of the owner of this pre import action',
	name                       VARCHAR(128) COMMENT 'Displayname of this pre import action',
	type                       VARCHAR(32) NOT NULL COMMENT 'Type of this pre import action. Mostly SQL',
	action                     LONGTEXT NOT NULL COMMENT 'Content of this pre import action. Mostly SQL script',
	creation_date              TIMESTAMP NULL COMMENT 'Creation date of this pre import action',
	change_date                TIMESTAMP NULL COMMENT 'Change date of this pre import action',
	PRIMARY KEY (ref_importaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Pre import actions for reference tables';

ALTER TABLE csv_imexport_description_tbl ADD pre_import_action INTEGER COMMENT 'Unique ID of a pre import action. References ref_import_action_tbl';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.006', CURRENT_USER, CURRENT_TIMESTAMP);
