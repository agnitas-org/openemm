/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 18.04.159
-- Remove deleted permission
DELETE FROM admin_permission_tbl WHERE security_token = 'actions.set_usage';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'actions.set_usage';

-- Execute change after version is available on all systems: 18.04.245
-- Remove deleted permission
DELETE FROM admin_permission_tbl WHERE security_token = 'export.auto.manual';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'export.auto.manual';

-- Execute change after version is available on all systems: 18.04.353
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'admin.login.fails.showWarning';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'admin.login.fails.showWarning';

-- Execute change after version is available on all systems: 18.04.394
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'administration.show';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'administration.show';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Administration.administration.show';

-- Execute change after version is available on all systems: 18.04.421
-- GWUA-3870: fixed for multi-threading environment
DROP TABLE temp_customer_tbl;

-- Execute change after version is available on all systems: 18.04.435
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'campaign.new';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'campaign.new';

-- Execute change after version is available on all systems: 18.04.449
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'dashboard.layout.reduced';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'dashboard.layout.reduced';

-- Execute change after version is available on all systems: 18.04.483
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'export.notify';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'export.notify';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Account.export.notify';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.286', CURRENT_USER, CURRENT_TIMESTAMP);
