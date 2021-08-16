/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE export_predef_tbl ADD timestamp_includecurrent INTEGER COMMENT 'Include the current day in export data by timestamp';
ALTER TABLE export_predef_tbl ADD creation_date_includecurrent INTEGER COMMENT 'Include the current day in export data by creation date';
ALTER TABLE export_predef_tbl ADD ml_bind_includecurrent INTEGER COMMENT 'Include the current day in export datamailinglist binding';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.254', CURRENT_USER, CURRENT_TIMESTAMP);
