/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- EMM-5851: drop unused column when 19.04.000 LTS is made available
-- Remove also Java Code: com.agnitas.dao.impl.ComMailingDaoImpl.migrateOldLinkExtension
ALTER TABLE mailing_tbl DROP COLUMN trackable_link_extension;
ALTER TABLE userform_tbl DROP COLUMN trackable_link_extension;

-- Execute change after version is available on all systems: 18.10.008
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.target.calculate';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.target.calculate';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Mailing.mailing.target.calculate';

-- Execute change after version is available on all systems: 18.10.041
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailinglist.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailinglist.new';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Mailinglist.mailinglist.new';

-- Execute change after version is available on all systems: 18.10.060
-- Drop obsolete rdir_traffic_amount_tbl without companyid in name
DROP TABLE rdir_traffic_amount_tbl;

-- Execute change after version is available on all systems: 18.10.066
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'admin.password.once';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'admin.password.once';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Administration.admin.password.once';

-- Execute change after version is available on all systems: 18.10.126
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'profileField.sort';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'profileField.sort';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Subscriber-Editor.profileField.sort';

-- Execute change after version is available on all systems: 18.10.134
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'recipient.list';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'recipient.list';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Subscriber-Editor.recipient.list';

-- Execute change after version is available on all systems: 18.10.159
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'role.create';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'role.create';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Administration.role.create';

-- Execute change after version is available on all systems: 18.10.168
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'settings.open';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'settings.open';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.General.settings.open';

-- Execute change after version is available on all systems: 18.10.168
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'stats.rdir';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'stats.rdir';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Statistics.stats.rdir';

-- Execute change after version is available on all systems: 18.10.246
-- Remove unneeded column
ALTER TABLE admin_tbl DROP COLUMN lastmessageoftheday;

-- Execute change after version is available on all systems: 18.10.274
-- Remove unneeded column
ALTER TABLE admin_tbl DROP COLUMN lastpopup;

-- Execute change after version is available on all systems: 18.10.328
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'template.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'template.new';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Template.template.new';

-- Execute change after version is available on all systems: 18.10.373
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'userlog.show';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'userlog.show';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Administration.userlog.show';

-- Execute change after version is available on all systems: 18.10.389
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'upload.admin';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'upload.admin';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.General.upload.admin';

-- Execute change after version is available on all systems: 18.10.420
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'heine.svcmail_frontend';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'heine.svcmail_frontend';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Premium#others.heine.svcmail_frontend';

-- Execute change after version is available on all systems: 18.10.468
-- Remove migration permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'upload.migration';

-- Execute change after version is available on all systems: 18.10.493
-- Remove renamed permission (EMM-6232, GWUA-4088)
DELETE FROM admin_permission_tbl WHERE security_token = 'forms.view';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'forms.view';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Forms.forms.view';

-- Execute change after version is available on all systems: 19.01.013
-- Remove renamed permission (EMM-6232)
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.parameter.view';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.parameter.view';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Premium#Mailing.mailing.parameter.view';

-- Execute change after version is available on all systems: 19.01.107
-- Remove permission (EMM-6314)
DELETE FROM admin_permission_tbl WHERE security_token = 'targets.advancedRules2.recipients';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'targets.advancedRules2.recipients';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Premium#AutomationPackage.targets.advancedRules2.recipients';

-- Execute changes after version is available on all systems: 19.01.132
-- Remove configuration (EMM-6330)
DELETE FROM company_info_tbl WHERE cname = 'targetgroups.use_querybuilder';
DELETE FROM config_tbl WHERE class = 'targetgroups' and name = 'user_querybuilder';

-- Execute changes after version is available on all systems: 19.01.240
-- Remove obsolete column (EMM-5459)
ALTER TABLE mailing_tbl DROP COLUMN xor_key;
ALTER TABLE company_tbl DROP COLUMN xor_key;
ALTER TABLE admin_use_tbl DROP COLUMN last_hint;

-- Execute change after version is available on all systems: 19.01.306
-- Remove renamed permissions (EMM-6232)
DELETE FROM admin_permission_tbl WHERE security_token = 'UserRight.Premium#RetargetingPackage.trackpoint.view';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'UserRight.Premium#RetargetingPackage.trackpoint.view';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Premium#RetargetingPackage.trackpoint.view';

-- Execute change after version is available on all system: 19.01.335
DELETE FROM company_info_tbl WHERE cname = 'security.allowOldPasswordHash';
DELETE FROM company_info_tbl WHERE cname = 'security.supervisor.allowOldPasswordHash';
DELETE FROM company_info_tbl WHERE cname = 'security.createNewPasswordHash';
DELETE FROM company_info_tbl WHERE cname = 'security.supervisor.createNewPasswordHash';

-- Execute change after version is available on all systems: 19.01.408
-- Remove renamed permissions (EMM-6404)
DELETE FROM admin_permission_tbl WHERE security_token = 'iwb_date';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'iwb_date';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Mailing.iwb_date';

-- Execute change after version is available on all systems: 19.01.408
-- Remove renamed permissions (EMM-6232)
DELETE FROM admin_permission_tbl WHERE security_token = 'upload.list';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'upload.list';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.General.upload.list';

-- Execute change after version is available on all system: 19.01.409
DELETE FROM company_info_tbl WHERE cname = 'component-storage';
DELETE FROM company_info_tbl WHERE cname = 'component.directory';
DELETE FROM config_tbl WHERE class = 'component' AND name = 'directory';

-- Execute change after version is available on all systems: 19.01.446
-- Remove renamed permissions (EMM-6232)
DELETE FROM admin_permission_tbl WHERE security_token = 'supervisors.list';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'supervisors.list';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.System.supervisors.list';

-- Execute change after version is available on all system: 19.01.485
-- Remove obsolete configuratin (EMM-6113)
DELETE FROM company_info_tbl WHERE cname = 'targetgroups.transform.shiftNotDown';

-- Execute change after version is available on all systems: 19.01.508
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'contentsource.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'contentsource.new';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.System.contentsource.new';

-- Execute change after version is available on all systems: 19.01.593
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.new';
DELETE FROM messages_tbl WHERE message_key = 'UserRight.Mailing.mailing.new';

DELETE FROM company_info_tbl WHERE cname = 'MigrateLinkExtensions' and cvalue = 'false';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.07.174', CURRENT_USER, CURRENT_TIMESTAMP);
