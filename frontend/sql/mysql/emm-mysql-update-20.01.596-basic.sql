/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE triggerdlg_fields_tbl (
	company_id                 INTEGER UNSIGNED NOT NULL COMMENT 'Client id',
	mailing_id                 INTEGER UNSIGNED NOT NULL COMMENT 'Mailing id',
	field_name                 VARCHAR(100) NOT NULL COMMENT 'Field name to use for postal mailings'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Field names to use for postal mailings';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.01.596', CURRENT_USER, CURRENT_TIMESTAMP);
