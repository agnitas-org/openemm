/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 20.10.442
-- Remove unused permissions
DELETE FROM admin_permission_tbl WHERE permission_name = 'temp.beta';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'temp.beta';
DELETE FROM company_permission_tbl WHERE permission_name = 'temp.beta';
DELETE FROM permission_tbl WHERE permission_name='temp.beta';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('21.07.205', CURRENT_USER, CURRENT_TIMESTAMP);
