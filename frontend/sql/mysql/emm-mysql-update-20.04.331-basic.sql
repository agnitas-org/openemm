/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 19.10.041
-- Drop unused columns (EMM-3645)
DELETE FROM config_tbl WHERE class = 'mailloop' AND name LIKE 'actionbased_autoresponder_ui%';
DELETE FROM company_info_tbl WHERE cname = 'mailloop.actionbased_autoresponder_ui';
ALTER TABLE mailloop_tbl DROP COLUMN ar_sender;
ALTER TABLE mailloop_tbl DROP COLUMN ar_subject;
ALTER TABLE mailloop_tbl DROP COLUMN ar_text;
ALTER TABLE mailloop_tbl DROP COLUMN ar_html;

-- Execute change after version is available on all systems: 19.10.069
-- Remove renamed permissions (EMM-6846)
DELETE FROM admin_permission_tbl WHERE security_token = 'temp.dumont';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'temp.dumont';
DELETE FROM company_permission_tbl WHERE security_token = 'temp.dumont';
DELETE FROM permission_tbl WHERE permission_name='temp.dumont';

-- Execute change after version is available on all systems: 19.10.295
-- Remove also Java code "ConfigurationValidityCheckBasicImpl.migrateOldBirtReportDefinitions"
-- Remove obsolete columns (EMM-6939)
ALTER TABLE birtreport_tbl DROP COLUMN send_days;
ALTER TABLE birtreport_tbl DROP COLUMN email;
ALTER TABLE birtreport_tbl DROP COLUMN send_time;
ALTER TABLE birtreport_tbl DROP COLUMN send_date;
ALTER TABLE birtreport_tbl DROP COLUMN delivery_date;

-- Execute change after version is available on all systems: 19.10.389
-- Remove obsolete columns (EMM-7052)
DELETE FROM config_tbl WHERE class = 'expire' AND name = 'statistics.default';
DELETE FROM company_info_tbl WHERE cname = 'expire.statistics.default';
DELETE FROM config_tbl WHERE class = 'expire' AND name = 'OnePixelDef';
DELETE FROM company_info_tbl WHERE cname = 'expire.OnePixelDef';
DELETE FROM config_tbl WHERE class = 'cleanup' AND name = 'bounce-table';
DELETE FROM company_info_tbl WHERE cname = 'cleanup.bounce-table';

-- Execute change after version is available on all systems: 20.01.023
-- Remove obsolete columns (EMM-7103)
ALTER TABLE company_tbl DROP COLUMN max_login_fails;
ALTER TABLE company_tbl DROP COLUMN login_block_time;

-- EMM-6912: Triggerdialog
-- Execute change after version is available on all systems: 20.01.xxx
DELETE FROM company_permission_tbl WHERE security_token IN ('mediatype.print', 'mediatype.triggerdialog');
DELETE FROM admin_permission_tbl WHERE security_token IN ('mediatype.print', 'mediatype.triggerdialog');
DELETE FROM admin_group_permission_tbl WHERE security_token IN ('mediatype.print', 'mediatype.triggerdialog');
DELETE FROM permission_tbl WHERE permission_name IN ('mediatype.print', 'mediatype.triggerdialog');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.331', CURRENT_USER, CURRENT_TIMESTAMP);
