/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE export_predef_tbl ADD dateformat INTEGER COMMENT 'Format of dates without time, see DateFormat-class';
ALTER TABLE export_predef_tbl ADD datetimeformat INTEGER COMMENT 'Format of dates with time, see DateFormat-class';
ALTER TABLE export_predef_tbl ADD timezone VARCHAR(32) COMMENT 'Timezone to export';
ALTER TABLE export_predef_tbl ADD decimalseparator VARCHAR(1) COMMENT 'Decimalseparator for float numbers';

UPDATE export_predef_tbl SET dateformat = 5, datetimeformat = 5, decimalseparator = ',' WHERE dateformat IS NULL AND EXISTS(SELECT 1 FROM auto_export_tbl, admin_tbl
WHERE auto_export_tbl.export_profile_id = export_predef_tbl.export_predef_id AND type = 'Recipient' AND auto_export_tbl.admin_id = admin_tbl.admin_id AND admin_tbl.admin_lang = 'de');

UPDATE export_predef_tbl SET dateformat = 7, datetimeformat = 7, decimalseparator = '.' WHERE dateformat IS NULL;

UPDATE export_predef_tbl SET timezone = (SELECT MAX(admin_tbl.admin_timezone) FROM auto_export_tbl, admin_tbl WHERE auto_export_tbl.export_profile_id = export_predef_tbl.export_predef_id AND type = 'Recipient' AND auto_export_tbl.admin_id = admin_tbl.admin_id AND admin_tbl.admin_lang = 'de')
WHERE timezone IS NULL AND EXISTS(SELECT 1 FROM auto_export_tbl, admin_tbl WHERE auto_export_tbl.export_profile_id = export_predef_tbl.export_predef_id AND type = 'Recipient' AND auto_export_tbl.admin_id = admin_tbl.admin_id);

UPDATE export_predef_tbl SET timezone = 'Europe/Berlin' WHERE timezone IS NULL;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.126', CURRENT_USER, CURRENT_TIMESTAMP);
