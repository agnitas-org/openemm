/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE export_column_mapping_tbl (
	id                         INT(11) NOT NULL AUTO_INCREMENT COMMENT 'unique ID',
	export_predef_id           INT(11) NOT NULL COMMENT 'references export_predef_tbl',
	db_column                  VARCHAR(255) COMMENT 'matching column in database',
	file_column                VARCHAR(255) COMMENT 'matching column in file',
	default_value              VARCHAR(255) DEFAULT '' COMMENT 'default (for this column and this export)',
	encrypted                  INT(11) DEFAULT 0 COMMENT '1 = yes',
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'settings saving a mapping on database-columns to file-columns in an export-profile';
ALTER TABLE export_column_mapping_tbl ADD CONSTRAINT exportcolmap$exppredef$fk FOREIGN KEY (export_predef_id) REFERENCES export_predef_tbl (export_predef_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$export_col_map_tbl FOREIGN KEY (signed_id) REFERENCES export_column_mapping_tbl (id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('21.07.177', CURRENT_USER, CURRENT_TIMESTAMP);
