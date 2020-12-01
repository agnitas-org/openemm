/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

update permission_tbl set SUB_CATEGORY='PushNotifications', CATEGORY='Premium' where CATEGORY='PushNotifications';
update permission_tbl set SUB_CATEGORY='Campaigns', CATEGORY='Mailing' where CATEGORY='Campaigns';
update permission_tbl set SUB_CATEGORY='Template', CATEGORY='Mailing' where CATEGORY='Template';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.366', CURRENT_USER, CURRENT_TIMESTAMP);

