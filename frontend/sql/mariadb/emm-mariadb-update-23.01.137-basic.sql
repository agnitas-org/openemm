/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE css_tbl (
	id                         INTEGER UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'unique key',
	company_id                 INTEGER UNSIGNED NOT NULL COMMENT 'company id',
	name                       VARCHAR(255) COMMENT 'name of scss parameter',
	value                      VARCHAR(255) COMMENT 'value of scss parameter',
	description                VARCHAR(1000) COMMENT 'description of scss parameter',
	PRIMARY KEY(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci COMMENT 'stores scss parameters for generation of main css';

INSERT INTO css_tbl (name, company_id, value, description) VALUES ('c-blue-27', 0, '#0071b9', 'Primary Color, Default: #0071b9');
INSERT INTO css_tbl (name, company_id, value, description) VALUES ('c-blue-56', 0, '#004470', 'Secondary Color, Default: #004470');
INSERT INTO css_tbl (name, company_id, value, description) VALUES ('c-blue-22', 0, '#338dc7', 'Tertiary Color, Default: #338dc7');
						
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.137', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
