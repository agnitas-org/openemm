/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE permission_tbl (
	permission_name            VARCHAR(64) NOT NULL COMMENT 'Name of permission, a.k.a security_token',
	category                   VARCHAR(32) NOT NULL COMMENT 'Category this permission is sorted in',
	sub_category               VARCHAR(32) DEFAULT NULL COMMENT 'Sub-Category this permission is sorted in (optional)',
	sort_order                 INTEGER DEFAULT 0 COMMENT 'Sorting order of this permission within its category',
	feature_package            VARCHAR(32) DEFAULT NULL COMMENT 'Feature package this permission is contained in (optional)',
	creation_date              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date of creation'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'All available permissions';
ALTER TABLE permission_tbl ADD CONSTRAINT perm$name$pk PRIMARY KEY (permission_name);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('company.force.sending', 'Account', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('export.notify', 'Account', NULL, 6, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.ActivateDoubleOptIn', 'Actions', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.ContentView', 'Actions', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.ExecuteScript', 'Actions', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.GetArchiveList', 'Actions', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.GetArchiveMailing', 'Actions', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.GetCustomer', 'Actions', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.IdentifyCustomer', 'Actions', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.SendMailing', 'Actions', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.ServiceMail', 'Actions', NULL, 9, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.SubscribeCustomer', 'Actions', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.UnsubscribeCustomer', 'Actions', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('action.op.UpdateCustomer', 'Actions', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('actions.change', 'Actions', NULL, 13, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('actions.delete', 'Actions', NULL, 14, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('actions.show', 'Actions', NULL, 15, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.change', 'Administration', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.delete', 'Administration', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('adminlog.show', 'Administration', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.new', 'Administration', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.password.once', 'Administration', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.setgroup', 'Administration', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.setpermission', 'Administration', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('admin.show', 'Administration', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailloop.change', 'Administration', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailloop.delete', 'Administration', NULL, 13, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailloop.show', 'Administration', NULL, 14, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('role.change', 'Administration', NULL, 15, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('role.create', 'Administration', NULL, 16, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('role.delete', 'Administration', NULL, 17, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('role.show', 'Administration', NULL, 18, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('userlog.show', 'Administration', NULL, 19, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('campaign.change', 'Campaigns', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('campaign.delete', 'Campaigns', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('campaign.show', 'Campaigns', NULL, 4, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('forms.change', 'Forms', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('forms.delete', 'Forms', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('forms.import', 'Forms', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('forms.show', 'Forms', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('forms.view', 'Forms', NULL, 6, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('calendar.show', 'General', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('charset.use.iso_8859_15', 'General', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('charset.use.utf_8', 'General', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('settings.open', 'General', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('upload.admin', 'General', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('upload.list', 'General', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('workflow.activate', 'General', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('workflow.delete', 'General', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('workflow.edit', 'General', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('workflow.show', 'General', NULL, 13, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mailinglist.without', 'ImportExport', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.add', 'ImportExport', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.add_update', 'ImportExport', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.blacklist', 'ImportExport', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.bounce', 'ImportExport', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.bouncereactivate', 'ImportExport', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.doublechecking', 'ImportExport', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.duplicates', 'ImportExport', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.only_update', 'ImportExport', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.reactivateSuspended', 'ImportExport', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.unsubscribe', 'ImportExport', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.preprocessing', 'ImportExport', NULL, 13, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.change.bulk', 'ImportExport', NULL, 14, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('wizard.export', 'ImportExport', NULL, 15, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('wizard.import', 'ImportExport', NULL, 16, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('wizard.importclassic', 'ImportExport', NULL, 17, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailinglist.change', 'Mailinglist', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailinglist.delete', 'Mailinglist', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailinglist.new', 'Mailinglist', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailinglist.recipients.delete', 'Mailinglist', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailinglist.show', 'Mailinglist', NULL, 5, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.attachments.show', 'Mailing', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.can_allow', 'Mailing', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.can_send_always', 'Mailing', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.change', 'Mailing', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.components.change', 'Mailing', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.components.show', 'Mailing', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.content.show', 'Mailing', NULL, 9, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.content.showExcludedTargetgroups', 'Mailing', NULL, 0, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.contentsource.date.limit', 'Mailing', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.delete', 'Mailing', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.envelope_address', 'Mailing', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.extend_trackable_links', 'Mailing', NULL, 14, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.import', 'Mailing', NULL, 16, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.send.admin.options', 'Mailing', NULL, 19, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.send.admin.target', 'Mailing', NULL, 20, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.send.show', 'Mailing', NULL, 21, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.send.world', 'Mailing', NULL, 22, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.setmaxrecipients', 'Mailing', NULL, 23, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.show', 'Mailing', NULL, 24, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.show.types', 'Mailing', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.target.calculate', 'Mailing', NULL, 25, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.email', 'Mailing', NULL, 29, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.gender.extended', 'Mailing', NULL, 30, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('salutation.change', 'Mailing', NULL, 31, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('salutation.delete', 'Mailing', NULL, 32, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('salutation.show', 'Mailing', NULL, 33, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.editor.trimmed', 'NegativePermissions', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.settings.hide', 'NegativePermissions', NULL, 3, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.history.mailing', 'Premium', 'AutomationPackage', 3, 'Automation Package');
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.advancedRules2.recipients', 'Premium', 'AutomationPackage', 4, 'Automation Package');

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.customerid', 'Premium', 'ImportExport', 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mapping.auto', 'Premium', 'ImportExport', 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('import.mode.add_update_exclusive', 'Premium', 'ImportExport', 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.import.encrypted', 'Premium', 'ImportExport', 19, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('grid.change', 'Premium', 'LayoutPackage', 1, 'Layout Package');
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('grid.css.default.edit', 'Premium', 'LayoutPackage', 10, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.components.sftp', 'Premium', 'Mailing', 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.content.disableLinkExtension', 'Premium', 'Mailing', 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.parameter.change', 'Premium', 'Mailing', 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.parameter.show', 'Premium', 'Mailing', 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.parameter.view', 'Premium', 'Mailing', 9, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.trackablelinks.nocleanup', 'Premium', 'Mailing', 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.trackablelinks.static', 'Premium', 'Mailing', 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.trackablelinks.url.edit', 'Premium', 'Mailing', 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.fax', 'Premium', 'Mailing', 13, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.mms', 'Premium', 'Mailing', 14, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.print', 'Premium', 'Mailing', 15, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.sms', 'Premium', 'Mailing', 16, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mediatype.whatsapp', 'Premium', 'Mailing', 17, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('heine.svcmail_frontend', 'Premium', 'others', 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('pluginmanager.change', 'Premium', 'others', 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('pluginmanager.show', 'Premium', 'others', 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.profileField.html.allowed', 'Premium', 'others', 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('statistic.individual.show', 'Premium', 'others', 6, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('deeptracking', 'Premium', 'RetargetingPackage', 1, 'Retargeting Package');
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('trackpoint.new', 'Premium', 'RetargetingPackage', 15, 'Retargeting Package');

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('statistic.load.specific', 'Statistics', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('statistic.softbounces.show', 'Statistics', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.domains', 'Statistics', NULL, 9, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.ecs', 'Statistics', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.mailing', 'Statistics', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.month', 'Statistics', NULL, 12, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.rdir', 'Statistics', NULL, 13, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.show', 'Statistics', NULL, 15, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('stats.userform', 'Statistics', NULL, 17, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('blacklist', 'Subscriber-Editor', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('profileField.show', 'Subscriber-Editor', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('profileField.sort', 'Subscriber-Editor', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('profileField.visible', 'Subscriber-Editor', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.change', 'Subscriber-Editor', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.create', 'Subscriber-Editor', NULL, 6, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.delete', 'Subscriber-Editor', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.history', 'Subscriber-Editor', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.list', 'Subscriber-Editor', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.show', 'Subscriber-Editor', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipient.tracking.veto', 'Subscriber-Editor', NULL, 12, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('clear.cache', 'System', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('company.authentication', 'System', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.content.change.always', 'System', NULL, 7, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.expire', 'System', NULL, 8, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('master.companies.show', 'System', NULL, 9, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('masterlog.show', 'System', NULL, 10, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('master.show', 'System', NULL, 11, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('restful.allowed', 'System', NULL, 17, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('server.status', 'System', NULL, 18, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('supervisors.list', 'System', NULL, 21, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('update.show', 'System', NULL, 23, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('user.xpress', 'System', NULL, 24, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('webservice.user.change', 'System', NULL, 25, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('webservice.user.create', 'System', NULL, 26, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('webservice.user.show', 'System', NULL, 27, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('temp.alpha', 'System', NULL, 28, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('temp.beta', 'System', NULL, 29, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('temp.gamma', 'System', NULL, 30, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('temp.dumont', 'System', NULL, 31, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('recipientsreport.migration', 'System', NULL, 32, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('user.activity.log.rollback', 'System', NULL, 33, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('content_tab_migration', 'System', NULL, 34, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.change', 'Target-Groups', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.createml', 'Target-Groups', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.delete', 'Target-Groups', NULL, 3, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.eql.edit', 'Target-Groups', NULL, 4, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.lock', 'Target-Groups', NULL, 5, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('targets.show', 'Target-Groups', NULL, 6, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('template.change', 'Template', NULL, 1, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('template.delete', 'Template', NULL, 2, NULL);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('template.show', 'Template', NULL, 3, NULL);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.07.215', CURRENT_USER, CURRENT_TIMESTAMP);
