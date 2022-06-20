/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE http_response_headers_tbl (
	header_name VARCHAR(100) NOT NULL,
	header_value TEXT NOT NULL,
	overwrite INTEGER(1) NOT NULL,
	app_types VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'settings saving a mapping on database-columns to file-columns in an export-profile';

INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) 
VALUE ('Content-Security-Policy', 'frame-ancestors ''self''', 1, 'emm');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.01.382', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
