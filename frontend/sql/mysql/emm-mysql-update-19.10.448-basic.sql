/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


-- Execute change after version is available on all systems: 19.04.020
-- Remove permissions (EMM-5551)
DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.ActivateDoubleOptIn';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.ActivateDoubleOptIn';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.ActivateDoubleOptIn';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.ActivateDoubleOptIn';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.ActivateDoubleOptIn';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.ContentView';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.ContentView';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.ContentView';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.ContentView';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.ContentView';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.ExecuteScript';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.ExecuteScript';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.ExecuteScript';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.ExecuteScript';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.ExecuteScript';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.GetArchiveList';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.GetArchiveList';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.GetArchiveList';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.GetArchiveList';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.GetArchiveList';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.GetArchiveMailing';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.GetArchiveMailing';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.GetArchiveMailing';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.GetArchiveMailing';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.GetArchiveMailing';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.GetCustomer';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.GetCustomer';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.GetCustomer';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.GetCustomer';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.GetCustomer';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.IdentifyCustomer';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.IdentifyCustomer';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.IdentifyCustomer';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.IdentifyCustomer';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.IdentifyCustomer';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.SendMailing';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.SendMailing';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.SendMailing';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.SendMailing';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.SendMailing';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.ServiceMail';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.ServiceMail';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.ServiceMail';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.ServiceMail';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.ServiceMail';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.SubscribeCustomer';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.SubscribeCustomer';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.SubscribeCustomer';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.SubscribeCustomer';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.SubscribeCustomer';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.UnsubscribeCustomer';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.UnsubscribeCustomer';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.UnsubscribeCustomer';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.UnsubscribeCustomer';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.UnsubscribeCustomer';

DELETE FROM admin_permission_tbl WHERE security_token = 'action.op.UpdateCustomer';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'action.op.UpdateCustomer';
DELETE FROM company_permission_tbl WHERE security_token = 'action.op.UpdateCustomer';
DELETE FROM permission_tbl WHERE permission_name = 'action.op.UpdateCustomer';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Actions.action.op.UpdateCustomer';

-- Remove unneeded column
ALTER TABLE rdir_action_tbl DROP COLUMN action_sql;

-- Execute change after version is available on all systems: 19.04.256
-- Remove renamed permissions (EMM-6233)
DELETE FROM admin_permission_tbl WHERE security_token = 'trackpoint.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'trackpoint.new';
DELETE FROM company_permission_tbl WHERE security_token = 'trackpoint.new';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Premium#RetargetingPackage.trackpoint.new';

-- Execute change after version is available on all systems: 19.04.287
-- Remove permissions (EMM-6569)
DELETE FROM admin_permission_tbl WHERE security_token = 'clear.cache';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'clear.cache';
DELETE FROM company_permission_tbl WHERE security_token = 'clear.cache';
DELETE FROM messages_tbl WHERE message_key ='UserRight.System.clear.cache';

-- Execute change after version is available on all systems: 19.04.318
-- Remove cleanup configvalue (EMM-6553)
DELETE FROM company_info_tbl WHERE cname = 'cleanup.deleteRecipientsData';

DELETE FROM company_info_tbl WHERE cname = 'MigrateLinkExtensions' AND cvalue = 'false';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.448', CURRENT_USER, CURRENT_TIMESTAMP);
