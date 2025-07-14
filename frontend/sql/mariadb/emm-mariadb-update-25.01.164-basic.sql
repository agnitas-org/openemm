/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE actop_get_archive_list_tbl ADD COLUMN limit_type INT(1) COMMENT 'Defines type of limit for action. 0 - Days, 1 - Mailings';
ALTER TABLE actop_get_archive_list_tbl ADD COLUMN limit_value INT COMMENT 'Stores value of limit';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('25.01.164', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
