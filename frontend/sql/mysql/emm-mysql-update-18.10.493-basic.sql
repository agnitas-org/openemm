/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO admin_permission_tbl (admin_id, security_token)
SELECT admin_id, 'forms.show' FROM admin_permission_tbl ap WHERE security_token = 'forms.view'
		AND NOT EXISTS(SELECT 1 FROM admin_permission_tbl WHERE admin_id = ap.admin_id AND security_token = 'forms.show');

INSERT INTO admin_group_permission_tbl (admin_group_id, security_token)
SELECT admin_group_id, 'forms.show' FROM admin_group_permission_tbl agp WHERE security_token = 'forms.view'
		AND NOT EXISTS(SELECT 1 FROM admin_group_permission_tbl WHERE admin_group_id = agp.admin_group_id AND security_token = 'forms.show');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.493', CURRENT_USER, CURRENT_TIMESTAMP);
