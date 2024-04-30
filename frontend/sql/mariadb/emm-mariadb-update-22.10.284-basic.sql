/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- EMM-8146: Remove obsolete configuration keys
-- Execute changes after version is available on all systems: 22.04.045
DELETE FROM company_info_tbl WHERE cname = 'development.useNewWebserviceRateLimiting';
DELETE FROM config_tbl WHERE class = 'development' AND name = 'useNewWebserviceRateLimiting';

-- EMM-9290: delete unused permission
-- Execute changes after version is available on all systems: 22.04.045
DELETE FROM admin_permission_tbl WHERE permission_name = 'import.mode.global_optout';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'import.mode.global_optout';
DELETE FROM company_permission_tbl WHERE permission_name = 'import.mode.global_optout';
DELETE FROM permission_tbl WHERE permission_name = 'import.mode.global_optout';

-- EMM-8946: Remove obsolete configuration keys
-- Execute changes after version is available on all systems: 22.04.189
DELETE FROM company_info_tbl WHERE cname = 'expire.onepixel';
DELETE FROM company_info_tbl WHERE cname = 'expire.OnePixelMax';

-- EMM-8138: delete unused permission
-- Execute changes after version is available on all systems: 22.04.333
DELETE FROM admin_permission_tbl WHERE permission_name = 'mediatype.mms';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mediatype.mms';
DELETE FROM company_permission_tbl WHERE permission_name = 'mediatype.mms';
DELETE FROM permission_tbl WHERE permission_name = 'mediatype.mms';

-- GWUA-5109: update info about workflows for mailings
-- After the script is executed, replace reading data from the workflow_dependency_tbl table with reading data from the workflow_id column of the mailing_tbl.
-- Execute changes after version is available on all systems: 22.04.342
UPDATE mailing_tbl m
SET workflow_id = (SELECT MAX(workflow_id) FROM workflow_dependency_tbl WHERE type = 4 AND entity_id = m.mailing_id)
WHERE EXISTS(SELECT 1 FROM workflow_dependency_tbl WHERE type = 4 AND entity_id = m.mailing_id);

-- GWUA-5108: delete MMS entries
-- Clear DB after the MMS mediatype feature is finally removed from all servers
DELETE FROM component_tbl WHERE compname = 'agnMMS';
DELETE FROM mailing_mt_tbl WHERE mediatype = 3;

-- Execute changes after preparation in 22.07.005 is available on all systems
ALTER TABLE import_profile_tbl DROP COLUMN bk_zip;

-- MySQL/MariaDB only
-- Execute changes after preparation in 22.07.005 is available on all systems
ALTER TABLE rdir_url_tbl DROP COLUMN bk_mailingtemplate_id;

-- EMM-9021
-- Remove permission 'recipient.import.auto.mailing' from db after 22.07.333 is globally available
DELETE FROM admin_permission_tbl WHERE permission_name = 'recipient.import.auto.mailing';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'recipient.import.auto.mailing';
DELETE FROM company_permission_tbl WHERE permission_name = 'recipient.import.auto.mailing';
DELETE FROM permission_tbl WHERE permission_name = 'recipient.import.auto.mailing';

-- Cleanup migration permissions
DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.create.classic.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.create.classic.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.create.classic.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.create.classic.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.create.emc.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.create.emc.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.create.emc.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.create.emc.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.settings.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.settings.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.settings.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.settings.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.parameter.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.parameter.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.parameter.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.parameter.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.content.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.content.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.content.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.content.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.styles.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.styles.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.styles.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.styles.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.send.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.send.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.send.migration';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.send.migration';

-- GWUA-5326
-- Remove permission 'mailing.send.threshold' from db after 22.10.088 is globally available
DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.send.threshold';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.send.threshold';
DELETE FROM company_permission_tbl WHERE permission_name = 'mailing.send.threshold';
DELETE FROM permission_tbl WHERE permission_name = 'mailing.send.threshold';

-- GWUA-5341
-- Execute changes after version is available on all systems: 22.10.147
-- Drop redundant columns and data
DELETE FROM admin_permission_tbl WHERE permission_name = 'grid.div.lock';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'grid.div.lock';
DELETE FROM company_permission_tbl WHERE permission_name = 'grid.div.lock';
DELETE FROM permission_tbl WHERE permission_name = 'grid.div.lock';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.10.284', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
