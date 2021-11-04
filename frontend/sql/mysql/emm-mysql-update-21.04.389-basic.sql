/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('V', 'Velocity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id) VALUES ('Velocity', 0, (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'V'));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.389', CURRENT_USER, CURRENT_TIMESTAMP);
