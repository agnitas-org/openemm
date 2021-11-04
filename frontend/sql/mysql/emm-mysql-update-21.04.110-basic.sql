/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Administration', 'settings.Admin', 1);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Administration', 'settings.Usergroups', 2);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Administration', 'Company', 3);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Administration', 'settings.Mailloop', 4);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Administration', 'ContentSource', 5);

UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 1 WHERE permission_name = 'admin.new';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 2 WHERE permission_name = 'admin.show';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 3 WHERE permission_name = 'admin.change';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 4 WHERE permission_name = 'admin.delete';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 5 WHERE permission_name = 'admin.setgroup';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 6 WHERE permission_name = 'admin.setpermission';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 7 WHERE permission_name = 'admin.sendWelcome';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Admin', sort_order = 8 WHERE permission_name = 'adminlog.show';

UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Usergroups', sort_order = 1 WHERE permission_name = 'role.show';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Usergroups', sort_order = 2 WHERE permission_name = 'role.change';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Usergroups', sort_order = 3 WHERE permission_name = 'role.delete';

UPDATE permission_tbl SET category = 'Administration', sub_category = 'Company', sort_order = 1 WHERE permission_name = 'company.new';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'Company', sort_order = 2 WHERE permission_name = 'company.show';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'Company', sort_order = 3 WHERE permission_name = 'company.change';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'Company', sort_order = 4 WHERE permission_name = 'company.delete';

UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Mailloop', sort_order = 1 WHERE permission_name = 'mailloop.show';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Mailloop', sort_order = 2 WHERE permission_name = 'mailloop.change';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'settings.Mailloop', sort_order = 3 WHERE permission_name = 'mailloop.delete';

UPDATE permission_tbl SET category = 'Administration', sub_category = 'ContentSource', sort_order = 1 WHERE permission_name = 'contentsource.show';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'ContentSource', sort_order = 2 WHERE permission_name = 'contentsource.change';
UPDATE permission_tbl SET category = 'Administration', sub_category = 'ContentSource', sort_order = 3 WHERE permission_name = 'contentsource.delete';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('21.04.110', CURRENT_USER, CURRENT_TIMESTAMP);
