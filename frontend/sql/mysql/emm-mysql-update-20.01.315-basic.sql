/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 19.07.117
-- Remove renamed permissions (EMM-6770)
DELETE FROM admin_permission_tbl WHERE security_token = 'targets.eql.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'targets.eql.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'targets.eql.edit';
DELETE FROM permission_tbl WHERE permission_name='targets.eql.edit';

-- Execute change after version is available on all systems: 19.07.187
-- Remove renamed permissions (EMM-6745)
DELETE FROM admin_permission_tbl WHERE security_token = 'masterlog.show';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'masterlog.show';
DELETE FROM company_permission_tbl WHERE security_token = 'masterlog.show';
DELETE FROM permission_tbl WHERE permission_name='masterlog.show';

-- Execute change after version is available on all systems: 19.07.225
-- Remove unused config values (PROJ-1039)
DELETE FROM company_info_tbl WHERE cname='SendMailingWithoutDkimAllowed';
DELETE FROM config_tbl WHERE name='SendMailingWithoutDkimAllowed';
DELETE FROM company_info_tbl WHERE cname='UnsubscribeOldPendingRecipients';
DELETE FROM config_tbl WHERE name='UnsubscribeOldPendingRecipients';

-- Execute change after version is acailable on all systems: 19.07.246
-- Remove unused table for old text import reports
ALTER TABLE prevent_table_drop DROP FOREIGN KEY lock$import_log_tbl;
DROP TABLE import_log_tbl;

-- Execute change after version is available on all systems: 19.07.262
-- Remove unused config values (PROJ-1039)
DELETE FROM company_info_tbl WHERE cname='component-storage';
DELETE FROM config_tbl WHERE name='component-storage';
DELETE FROM company_info_tbl WHERE cname='cdn.destination';
DELETE FROM config_tbl WHERE name='cdn.destination';
DELETE FROM company_info_tbl WHERE cname='cdnmaster.cdnctrl';
DELETE FROM config_tbl WHERE name='cdnmaster.cdnctrl';
DELETE FROM company_info_tbl WHERE cname='dmexco.whatsbroadcast.quickfix';
DELETE FROM config_tbl WHERE name='dmexco.whatsbroadcast.quickfix';

-- Execute change after version is available on all systems: 19.07.274
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'grid.div.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'grid.div.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'grid.div.edit';
DELETE FROM permission_tbl WHERE permission_name='grid.div.edit';

-- Execute change after version is available on all systems: 19.07.278
-- Remove renamed permissions (EMM-6763)
DELETE FROM admin_permission_tbl WHERE security_token = 'statistic.individual.show';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'statistic.individual.show';
DELETE FROM company_permission_tbl WHERE security_token = 'statistic.individual.show';
DELETE FROM permission_tbl WHERE permission_name='statistic.individual.show';

-- Execute change after version is available on all systems: 19.07.290
-- Remove unused config values (PROJ-1039)
DELETE FROM company_info_tbl WHERE cname='expire-mailtracking';
DELETE FROM company_info_tbl WHERE cname='expire_mailtrack';
DELETE FROM company_info_tbl WHERE cname='links.nolinktrackingbydomainpreselection';

-- Execute change after version is available on all systems: 19.07.314
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'grid.grid.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'grid.grid.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'grid.grid.edit';
DELETE FROM permission_tbl WHERE permission_name='grid.grid.edit';

-- Execute change after version is available on all systems: 19.07.325
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.trackablelinks.url.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.trackablelinks.url.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'mailing.trackablelinks.url.edit';
DELETE FROM permission_tbl WHERE permission_name='mailing.trackablelinks.url.edit';

-- Execute change after version is available on all systems: 19.07.334
-- Remove renamed permissions (EMM-6720)
DELETE FROM admin_permission_tbl WHERE security_token = 'import.auto.manual';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'import.auto.manual';
DELETE FROM company_permission_tbl WHERE security_token = 'import.auto.manual';
DELETE FROM permission_tbl WHERE permission_name='import.auto.manual';

-- Execute change after version is available on all systems: 19.07.334
-- Remove renamed permissions (EMM-6720)
DELETE FROM admin_permission_tbl WHERE security_token = 'recipient.export.auto.manual';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'recipient.export.auto.manual';
DELETE FROM company_permission_tbl WHERE security_token = 'recipient.export.auto.manual';
DELETE FROM permission_tbl WHERE permission_name='recipient.export.auto.manual';

-- Execute change after version is available on all systems: 19.07.339
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'push.trigger.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'push.trigger.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'push.trigger.edit';
DELETE FROM permission_tbl WHERE permission_name='push.trigger.edit';

-- Execute change after version is available on all systems: 19.07.439
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'report.birt.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'report.birt.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'report.birt.edit';
DELETE FROM permission_tbl WHERE permission_name='report.birt.edit';

-- Execute change after version is available on all systems: 19.07.464
-- Remove unused config values (PROJ-1039)
DELETE FROM company_info_tbl WHERE cname='recipients.allowDoNotTrack';
DELETE FROM company_info_tbl WHERE cname='redirection.status';
DELETE FROM company_info_tbl WHERE cname='responsive-layout';
DELETE FROM company_info_tbl WHERE cname='socialmediafinder.apikey';
DELETE FROM company_info_tbl WHERE cname='socialmediafinder.url';
DELETE FROM company_info_tbl WHERE cname='system.CleanWorkflowTargets';
DELETE FROM company_info_tbl WHERE cname='system.MigrateWorkflowTargetRepresentation';

-- Execute change after version is available on all systems: 19.07.513
-- Remove unused config values (PROJ-1039)
DELETE FROM company_info_tbl WHERE cname='webservice.password.convertMD5toSHA512onStartup';
DELETE FROM company_info_tbl WHERE cname='whatsbroadcast.apiKey';
DELETE FROM company_info_tbl WHERE cname='whatsbroadcast.attachmentHost';

-- Execute change after version is available on all systems: 19.07.566
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'supervisors.edit';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'supervisors.edit';
DELETE FROM company_permission_tbl WHERE security_token = 'supervisors.edit';
DELETE FROM permission_tbl WHERE permission_name='supervisors.edit';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.01.315', CURRENT_USER, CURRENT_TIMESTAMP);
