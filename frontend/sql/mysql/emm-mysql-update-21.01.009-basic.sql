/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE permission_category_tbl (
	category_name              VARCHAR(32) COMMENT 'Technical name of this category',
	sort_order                 INT(11) DEFAULT 0 COMMENT 'Sort order in GUI for this category',
	creation_date              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date of creation of this category',
	PRIMARY KEY (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'All available permission categories';

INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('General', 1);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Mailing', 2);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Template', 3);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Campaigns', 4);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Subscriber-Editor', 5);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('ImportExport', 6);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Target-Groups', 7);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Statistics', 8);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Forms', 9);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Actions', 10);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Administration', 11);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('NegativePermissions', 12);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('System', 13);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Premium', 14);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('PushNotifications', 15);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Messenger', 16);

CREATE TABLE permission_subcategory_tbl (
	category_name              VARCHAR(32) COMMENT 'Technical name of the parent category of this subcategory',
	subcategory_name           VARCHAR(32) COMMENT 'Technical name of this subcategory',
	sort_order                 INT(11) DEFAULT 0 COMMENT 'Sort order in GUI for this subcategory',
	creation_date              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date of creation of this subcategory',
	PRIMARY KEY (category_name, subcategory_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'All available permission subcategories';

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'Settings', 1);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'mailing.searchContent', 2);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'Delivery', 3);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'settings.FormsOfAddress', 4);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'Campaigns', 5);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'Template', 6);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Mailing', 'Mediapool', 7);

ALTER TABLE permission_tbl ADD CONSTRAINT perm$permcat$fk FOREIGN KEY (category) REFERENCES permission_category_tbl (category_name) ON DELETE CASCADE;
ALTER TABLE permission_subcategory_tbl ADD CONSTRAINT permsubcat$permcat$fk FOREIGN KEY (category_name) REFERENCES permission_category_tbl (category_name) ON DELETE CASCADE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.009', CURRENT_USER, CURRENT_TIMESTAMP);
