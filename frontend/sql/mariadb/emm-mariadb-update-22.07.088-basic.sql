/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE import_profile_tbl ADD report_locale_lang VARCHAR(10) DEFAULT NULL COMMENT 'Language part of locale used for reports';
ALTER TABLE import_profile_tbl ADD report_locale_country VARCHAR(10) DEFAULT NULL COMMENT 'Country part of locale used for reports';
ALTER TABLE import_profile_tbl ADD report_timezone VARCHAR(50) DEFAULT NULL COMMENT 'Timezone used for reports';

ALTER TABLE auto_import_tbl ADD report_locale_lang VARCHAR(10) DEFAULT NULL COMMENT 'Language part of locale used for reports';
ALTER TABLE auto_import_tbl ADD report_locale_country VARCHAR(10) DEFAULT NULL COMMENT 'Country part of locale used for reports';
ALTER TABLE auto_import_tbl ADD report_timezone VARCHAR(50) DEFAULT NULL COMMENT 'Timezone used for reports';

ALTER TABLE csv_imexport_description_tbl ADD locale_lang VARCHAR(10) DEFAULT NULL COMMENT 'Language part of locale used for reports';
ALTER TABLE csv_imexport_description_tbl ADD locale_country VARCHAR(10) DEFAULT NULL COMMENT 'Country part of locale used for reports';

CREATE TABLE migration_tbl (
	version_number             VARCHAR(15) COMMENT '(EMM-) version of migration - script',
	updating_user              VARCHAR(64) COMMENT 'executing (DB-) user',
	update_timestamp           DATE COMMENT 'execution timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'stores execution times of migration statements';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.088', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
