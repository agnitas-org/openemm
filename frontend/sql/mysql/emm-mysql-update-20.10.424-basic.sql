/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 20.07.097
-- Drop obsolete column form java code and db table (EMM-5085)
ALTER TABLE rdir_url_tbl DROP INDEX rdir_url$rlv_coid_mid$idx;
ALTER TABLE rdir_url_tbl DROP COLUMN relevance;
ALTER TABLE rdir_url_userform_tbl DROP COLUMN relevance;

-- Execute change after version is available on all systems_ 20.07.123
-- Delete permission (EMM-7709)
DELETE FROM admin_permission_tbl WHERE security_token = 'grid.layout.thumbnail';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'grid.layout.thumbnail';
DELETE FROM company_permission_tbl WHERE security_token = 'grid.layout.thumbnail';
DELETE FROM permission_tbl WHERE permission_name = 'grid.layout.thumbnail';

-- Execute change after version is available on all systems: 20.07.188
-- Permission is no longer premium (EMM-6526)
DELETE FROM company_permission_tbl WHERE security_token = 'mailing.text.html';

-- Execute change after version is available on all systems: 20.07.229
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'workflow.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'workflow.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'workflow.edit';
DELETE FROM permission_tbl WHERE permission_name = 'workflow.edit';

-- Execute changes after version is available on all system: 20.07.262
ALTER TABLE dyn_target_tbl DROP COLUMN target_representation;
DELETE FROM config_tbl WHERE class = 'targetgroups' AND name = 'load_from_eql';
DELETE FROM company_info_tbl WHERE cname = 'targetgroups.load_from_eql';
DELETE FROM config_tbl WHERE class = 'targetgroups' AND name = 'build_eql_on_deserialization';
DELETE FROM company_info_tbl WHERE cname = 'targetgroups.build_eql_on_deserialization';
DELETE FROM config_tbl WHERE class = 'targetgroups' AND name = 'migrateOnStartup';
DELETE FROM company_info_tbl WHERE cname = 'targetgroups.migrateOnStartup';

-- Execute change after version is available on all systems: 20.07.267
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'manage.tables.data.create';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'manage.tables.data.create';
DELETE FROM company_permission_tbl WHERE security_token = 'manage.tables.data.create';
DELETE FROM permission_tbl WHERE permission_name = 'manage.tables.data.create';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.424', CURRENT_USER, CURRENT_TIMESTAMP);
