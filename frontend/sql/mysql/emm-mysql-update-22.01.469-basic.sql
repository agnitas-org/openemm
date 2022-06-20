/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE mailing_import_lock_tbl (
	maildrop_status_id         INTEGER NOT NULL COMMENT 'Reference id for maildrop_status_tbl',
	auto_import_id             INTEGER NOT NULL COMMENT 'Reference id for auto_import_tbl',
	status_ok                  INTEGER COMMENT 'Status of referenced auto_import_id in auto_import_tbl. (0 = Error, 1 = OK)',
	change_date                TIMESTAMP NULL COMMENT 'Date of last status change'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'List of AutoImports that block the delivery of mailings';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.01.469', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
