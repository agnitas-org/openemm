/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.send.migration', 'System', 'Migration', 5, NULL);
	
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.settings.migration', 'System', 'Migration', 6, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.create.classic.migration', 'System', 'Migration', 7, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.parameter.migration', 'System', 'Migration', 34, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.styles.migration', 'System', 'Migration', 44, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.create.emc.migration', 'System', 'Migration', 54, NULL);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	VALUES ('mailing.content.migration', 'System', 'Migration', 55, NULL);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.10.035', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
