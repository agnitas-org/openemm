/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 20.10.425
-- Drop unused permissio (EMM-6793)
-- Check in: AGN-14

ALTER TABLE admin_tbl DROP COLUMN bk_admin_group_id;

-- Execute changes after version is available on all systems: 21.04.205
-- Drop obsolete column form java code and db table (GWUA-4763)
DELETE FROM admin_permission_tbl WHERE permission_name = 'editor.mediapool.images.load';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'editor.mediapool.images.load';
DELETE FROM company_permission_tbl WHERE permission_name = 'editor.mediapool.images.load';
DELETE FROM permission_tbl WHERE permission_name = 'editor.mediapool.images.load';

-- Execute changes after version is available on all systems: 21.04.232
-- Remove company specific values for cleanup (EMM-7909)
DELETE FROM company_info_tbl WHERE cname = 'system.Report.Expire';
DELETE FROM company_info_tbl WHERE cname = 'system.ExportReport.Expire';

-- Execute change after version is available on all systems: 21.07.066
-- Remove unused permissions
DELETE FROM admin_permission_tbl WHERE permission_name = 'campaign.autoopt';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'campaign.autoopt';
DELETE FROM company_permission_tbl WHERE permission_name = 'campaign.autoopt';
DELETE FROM permission_tbl WHERE permission_name='campaign.autoopt';

-- Execute changes after version is available on all systems: 21.07.267
-- Remove unused config value (EMM-8375)
DELETE FROM company_info_tbl WHERE cname = 'system.DontWriteLatestDatasourceId';

-- Execute change after version is available on all systems: 21.07.207
-- Remove unused permissions
DELETE FROM admin_permission_tbl WHERE permission_name = 'company.view.redesign';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'company.view.redesign';
DELETE FROM company_permission_tbl WHERE permission_name = 'company.view.redesign';
DELETE FROM permission_tbl WHERE permission_name='company.view.redesign';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.088', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
