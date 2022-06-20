/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 21.07.337
-- Remove unused permission (EMM-8367)
DELETE FROM admin_permission_tbl WHERE permission_name = 'company.force.sending';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'company.force.sending';
DELETE FROM company_permission_tbl WHERE permission_name = 'company.force.sending';
DELETE FROM permission_tbl WHERE permission_name = 'company.force.sending';

-- Execute changes after version is available on all systems: 21.07.416
-- Remove restful permission (EMM-8796)
DELETE FROM admin_permission_tbl WHERE permission_name = 'restful.allowed';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'restful.allowed';
DELETE FROM company_permission_tbl WHERE permission_name = 'restful.allowed';
DELETE FROM permission_tbl WHERE permission_name = 'restful.allowed';

-- Execute changes after version is available on all systems: 21.07.435
-- Remove form creator permission (GWUA-4404)
DELETE FROM admin_permission_tbl WHERE permission_name = 'forms.creator';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'forms.creator';
DELETE FROM company_permission_tbl WHERE permission_name = 'forms.creator';
DELETE FROM permission_tbl WHERE permission_name = 'forms.creator';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.01.393', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
