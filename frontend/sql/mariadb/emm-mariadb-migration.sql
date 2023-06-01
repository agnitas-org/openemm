/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Migration of admin configuration values to AutoImport objects: EMM-9197
-- Remove after 22.07.105 is available on all systems.
UPDATE import_profile_tbl SET
		report_locale_lang = (SELECT admin_lang FROM admin_tbl WHERE admin_tbl.admin_id = import_profile_tbl.admin_id),
		report_locale_country = (SELECT admin_country FROM admin_tbl WHERE admin_tbl.admin_id = import_profile_tbl.admin_id),
		report_timezone = (SELECT admin_timezone FROM admin_tbl WHERE admin_tbl.admin_id = import_profile_tbl.admin_id)
	WHERE report_locale_lang IS NULL AND report_locale_country IS NULL AND report_timezone IS NULL AND admin_id IS NOT NULL;

UPDATE auto_import_tbl SET
		report_locale_lang = (SELECT admin_lang FROM admin_tbl WHERE admin_tbl.admin_id = auto_import_tbl.admin_id),
		report_locale_country = (SELECT admin_country FROM admin_tbl WHERE admin_tbl.admin_id = auto_import_tbl.admin_id),
		report_timezone = (SELECT admin_timezone FROM admin_tbl WHERE admin_tbl.admin_id = auto_import_tbl.admin_id)
	WHERE report_locale_lang IS NULL AND report_locale_country IS NULL AND report_timezone IS NULL AND admin_id IS NOT NULL;

INSERT INTO migration_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.088', USER(), CURRENT_TIMESTAMP);

-- Migration of admin configuration values to AutoExport objects: EMM-9197
-- Remove after 22.07.105 is available on all systems.
UPDATE auto_export_tbl SET
		locale_lang = (SELECT admin_lang FROM admin_tbl WHERE admin_tbl.admin_id = auto_export_tbl.admin_id),
		locale_country = (SELECT admin_country FROM admin_tbl WHERE admin_tbl.admin_id = auto_export_tbl.admin_id)
	WHERE locale_lang IS NULL AND locale_country IS NULL AND timezone IS NULL AND admin_id IS NOT NULL;
	
INSERT INTO migration_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.095', USER(), CURRENT_TIMESTAMP);

-- Migration of mailing senddates for display purposes: EMM-8225
-- Remove after Backend version 22.04.155 is available on all systems.
-- worldmailings + followUp
UPDATE mailing_tbl m SET send_date =
	(SELECT MIN(timestamp) FROM mailing_account_tbl acc WHERE m.mailing_id = acc.mailing_id AND acc.status_field = 'W')
	WHERE send_date IS NULL and mailing_type IN (0, 3) AND work_status = 'mailing.status.sent';
-- norecipients
UPDATE mailing_tbl m SET send_date =
	(SELECT MAX(senddate) FROM maildrop_status_tbl mds WHERE m.mailing_id = mds.mailing_id AND mds.status_field = 'W')
	WHERE send_date IS NULL AND mailing_type IN (0, 3) AND work_status = 'mailing.status.norecipients';
INSERT INTO migration_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.154', USER(), CURRENT_TIMESTAMP);

COMMIT;
