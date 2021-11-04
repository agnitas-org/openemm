/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Statistics', 'General', 1);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Statistics', 'Mailing', 2);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Statistics', 'Reports', 3);

UPDATE permission_tbl SET category = 'Statistics', sub_category = 'General', sort_order = 1 WHERE permission_name = 'stats.mailing';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'General', sort_order = 2 WHERE permission_name = 'stats.device';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'General', sort_order = 3 WHERE permission_name = 'stats.domains';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'General', sort_order = 4 WHERE permission_name = 'stats.month';

UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 1 WHERE permission_name = 'stats.ecs';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 2 WHERE permission_name = 'statistic.benchmark.show';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 3 WHERE permission_name = 'stats.revenue';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 4 WHERE permission_name = 'stats.social';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 5 WHERE permission_name = 'statistic.softbounces.show';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Mailing', sort_order = 6 WHERE permission_name = 'statistic.load.specific';

UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Reports', sort_order = 1 WHERE permission_name = 'report.birt.show';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Reports', sort_order = 2 WHERE permission_name = 'report.birt.change';
UPDATE permission_tbl SET category = 'Statistics', sub_category = 'Reports', sort_order = 3 WHERE permission_name = 'report.birt.delete';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.023', CURRENT_USER, CURRENT_TIMESTAMP);
