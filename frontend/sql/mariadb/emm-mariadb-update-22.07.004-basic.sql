/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute changes after version is available on all systems: 21.10.124
RENAME TABLE followup_stat_result_tbl TO followup_stat_result_tbl_bk;

-- Execute changes after version is available on all systems: 21.10.201
-- Remove permission for creating a mailinglist out of a target group (EMM-8885)
DELETE FROM admin_permission_tbl WHERE permission_name = 'targets.createml';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'targets.createml';
DELETE FROM company_permission_tbl WHERE permission_name = 'targets.createml';
DELETE FROM permission_tbl WHERE permission_name = 'targets.createml';

-- Execute changes after version is available on all systems: 21.10.237
-- Drop column zip from import_profile_tbl
ALTER TABLE import_profile_tbl CHANGE COLUMN zip bk_zip int(1) DEFAULT 0 COMMENT 'Zip file option for import csv files in zip format';

-- Execute changes after version is available on all systems: 21.10.283
-- Remove permission (GWUA-4955)
DELETE FROM admin_permission_tbl WHERE permission_name = 'targets.access.limit.show';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'targets.access.limit.show';
DELETE FROM company_permission_tbl WHERE permission_name = 'targets.access.limit.show';
DELETE FROM permission_tbl WHERE permission_name = 'targets.access.limit.show';

-- Execute changes after version is available on all systems: 21.10.364
-- Remove permission (EMM-4101)
DELETE FROM company_info_tbl where cname = 'cleanup.bindings_of_deleted_mailinglists';

-- Remove webpush-related configurations not longer requires
DELETE FROM config_tbl WHERE class = 'webpush' AND (name = 'recipient_tracking.max_redirect_token_age' OR name LIKE 'recipient_tracking.max_redirect_token_age.%');
DELETE FROM config_tbl WHERE class = 'webpush' AND (name = 'recipient_tracking.max_redirect_token_age' OR name LIKE 'recipient_tracking.max_redirect_token_age.%');
DELETE FROM config_tbl WHERE class = 'webpush' AND (name = 'recipient_tracking.max_tracking_id_generation_attempts' OR name LIKE 'recipient_tracking.max_tracking_id_generation_attempts.%');

DELETE FROM company_info_tbl WHERE cname = 'webpush.recipient_tracking.max_redirect_token_generation_attempts';
DELETE FROM company_info_tbl WHERE cname = 'webpush.recipient_tracking.max_redirect_token_age';
DELETE FROM company_info_tbl WHERE cname = 'webpush.recipient_tracking.max_tracking_id_generation_attempts';

-- Execute changes after version is available on all systems: 21.10.415
DELETE FROM admin_permission_tbl WHERE permission_name = 'predeliverycheck.show.support';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'predeliverycheck.show.support';
DELETE FROM company_permission_tbl WHERE permission_name = 'predeliverycheck.show.support';
DELETE FROM permission_tbl WHERE permission_name = 'predeliverycheck.show.support';

-- Execute changes after version is available on all systems: 21.10.415
DELETE FROM admin_permission_tbl WHERE permission_name = 'mediatype.print';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mediatype.print';
DELETE FROM company_permission_tbl WHERE permission_name = 'mediatype.print';
DELETE FROM permission_tbl WHERE permission_name = 'mediatype.print';

-- Execute changes after version is available on all systems: 22.01.138
ALTER TABLE rdir_url_tbl CHANGE COLUMN mailingtemplate_id bk_mailingtemplate_id int(10) unsigned DEFAULT NULL COMMENT 'legacy?';

-- Execute changes after version is available on all systems: 22.01.293
DELETE FROM company_info_tbl WHERE cname = 'mailinglist.maxmiumMailinglistApproval';

-- Execute changes after version is available on all systems: 22.01.332
DELETE FROM company_info_tbl WHERE CNAME = 'cleanup.deleteMailingData';

-- Execute changes after version is available on all systems: 22.01.376
-- Remove unused permission (EMM-9201)
DELETE FROM admin_permission_tbl WHERE permission_name = 'statistic.load.specific';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'statistic.load.specific';
DELETE FROM company_permission_tbl WHERE permission_name = 'statistic.load.specific';
DELETE FROM permission_tbl WHERE permission_name = 'statistic.load.specific';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.004', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
