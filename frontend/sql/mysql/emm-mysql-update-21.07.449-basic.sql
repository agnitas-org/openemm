/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date)
	VALUES ('RS', 'RestfulService', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id)
	VALUES ('RestfulService', 0, (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'RS'));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('21.07.449', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
