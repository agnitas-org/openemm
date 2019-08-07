/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE doc_mapping_tbl SET filename = 'what_are_web_push_notifications.htm' WHERE pagekey = 'Manage_create_send_and_evaluate_push_notifications';
UPDATE doc_mapping_tbl SET filename = 'creating_a_new_push_notification.htm' WHERE pagekey = 'Create_a_new_push_notification';
UPDATE doc_mapping_tbl SET filename = 'push_trigger_notifications.htm' WHERE pagekey = 'Push-trigger_notifications';

INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('push_global_statistics', 'push_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('push_notification_view', 'shipping_settings_and_statistics_for_push_notification.htm');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.268', CURRENT_USER, CURRENT_TIMESTAMP);
