/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Set company_id in userlog_tbl to the matching admin_tbl company_id (restful=0) for the same username, only where company_id is 0
UPDATE userlog_tbl ul SET logtime = logtime, company_id = (SELECT COALESCE(MAX(company_id), 0) FROM admin_tbl a WHERE a.username = ul.username AND a.restful = 0) WHERE company_id = 0;
COMMIT;
