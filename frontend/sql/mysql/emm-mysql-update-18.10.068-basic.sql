/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE mimetype_whitelist_tbl (
	mimetype               VARCHAR(100) NOT NULL COMMENT 'Mimetype pattern (can include asterisk)',
	description            VARCHAR(100) COMMENT 'Optional description',
	creation_date          TIMESTAMP NOT NULL COMMENT 'Timestamp of creating record'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Whitelist for Mimetypes supported for uploads';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.068', CURRENT_USER, CURRENT_TIMESTAMP);
