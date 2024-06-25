/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO admin_permission_tbl (admin_id, permission_name)
	(SELECT admin_id, 'company.settings.deeptracking' FROM admin_tbl a
		WHERE EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE permission_name = 'company.change' AND a.admin_id = b.admin_id)
		AND NOT EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE permission_name = 'company.settings.deeptracking' AND a.admin_id = b.admin_id));
INSERT INTO admin_permission_tbl (admin_id, permission_name)
	(SELECT admin_id, 'company.settings.deliveryspeed' FROM admin_tbl a
		WHERE EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE permission_name = 'company.change' AND a.admin_id = b.admin_id)
		AND NOT EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE permission_name = 'company.settings.deliveryspeed' AND a.admin_id = b.admin_id));

INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name)
	(SELECT admin_group_id, 'company.settings.deeptracking' FROM admin_group_tbl a
		WHERE EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE permission_name = 'company.change' AND a.admin_group_id = b.admin_group_id)
		AND NOT EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE permission_name = 'company.settings.deeptracking' AND a.admin_group_id = b.admin_group_id));
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name)
	(SELECT admin_group_id, 'company.settings.deliveryspeed' FROM admin_group_tbl a
		WHERE EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE permission_name = 'company.change' AND a.admin_group_id = b.admin_group_id)
		AND NOT EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE permission_name = 'company.settings.deliveryspeed' AND a.admin_group_id = b.admin_group_id));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.163', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
