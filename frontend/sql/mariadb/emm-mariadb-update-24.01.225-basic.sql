/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT IGNORE INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.show', 'Statistics', 'Reports', 1, NULL, CURRENT_TIMESTAMP);
INSERT IGNORE INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.change', 'Statistics', 'Reports', 2, NULL, CURRENT_TIMESTAMP);
INSERT IGNORE INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.delete', 'Statistics', 'Reports', 3, NULL, CURRENT_TIMESTAMP);

INSERT IGNORE INTO company_permission_tbl (company_id, permission_name) VALUES (0, 'report.birt.delete');
INSERT IGNORE INTO company_permission_tbl (company_id, permission_name) VALUES (0, 'report.birt.change');
INSERT IGNORE INTO company_permission_tbl (company_id, permission_name) VALUES (0, 'report.birt.show');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.225', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
