/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Execute change after version is available on all systems: 18.07.031
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'import.mode.null_values';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'import.mode.null_values';
DELETE FROM messages_tbl WHERE message_key ='UserRight.ImportExport.import.mode.null_values';

-- Execute change after version is available on all systems: 18.07.050
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.column.targetgroups';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.column.targetgroups';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.column.targetgroups';

-- Execute change after version is available on all systems: 18.07.086
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.copy';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.copy';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.copy';

-- Execute change after version is available on all systems: 18.07.106
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.default_action';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.default_action';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.default_action';

-- Execute change after version is available on all systems: 18.07.180
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.doublechecking';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.doublechecking';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.doublechecking';

-- Execute change after version is available on all systems: 18.07.215
-- Remove deleted permissionsINSERT INTO admin_permission_tbl (admin_id, security_token) (SELECT admin_id, 'trackpoint.show' FROM admin_permission_tbl a WHERE security_token = 'trackpoint.view' AND NOT EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE security_token = 'trackpoint.show' AND a.admin_id = b.admin_id));
INSERT INTO admin_group_permission_tbl (admin_group_id, security_token) (SELECT admin_group_id, 'trackpoint.show' FROM admin_group_permission_tbl a WHERE security_token = 'trackpoint.view' AND NOT EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE security_token = 'trackpoint.show' AND a.admin_group_id = b.admin_group_id));

DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.graphics_upload';
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.graphics_upload_archive';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.graphics_upload';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.graphics_upload_archive';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.graphics_upload';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.graphics_upload_archive';

-- Execute change after version is available on all systems: 18.07.259
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.needstarget';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.needstarget';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.needstarget';

-- Execute change after version is available on all systems: 18.07.283
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.send.admin';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.send.admin';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.send.admin';
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.send.test';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.send.test';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.send.test';

-- Execute change after version is available on all systems: 18.07.325
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.send.status.email';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.send.status.email';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.send.status.email';

-- Execute change after version is available on all cloud systems: 18.07.511 (EMM Inhouse: 19.04)
-- GWUA-3944: remove obsolete admin preference
DELETE FROM admin_pref_tbl WHERE pref = 'listsize';

-- Execute change after version is available on all systems: 18.07.641
-- Remove deleted permissions
DELETE FROM admin_permission_tbl WHERE security_token = 'mailing.show.charsets';
DELETE FROM admin_group_permission_tbl WHERE security_token = 'mailing.show.charsets';
DELETE FROM messages_tbl WHERE message_key ='UserRight.Mailing.mailing.show.charsets';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.01.432', CURRENT_USER, CURRENT_TIMESTAMP);
