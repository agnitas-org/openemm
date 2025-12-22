/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE userlog_tbl
    ADD COLUMN company_id INTEGER DEFAULT 0 COMMENT 'Stores the company identifier (company_tbl)';

UPDATE userlog_tbl ul
SET logtime    = logtime,
    company_id = (SELECT COALESCE(MAX(company_id), 0) FROM admin_tbl a WHERE a.username = ul.username AND a.restful = 0)
WHERE company_id = 0;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.04.565', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
