/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE mailing_tbl ADD COLUMN locking_admin_id INT(11) DEFAULT NULL COMMENT 'if set: references EMM-User (admin_tbl) that was the last one who acquired the locking';
ALTER TABLE mailing_tbl ADD COLUMN locking_expire_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'if set: the timestamp when a locking is not valid anymore';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.181', CURRENT_USER, CURRENT_TIMESTAMP);
