/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 20.01.368
-- Remove renamed permissions (EMM-6231)
DELETE FROM admin_permission_tbl WHERE security_token = 'upload.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'upload.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'upload.edit';
DELETE FROM permission_tbl WHERE permission_name='upload.edit';

-- Execute changes after version is available on all systems: 20.01.402
-- Removed plugin system (EMM-7161)
DELETE FROM admin_permission_tbl WHERE security_token = 'pluginmanager.change';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'pluginmanager.change';
DELETE FROM company_permission_tbl WHERE security_token = 'pluginmanager.change';
DELETE FROM permission_tbl WHERE permission_name='pluginmanager.change';

DELETE FROM admin_permission_tbl WHERE security_token = 'pluginmanager.show';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'pluginmanager.show';
DELETE FROM company_permission_tbl WHERE security_token = 'pluginmanager.show';
DELETE FROM permission_tbl WHERE permission_name='pluginmanager.show';

DROP TABLE plugins_tbl;

-- Execute changes after version is available on all systems: 533
-- Removed old permission system (EMM-6836)
DELETE FROM company_info_tbl WHERE cname = 'permission.system';

-- Execute changes after version is available on all systems: 20.04.011
-- Drop obsolete fields (EMM-7342)
ALTER TABLE csv_imexport_description_tbl DROP COLUMN change_date_from;
ALTER TABLE csv_imexport_description_tbl DROP COLUMN change_date_till;
 
-- Execute changes after version is available on all systems: 20.04.027
-- Delete obsolete cdn config (EMM-7433)
DELETE FROM company_info_tbl WHERE cname in ('CdnMediaImageRedirectLinkBase', 'CdnMediaBgImageRedirectLinkBase');

-- Execute change after version is available on all systems: 20.04.348
-- Remove obsolete columns (EMM-7052), if still there (check!)
ALTER TABLE company_tbl DROP COLUMN maxadminmails;
ALTER TABLE company_tbl DROP COLUMN expire_cookie;
ALTER TABLE company_tbl DROP COLUMN expire_stat;
ALTER TABLE company_tbl DROP COLUMN expire_bounce;
ALTER TABLE company_tbl DROP COLUMN expire_onepixel;
ALTER TABLE company_tbl DROP COLUMN expire_recipient;
ALTER TABLE company_tbl DROP COLUMN expire_upload;
ALTER TABLE company_tbl DROP COLUMN expire_success;
ALTER TABLE company_tbl DROP COLUMN max_fields;

-- Execute change after version is available on all systems: 20.04.389
-- Drop config value (EMM-7052)
DELETE FROM company_info_tbl WHERE cname = 'CompanyValuesMigrated';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.283', CURRENT_USER, CURRENT_TIMESTAMP);
