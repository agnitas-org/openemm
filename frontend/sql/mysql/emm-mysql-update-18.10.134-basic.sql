/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO admin_permission_tbl (admin_id, security_token) (SELECT admin_id, 'recipient.show' FROM admin_permission_tbl a WHERE security_token = 'recipient.list' AND NOT EXISTS(SELECT 1 FROM admin_permission_tbl b WHERE security_token = 'recipient.show' AND a.admin_id = b.admin_id));
INSERT INTO admin_group_permission_tbl (admin_group_id, security_token) (SELECT admin_group_id, 'recipient.show' FROM admin_group_permission_tbl a WHERE security_token = 'recipient.list' AND NOT EXISTS(SELECT 1 FROM admin_group_permission_tbl b WHERE security_token = 'recipient.show' AND a.admin_group_id = b.admin_group_id));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.134', CURRENT_USER, CURRENT_TIMESTAMP);
