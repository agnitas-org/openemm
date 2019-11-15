/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date)
SELECT c.company_id, 'targetgroups.migrateOnStartup', 'true', 'Migration scheduled', current_timestamp
FROM company_tbl c
WHERE NOT EXISTS 
        (
            SELECT 1 
            FROM company_info_tbl  ci
            WHERE c.company_id = ci.company_id 
            AND cname = 'targetgroups.migrateOnStartup'
        )
ORDER BY c.company_id
;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('19.10.001', CURRENT_USER, CURRENT_TIMESTAMP);
