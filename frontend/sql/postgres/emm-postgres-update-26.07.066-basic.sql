/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date)
VALUES ('restful.api.legacy', 'System', NULL, 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO admin_permission_tbl (permission_name, admin_id)
SELECT 'restful.api.legacy', a.admin_id
FROM admin_tbl a
WHERE a.restful = 1
  AND NOT EXISTS (
    SELECT 1
    FROM admin_permission_tbl ap
    WHERE ap.admin_id = a.admin_id
      AND ap.permission_name = 'restful.api.legacy'
);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.066', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
